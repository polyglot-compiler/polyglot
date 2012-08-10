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

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Local;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.SourceFile;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.frontend.Job;
import polyglot.main.Report;
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
import polyglot.util.Position;

// TODO:
//Convert closures to anon
//Add frame classes around anon and local
//now all classes access only final locals
//Convert local and anon to member
//Dup inner member to static
//Remove inner member

public class InnerClassRemover extends ContextVisitor {
    // Name of field used to carry a pointer to the enclosing class.
    private static final String OUTER_FIELD_NAME = "out$";

    public InnerClassRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    Map<ParsedClassType, FieldInstance> outerFieldInstance =
            new HashMap<ParsedClassType, FieldInstance>();

    /** Get a reference to the enclosing instance of the current class that is of type containerClass */
    Expr getContainer(Position pos, Expr this_, ClassType currentClass,
            ClassType containerClass) {
        if (containerClass == currentClass) {
            return this_;
        }
        FieldInstance fi = boxThis(currentClass, currentClass.outer());
        Field f = nf.Field(pos, this_, nf.Id(pos, OUTER_FIELD_NAME));
        f = f.fieldInstance(fi);
        f = (Field) f.type(fi.type());
        f = f.targetImplicit(false);
        return getContainer(pos, f, currentClass.outer(), containerClass);
    }

    protected ContextVisitor localClassRemover() {
        LocalClassRemover lcv = new LocalClassRemover(job, ts, nf);
        return lcv;
    }

    @Override
    public Node override(Node parent, Node n) {
        if (n instanceof SourceFile) {
            ContextVisitor lcv = localClassRemover();
            lcv = (ContextVisitor) lcv.begin();
            lcv = lcv.context(context);

            if (Report.should_report("innerremover", 1)) {
                System.out.println(">>> output ----------------------");
                n.prettyPrint(System.out);
                System.out.println("<<< output ----------------------");
            }

            n = n.visit(lcv);

            if (Report.should_report("innerremover", 1)) {
                System.out.println(">>> locals removed ----------------------");
                n.prettyPrint(System.out);
                System.out.println("<<< locals removed ----------------------");
            }

            n = this.visitEdgeNoOverride(parent, n);

            if (Report.should_report("innerremover", 1)) {
                System.out.println(">>> inners removed ----------------------");
                n.prettyPrint(System.out);
                System.out.println("<<< inners removed ----------------------");
            }

            return n;
        }
        else {
            return null;
        }
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        Context context = this.context();

        Position pos = n.position();

        if (n instanceof Special) {
            Special s = (Special) n;
            if (s.qualifier() == null) return s;
            assert s.qualifier().type().toClass() != null;
            if (s.qualifier().type().toClass().declaration() == context.currentClassScope())
                return s;
            Node ret =
                    getContainer(pos,
                                 nf.This(pos).type(context.currentClass()),
                                 context.currentClass(),
                                 s.qualifier().type().toClass());
            return ret;
        }

        // Add the qualifier as an argument to constructor calls.
        if (n instanceof New) {
            New neu = (New) n;

            Expr q = neu.qualifier();

            if (q != null) {
                neu = neu.qualifier(null);
                ConstructorInstance ci = neu.constructorInstance();
                // Fix the ci if a copy; otherwise, let the ci be modified at the declaration node.
                if (ci != ci.declaration()) {
                    List<Type> args = new ArrayList<Type>();
                    args.add(ci.container());
                    args.addAll(ci.formalTypes());
                    ci = ci.formalTypes(args);
                    neu = neu.constructorInstance(ci);
                }

                List<Expr> args = new ArrayList<Expr>();
                args.add(q);
                args.addAll(neu.arguments());
                neu = (New) neu.arguments(args);
            }

            return neu;
        }

        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;

            if (cc.kind() != ConstructorCall.SUPER) {
                return cc;
            }

            ConstructorInstance ci = cc.constructorInstance();

            // NOTE: we require that a constructor call to a non-static member have a qualifier.
            // We can't check for this now, though, since the type information may already have been
            // rewritten.

//            ClassType ct = ci.container().toClass();

//            // Add a qualifier to non-static member class super() calls if not present.
//            if (ct.isMember() && ! ct.flags().isStatic()) {
//                if (cc.qualifier() == null) {
//                    cc = cc.qualifier(nf.This(pos).type(context.currentClass()));
//                }
//            }

            if (cc.qualifier() == null) {
                return cc;
            }

            Expr q = cc.qualifier();
            cc = cc.qualifier(null);

            ConstructorInstance cidecl = (ConstructorInstance) ci.declaration();
            boolean fixCI =
                    cc.arguments().size() + 1 != ci.formalTypes().size();

//            if (q == null) {
//                if (ct.isMember() && ! ct.flags().isStatic()) {
//                    q = getContainer(pos, nf.Special(pos, Special.THIS).type(context.currentClass()), context.currentClass(), ct);
//                }
//                else if (ct.isMember()) {
//                    // might have already been rewritten to static.  If so, the CI should have been rewritten also.
//                    if (((ConstructorInstance) ci.declaration()).formalTypes().size() >= cc.arguments().size()) {
//                        q = nf.Special(pos, Special.THIS).type(context.currentClass());
//                    }
//                }
//            }

            // Fix the ci if a copy; otherwise, let the ci be modified at the declaration node.
            if (ci != cidecl && fixCI) {
                List<Type> args = new ArrayList<Type>();
                args.add(ci.container());
                args.addAll(ci.formalTypes());
                ci = ci.formalTypes(args);
                cc = cc.constructorInstance(ci);
            }

            List<Expr> args = new ArrayList<Expr>();
            args.add(q);
            args.addAll(cc.arguments());
            cc = (ConstructorCall) cc.arguments(args);

            return cc;
        }

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;

            if (cd.type().isMember() && !cd.type().flags().isStatic()) {
                cd.type().flags(cd.type().flags().Static());
                cd = cd.flags(cd.type().flags());

                // Add a field for the enclosing class.
                ClassType ct = (ClassType) cd.type().container();
                FieldInstance fi = boxThis(cd.type(), ct);

                cd =
                        addFieldsToClass(cd,
                                         Collections.singletonList(fi),
                                         ts,
                                         nf,
                                         true);

                cd = fixQualifiers(cd);
            }

            return cd;
        }

        if (n instanceof Field) {
            Field f = (Field) n;
            if (f.isTargetImplicit() && f.target() instanceof Field) {
                // we translated the target from "this" to a field
                f = f.targetImplicit(false);
            }
            return f;
        }

        if (n instanceof Call) {
            Call c = (Call) n;
            if (c.isTargetImplicit() && c.target() instanceof Field) {
                // we translated the target, from "this" to a field
                c = c.targetImplicit(false);
            }
            return c;
        }

        return n;
    }

    public ClassDecl fixQualifiers(ClassDecl cd) {
        return (ClassDecl) cd.visitChildren(new NodeVisitor() {
            LocalInstance li;

            @Override
            public Node override(Node parent, Node n) {
                if (n instanceof ClassBody) {
                    return null;
                }

                if (n instanceof ConstructorDecl) {
                    return null;
                }

                if (parent instanceof ConstructorDecl && n instanceof Formal) {
                    Formal f = (Formal) n;
                    LocalInstance li = f.localInstance();
                    if (li.name().equals(OUTER_FIELD_NAME)) {
                        this.li = li;
                    }
                    return n;
                }

                if (parent instanceof ConstructorDecl && n instanceof Block) {
                    return null;
                }

                if (parent instanceof Block && n instanceof ConstructorCall) {
                    return null;
                }

                if (parent instanceof ConstructorCall) {
                    return null;
                }

                return n;
//
//                if (n instanceof ClassMember) {
//                    this.li = null;
//                    return n;
//                }
//
//                return null;
            }

            @Override
            public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
                if (parent instanceof ConstructorCall && li != null
                        && n instanceof Expr) {
                    return fixQualifier((Expr) n, li);
                }
                return n;
            }
        });
    }

    public Expr fixQualifier(Expr e, final LocalInstance li) {
        return (Expr) e.visit(new NodeVisitor() {
            @Override
            public Node leave(Node old, Node n, NodeVisitor v) {
                if (n instanceof Field) {
                    Field f = (Field) n;
                    if (f.target() instanceof Special) {
                        Special s = (Special) f.target();
                        if (s.kind() == Special.THIS
                                && f.name().equals(OUTER_FIELD_NAME)) {
                            Local l = nf.Local(n.position(), f.id());
                            l = l.localInstance(li);
                            l = (Local) l.type(li.type());
                            return l;
                        }
                    }
                }
                return n;
            }
        });
    }

    public static ClassDecl addFieldsToClass(ClassDecl cd,
            List<FieldInstance> newFields, TypeSystem ts, NodeFactory nf,
            boolean rewriteMembers) {
        if (newFields.isEmpty()) {
            return cd;
        }

        ClassBody b = cd.body();

        // Add the new fields to the class.
        List<ClassMember> newMembers = new ArrayList<ClassMember>();
        for (FieldInstance fi : newFields) {
            Position pos = fi.position();
            FieldDecl fd =
                    nf.FieldDecl(pos,
                                 fi.flags(),
                                 nf.CanonicalTypeNode(pos, fi.type()),
                                 nf.Id(pos, fi.name()));
            fd = fd.fieldInstance(fi);
            newMembers.add(fd);
        }

        for (ClassMember m : b.members()) {
            if (m instanceof ConstructorDecl) {
                ConstructorDecl td = (ConstructorDecl) m;

                // Create a list of formals to add to the constructor.
                List<Formal> formals = new ArrayList<Formal>();
                List<LocalInstance> locals = new ArrayList<LocalInstance>();

                for (FieldInstance fi : newFields) {
                    Position pos = fi.position();
                    LocalInstance li =
                            ts.localInstance(pos,
                                             Flags.FINAL,
                                             fi.type(),
                                             fi.name());
                    li.setNotConstant();
                    Formal formal =
                            nf.Formal(pos,
                                      li.flags(),
                                      nf.CanonicalTypeNode(pos, li.type()),
                                      nf.Id(pos, li.name()));
                    formal = formal.localInstance(li);
                    formals.add(formal);
                    locals.add(li);
                }

                List<Formal> newFormals = new ArrayList<Formal>();
                newFormals.addAll(formals);
                newFormals.addAll(td.formals());
                td = td.formals(newFormals);

                // Create a list of field assignments.
                List<Stmt> statements = new ArrayList<Stmt>();

                for (int j = 0; j < newFields.size(); j++) {
                    FieldInstance fi = newFields.get(j);
                    LocalInstance li = formals.get(j).localInstance();

                    Position pos = fi.position();

                    Field f =
                            nf.Field(pos,
                                     nf.This(pos).type(fi.container()),
                                     nf.Id(pos, fi.name()));
                    f = (Field) f.type(fi.type());
                    f = f.fieldInstance(fi);
                    f = f.targetImplicit(false);

                    Local l = nf.Local(pos, nf.Id(pos, li.name()));
                    l = (Local) l.type(li.type());
                    l = l.localInstance(li);

                    Assign a = nf.FieldAssign(pos, f, Assign.ASSIGN, l);
                    a = (Assign) a.type(li.type());

                    Eval e = nf.Eval(pos, a);
                    statements.add(e);
                }

                // Add the assignments to the constructor body after the super call.
                // Or, add pass the locals to another constructor if a this call.
                Block block = td.body();
                if (block.statements().size() > 0) {
                    Stmt s0 = block.statements().get(0);
                    if (s0 instanceof ConstructorCall) {
                        ConstructorCall cc = (ConstructorCall) s0;
                        ConstructorInstance ci = cc.constructorInstance();
                        if (cc.kind() == ConstructorCall.THIS) {
                            // Not a super call.  Pass the locals as arguments.
                            List<Expr> arguments = new ArrayList<Expr>();
                            for (Stmt si : statements) {
                                Eval e = (Eval) si;
                                Assign a = (Assign) e.expr();
                                arguments.add(a.right());
                            }

                            // Modify the CI if it is a copy of the declaration CI.
                            // If not a copy, it will get modified at the declaration.
                            if (ci != ci.declaration()) {
                                List<Type> newFormalTypes =
                                        new ArrayList<Type>();
                                for (int j = 0; j < newFields.size(); j++) {
                                    FieldInstance fi = newFields.get(j);
                                    newFormalTypes.add(fi.type());
                                }
                                newFormalTypes.addAll(ci.formalTypes());
                                ci.setFormalTypes(newFormalTypes);
                            }

                            arguments.addAll(cc.arguments());
                            cc = (ConstructorCall) cc.arguments(arguments);
                        }
                        else {
                            // A super call.  Don't rewrite it here; the visitor will handle it elsewhere.
                        }

                        // prepend the super call
                        statements.add(0, cc);
                    }

                    statements.addAll(block.statements()
                                           .subList(1,
                                                    block.statements().size()));
                }
                else {
                    statements.addAll(block.statements());
                }

                block = block.statements(statements);
                td = (ConstructorDecl) td.body(block);

                newMembers.add(td);

                List<Type> newFormalTypes = new ArrayList<Type>();
                for (Formal f : newFormals) {
                    newFormalTypes.add(f.declType());
                }

                ConstructorInstance ci = td.constructorInstance();
                assert ci.declaration() == ci;

                ci.setFormalTypes(newFormalTypes);
            }
            else {
                newMembers.add(m);
            }
        }

        b = b.members(newMembers);
        return cd.body(b);
    }

    // Add local variables to the argument list until it matches the declaration.
    List<Expr> addArgs(ProcedureCall n, ConstructorInstance nci, Expr q) {
        if (nci == null || q == null) return n.arguments();
        List<Expr> args = new ArrayList<Expr>();
        args.add(q);
        args.addAll(n.arguments());
        assert args.size() == nci.formalTypes().size();
        return args;
    }

    // Create a field instance for a qualified this.
    private FieldInstance boxThis(ClassType currClass, ClassType outerClass) {
        FieldInstance fi = outerFieldInstance.get(currClass);
        if (fi != null) return fi;

        Position pos = outerClass.position();

        fi =
                ts.fieldInstance(pos,
                                 currClass,
                                 Flags.FINAL.Private(),
                                 outerClass,
                                 OUTER_FIELD_NAME);
        fi.setNotConstant();

        ParsedClassType currDecl = (ParsedClassType) currClass.declaration();
        currDecl.addField(fi);

        outerFieldInstance.put(currDecl, fi);
        return fi;
    }

    public static <K, V> V hashGet(Map<K, V> map, K k, V v) {
        return LocalClassRemover.hashGet(map, k, v);
    }
}
