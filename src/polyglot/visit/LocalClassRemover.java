/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeNode;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.LocalClassDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.util.UniqueID;

// TODO:
//Convert closures to anon
//Add frame classes around anon and local
//now all classes access only final locals
//Convert local and anon to member
//Dup inner member to static
//Remove inner member

public class LocalClassRemover extends ContextVisitor {
    protected final class ConstructorCallRewriter extends NodeVisitor {
        private final List<FieldInstance> newFields;
        private final ClassType theLocalClass;
        ParsedClassType curr;

        protected ConstructorCallRewriter(List<FieldInstance> fields,
                ClassType ct) {
            this.newFields = fields;
            this.theLocalClass = ct;
        }

        @Override
        public NodeVisitor enter(Node n) {
            if (n instanceof ClassDecl) {
                ConstructorCallRewriter v = (ConstructorCallRewriter) copy();
                v.curr = ((ClassDecl) n).type();
                return v;
            }
            return this;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof New) {
                New neu = (New) n;
                ConstructorInstance ci = neu.constructorInstance();
                ConstructorInstance nci =
                        (ConstructorInstance) ci.declaration();
                if (nci.container().toClass().declaration() == theLocalClass.declaration()) {
                    neu =
                            (New) neu.arguments(addArgs(neu,
                                                        nci,
                                                        newFields,
                                                        curr,
                                                        theLocalClass));
                    if (!theLocalClass.flags().isStatic()) {
                        Expr q;
                        if (theLocalClass.outer() == context.currentClass())
                            q =
                                    nf.This(neu.position())
                                      .type(theLocalClass.outer());
                        else q =
                                nf.This(neu.position(),
                                        nf.CanonicalTypeNode(neu.position(),
                                                             theLocalClass.outer()))
                                  .type(theLocalClass.outer());
                        neu = neu.qualifier(q);
                    }
                }
                return neu;
            }
            if (n instanceof ConstructorCall) {
                ConstructorCall neu = (ConstructorCall) n;
                ConstructorInstance ci = neu.constructorInstance();
                ConstructorInstance nci =
                        (ConstructorInstance) ci.declaration();
                if (nci.container().toClass().declaration() == theLocalClass.declaration()) {
                    neu =
                            (ConstructorCall) neu.arguments(addArgs(neu,
                                                                    nci,
                                                                    newFields,
                                                                    curr,
                                                                    theLocalClass));
                    // This is wrong: we cannot refer to this in a super() call qualifier.
                    // For now, let this pass and assume InnerClassRemover will fix it.
                    if (!theLocalClass.flags().isStatic()) {
                        Expr q;
                        if (theLocalClass.outer() == context.currentClass())
                            q =
                                    nf.This(neu.position())
                                      .type(theLocalClass.outer());
                        else q =
                                nf.This(neu.position(),
                                        nf.CanonicalTypeNode(neu.position(),
                                                             theLocalClass.outer()))
                                  .type(theLocalClass.outer());
                        neu = neu.qualifier(q);
                    }
                }
                return neu;
            }

            return n;
        }
    }

    public LocalClassRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    Map<Pair<LocalInstance, ClassType>, FieldInstance> fieldForLocal =
            new HashMap<Pair<LocalInstance, ClassType>, FieldInstance>();
    Map<ParsedClassType, List<ClassDecl>> orphans =
            new HashMap<ParsedClassType, List<ClassDecl>>();
    Map<ParsedClassType, List<FieldInstance>> newFields =
            new HashMap<ParsedClassType, List<FieldInstance>>();

    @Override
    public Node override(Node parent, Node n) {
        // Find local classes in a block and remove them.
        // Rewrite local classes to instance member classes.
        // Add a field to the local class for each captured local variable.
        // Add a formal to each constructor for each introduced field.
        // Add a field initializer to each constructor for each introduced field.
        // Rewrite constructor calls to pass in the locals.

        // TODO: handle SwitchBlock correctly

        if (n instanceof Block) {
            final Block b = (Block) n;
            List<Stmt> ss = new ArrayList<Stmt>(b.statements());
            for (int i = 0; i < ss.size(); i++) {
                Stmt s = ss.get(i);
                if (s instanceof LocalClassDecl) {
                    s = (Stmt) n.visitChild(s, this);

                    LocalClassDecl lcd = (LocalClassDecl) s;
                    ClassDecl cd = lcd.decl();
                    Flags flags =
                            context.inStaticContext() ? Flags.PRIVATE.Static()
                                    : Flags.PRIVATE;
                    cd = cd.flags(flags);
                    cd.type().flags(flags);
                    cd.type().kind(ClassType.MEMBER);

                    cd =
                            rewriteLocalClass(cd,
                                              hashGet(newFields,
                                                      cd.type(),
                                                      Collections.<FieldInstance> emptyList()));

                    if (cd != lcd.decl()) {
                        ss.set(i, lcd.decl(cd));

                        // Rewrite the constructor calls in the remaining statements, including the class declaration statement
                        // itself.
                        for (int j = i; j < ss.size(); j++) {
                            Stmt sj = ss.get(j);
                            sj =
                                    (Stmt) rewriteConstructorCalls(sj,
                                                                   cd.type(),
                                                                   hashGet(newFields,
                                                                           cd.type(),
                                                                           Collections.<FieldInstance> emptyList()));
                            ss.set(j, sj);
                        }

                        // Get the cd again.
                        lcd = (LocalClassDecl) ss.get(i);
                        cd = lcd.decl();
                    }

                    hashAdd(orphans, context.currentClassScope(), cd);

                    ss.remove(i);
                    i--;
                }
                else {
                    s = (Stmt) n.visitChild(s, this);
                    ss.set(i, s);
                }
            }

            return b.statements(ss);
        }

        return null;
    }

    boolean inConstructorCall;

    @Override
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        LocalClassRemover v = (LocalClassRemover) super.enterCall(parent, n);
        if (n instanceof ConstructorCall) {
            if (!inConstructorCall) {
                v = (LocalClassRemover) v.copy();
                v.inConstructorCall = true;
                return v;
            }
        }
        if (n instanceof ClassBody || n instanceof CodeNode) {
            if (v.inConstructorCall) {
                v = (LocalClassRemover) v.copy();
                v.inConstructorCall = false;
                return v;
            }
        }
        return v;
    }

    protected boolean isLocal(Context c, String name) {
        return c.isLocal(name);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        Position pos = n.position();

        if (n instanceof Local && !inConstructorCall) {
            Local l = (Local) n;
            if (!isLocal(context, l.name())) {
                FieldInstance fi = boxLocal(l.localInstance());
                if (fi != null) {
                    Field f =
                            nf.Field(pos,
                                     makeMissingFieldTarget(fi, pos),
                                     nf.Id(pos, fi.name()));
                    f = f.fieldInstance(fi);
                    f = (Field) f.type(fi.type());
                    return f;
                }
            }
        }

        // Convert anonymous classes into member classes
        if (n instanceof New) {
            New neu = (New) n;

            ClassBody body = neu.body();

            if (body == null) return neu;

            // Check if extending a class or an interface.
            TypeNode superClass = neu.objectType();
            List<TypeNode> interfaces = Collections.emptyList();

            Type supertype = neu.objectType().type();
            if (supertype instanceof ClassType) {
                ClassType s = (ClassType) supertype;
                if (s.flags().isInterface()) {
                    superClass = defaultSuperType(pos);
                    interfaces = Collections.singletonList(neu.objectType());
                }
            }

            Id name = nf.Id(pos, UniqueID.newID("Anonymous"));
            ClassDecl cd =
                    nf.ClassDecl(pos,
                                 Flags.PRIVATE,
                                 name,
                                 superClass,
                                 interfaces,
                                 body);

            ParsedClassType type = neu.anonType();
            type.kind(ClassType.MEMBER);
            type.name(cd.name());
            type.setContainer(context.currentClass());
            type.package_(context.package_());

            Flags flags =
                    context.inStaticContext() ? Flags.PRIVATE.Static()
                            : Flags.PRIVATE;
            type.flags(flags);

            cd = cd.type(type);
            cd = cd.flags(flags);

            ConstructorDecl td = addConstructor(cd, neu);

            // Add the CI to the class.
            cd.type().addConstructor(td.constructorInstance());

            {
                // Append the constructor to the body.
                ClassBody b = cd.body();
                List<ClassMember> members = new ArrayList<ClassMember>();
                members.addAll(b.members());
                members.add(td);
                b = b.members(members);
                cd = cd.body(b);
            }

            neu = neu.constructorInstance(td.constructorInstance());
            neu = neu.anonType(null);

            if (!flags.isStatic()) {
                neu = neu.qualifier(nf.This(pos).type(context.currentClass()));
            }

            cd =
                    rewriteLocalClass(cd,
                                      hashGet(newFields,
                                              cd.type(),
                                              Collections.<FieldInstance> emptyList()));
            hashAdd(orphans, context.currentClassScope(), cd);
            neu = neu.objectType(nf.CanonicalTypeNode(pos, type)).body(null);
            neu =
                    (New) rewriteConstructorCalls(neu,
                                                  cd.type(),
                                                  hashGet(newFields,
                                                          cd.type(),
                                                          Collections.<FieldInstance> emptyList()));
            return neu;
        }

        // Add any orphaned declarations created below to the class body
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            List<ClassDecl> o = orphans.get(cd.type());
            if (o == null) return cd;
            ClassBody b = cd.body();
            List<ClassMember> members = new ArrayList<ClassMember>();
            members.addAll(b.members());
            members.addAll(o);
            b = b.members(members);
            return cd.body(b);
        }

        return n;
    }

    /**
     * The type to be extended when translating an anonymous class that
     * implements an interface.
     */
    protected TypeNode defaultSuperType(Position pos) {
        return nf.CanonicalTypeNode(pos, ts.Object());
    }

    ClassDecl rewriteLocalClass(ClassDecl cd, List<FieldInstance> newFields) {
        return InnerClassRemover.addFieldsToClass(cd, newFields, ts, nf, false);
    }

    Node rewriteConstructorCalls(Node s, final ClassType ct,
            final List<FieldInstance> fields) {
        Node r = s.visit(new ConstructorCallRewriter(fields, ct));
        return r;
    }

    // Create a new constructor for an anonymous class.
    ConstructorDecl addConstructor(ClassDecl cd, New neu) {
        // Build the list of formal parameters and list of arguments for the super call.
        List<Formal> formals = new ArrayList<Formal>();
        List<Expr> args = new ArrayList<Expr>();
        List<Type> argTypes = new ArrayList<Type>();
        int i = 1;

        for (Expr e : neu.arguments()) {
            Position pos = e.position();
            Id name = nf.Id(pos, "a" + i);
            i++;
            Formal f =
                    nf.Formal(pos,
                              Flags.FINAL,
                              nf.CanonicalTypeNode(pos, e.type()),
                              name);
            Local l = nf.Local(pos, name);

            LocalInstance li =
                    ts.localInstance(pos, f.flags(), f.declType(), name.id());
            li.setNotConstant();
            f = f.localInstance(li);
            l = l.localInstance(li);
            l = (Local) l.type(li.type());

            formals.add(f);
            args.add(l);
            argTypes.add(li.type());
        }

        Position pos = cd.position();

        // Create the super call.
        ConstructorCall cc = nf.SuperCall(pos, args);
        cc = cc.constructorInstance(neu.constructorInstance());
        cc = cc.qualifier(adjustQualifier(neu.qualifier()));

        List<Stmt> statements = new ArrayList<Stmt>();
        statements.add(cc);

        // Build the list of throw types, copied from the new expression's constructor (now the superclass constructor).
        List<TypeNode> throwTypeNodes = new ArrayList<TypeNode>();
        List<Type> throwTypes = new ArrayList<Type>();
        for (Type t : neu.constructorInstance().throwTypes()) {
            throwTypes.add(t);
            throwTypeNodes.add(nf.CanonicalTypeNode(pos, t));
        }

        // Create the constructor declaration node and the CI.
        ConstructorDecl td =
                nf.ConstructorDecl(pos,
                                   Flags.PRIVATE,
                                   cd.id(),
                                   formals,
                                   throwTypeNodes,
                                   nf.Block(pos, statements));
        ConstructorInstance ci =
                ts.constructorInstance(pos,
                                       cd.type(),
                                       Flags.PRIVATE,
                                       argTypes,
                                       throwTypes);
        td = td.constructorInstance(ci);

        return td;
    }

    private Expr adjustQualifier(Expr e) {
        if (e instanceof Special) {
            Special s = (Special) e;
            if (s.kind() == Special.THIS && s.qualifier() == null) {
                return s.qualifier(nf.CanonicalTypeNode(s.position(), s.type()));
            }
        }
        return e;
    }

    // Add local variables to the argument list until it matches the declaration.
    List<Expr> addArgs(ProcedureCall n, ConstructorInstance nci,
            List<FieldInstance> fields, ClassType curr, ClassType theLocalClass) {
        if (nci == null || fields == null || fields.isEmpty()
                || n.arguments().size() == nci.formalTypes().size())
            return n.arguments();
        List<Expr> args = new ArrayList<Expr>();
        for (FieldInstance fi : fields) {
            if (curr != null
                    && theLocalClass != null
                    && ts.isEnclosed((ClassType) curr.declaration(),
                                     (ClassType) theLocalClass.declaration())) {
                // If in the local class being rewritten, use the boxed local (i.e., field) instead of the local.
                // This could generate a bad constructor call since the field will refer to 'this' before the superclass
                // is initialized, but we'll patch this up later.
                Position pos = fi.position();
                Field f =
                        nf.Field(pos,
                                 makeMissingFieldTarget(fi, pos),
                                 nf.Id(pos, fi.name()));
                f = f.fieldInstance(fi);
                f = (Field) f.type(fi.type());
                args.add(f);
            }
            else {
                LocalInstance li = localOfField.get(fi);
                if (li != null) {
                    Local l =
                            nf.Local(li.position(),
                                     nf.Id(li.position(), li.name()));
                    l = l.localInstance(li);
                    l = (Local) l.type(li.type());
                    args.add(l);
                }
                else {
                    throw new InternalCompilerError("field " + fi
                            + " created with rev map to null", n.position());
                }
            }
        }

        args.addAll(n.arguments());
        assert args.size() == nci.formalTypes().size();
        return args;
    }

    Map<FieldInstance, LocalInstance> localOfField =
            new HashMap<FieldInstance, LocalInstance>();

    // Create a field instance for a local.
    private FieldInstance boxLocal(LocalInstance li) {
        ClassType curr = currLocalClass();

        if (curr == null)
        // not defined in a local class
            return null;

        li = (LocalInstance) li.declaration();

        Pair<LocalInstance, ClassType> key =
                new Pair<LocalInstance, ClassType>(li, curr);
        FieldInstance fi = fieldForLocal.get(key);
        if (fi != null) return fi;

        Position pos = li.position();

        fi =
                ts.fieldInstance(pos,
                                 curr,
                                 li.flags().Private(),
                                 li.type(),
                                 li.name());
        fi.setNotConstant();

        ParsedClassType ct = (ParsedClassType) curr.declaration();
        ct.addField(fi);

        List<FieldInstance> l =
                hashGet(newFields, ct, new ArrayList<FieldInstance>());
        l.add(fi);

        localOfField.put(fi, li);
        fieldForLocal.put(key, fi);
        return fi;
    }

    /** Get the currently enclosing local class, or null. */
    private ClassType currLocalClass() {
        ClassType curr = context.currentClass();
        while (curr != null) {
            if (curr.isLocal() || curr.isAnonymous()) return curr;
            if (curr.isTopLevel()) break;
            curr = curr.outer();
        }
        return null;
    }

    protected Receiver makeMissingFieldTarget(FieldInstance fi, Position pos) {
        Receiver r;

        Context c = context();

        if (fi.flags().isStatic()) {
            r = nf.CanonicalTypeNode(pos, fi.container());
        }
        else {
            // The field is non-static, so we must prepend with
            // "this", but we need to determine if the "this"
            // should be qualified.  Get the enclosing class which
            // brought the field into scope.  This is different
            // from fi.container().  fi.container() returns a super
            // type of the class we want.
            ClassType scope = (ClassType) fi.container();

            if (!ts.equals(scope, c.currentClass())) {
                r =
                        nf.This(pos.startOf(), nf.CanonicalTypeNode(pos, scope))
                          .type(scope);
            }
            else {
                r = nf.This(pos.startOf()).type(scope);
            }
        }

        return r;
    }

    public static <K, V> V hashGet(Map<K, V> map, K k, V v) {
        V x = map.get(k);
        if (x != null) return x;
        map.put(k, v);
        return v;
    }

    public static <K, V> void hashAdd(Map<K, List<V>> map, K k, V v) {
        List<V> l = map.get(k);
        if (l == null) {
            l = new ArrayList<V>();
            map.put(k, l);
        }
        l.add(v);
    }
}
