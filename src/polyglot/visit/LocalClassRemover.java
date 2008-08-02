package polyglot.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeNode;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.LocalClassDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.Receiver;
import polyglot.ast.SourceFile;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.SwitchBlock;
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
import polyglot.types.TypeObject;
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
    public LocalClassRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    Map fieldForLocal = new HashMap();
    Map orphans = new HashMap();
    Map newFields = new HashMap();

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
            List ss = new ArrayList(b.statements());
            for (int i = 0; i < ss.size(); i++) {
                Stmt s = (Stmt) ss.get(i);
                if (s instanceof LocalClassDecl) {
                    s = (Stmt) n.visitChild(s, this);

                    LocalClassDecl lcd = (LocalClassDecl) s;                    
                    ClassDecl cd = lcd.decl();
                    Flags flags = context.inStaticContext() ? Flags.PRIVATE.Static() : Flags.PRIVATE;
                    cd = cd.flags(flags);
                    cd.type().flags(flags);
                    cd.type().kind(ClassType.MEMBER);

                    cd = rewriteLocalClass(cd, (List) hashGet(newFields, cd.type(), Collections.EMPTY_LIST));
                    
                    if (cd != lcd.decl()) {
                        for (int j = i+1; j < ss.size(); j++) {
                            Stmt sj = (Stmt) ss.get(j);
                            sj = (Stmt) rewriteConstructorCalls(sj, cd.type(), (List) hashGet(newFields, cd.type(), Collections.EMPTY_LIST));
                            ss.set(j, sj);
                        }
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
    
    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        LocalClassRemover v = (LocalClassRemover) super.enterCall(parent, n);
        if (n instanceof ConstructorCall) {
            if (! inConstructorCall) {
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

    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        Context c = this.context();

        Position pos = n.position();

        if (n instanceof Local && ! inConstructorCall) {
            Local l = (Local) n;
            if (! context.isLocal(l.name())) {
                FieldInstance fi = boxLocal(l.localInstance());
                Field f = nf.Field(pos, makeMissingFieldTarget(fi, pos), nf.Id(l.position(), fi.name()));
                return f;
            }
        }
        
        // Convert anonymous classes into member classes
        if (n instanceof New) {
            New neu = (New) n;

            if (neu.body() == null)
                return neu;

            // Check if extending a class or an interface.
            TypeNode superClass = neu.objectType();
            List interfaces = Collections.EMPTY_LIST;

            Type supertype = neu.objectType().type();
            if (supertype instanceof ClassType) {
                ClassType s = (ClassType) supertype;
                if (s.flags().isInterface()) {
                    superClass = nf.CanonicalTypeNode(pos, ts.Object());
                    interfaces = Collections.singletonList(neu.objectType());
                }
            }

            ClassBody body = neu.body();

            Id name = nf.Id(pos, UniqueID.newID("Anonymous"));
            ClassDecl cd = nf.ClassDecl(pos, Flags.PRIVATE, name, superClass, interfaces, body);
            
            ParsedClassType type = neu.anonType();
            type.kind(ClassType.MEMBER);
            type.name(cd.name());

            Flags flags = context.inStaticContext() ? Flags.PRIVATE.Static() : Flags.PRIVATE;
            type.flags(flags);

            cd = cd.type(type);
            cd = cd.flags(flags);
            cd = addConstructor(cd, neu);
            
            ConstructorDecl td = (ConstructorDecl) cd.body().members().get(cd.body().members().size()-1);
            ConstructorInstance oldCi = neu.constructorInstance();
            neu = neu.constructorInstance(td.constructorInstance());
            neu = neu.anonType(null);

            cd = rewriteLocalClass(cd, (List) hashGet(newFields, cd.type(), Collections.EMPTY_LIST));
            hashAdd(orphans, context.currentClassScope(), cd);
            neu = neu.objectType(nf.CanonicalTypeNode(pos, type)).body(null);
            neu = (New) rewriteConstructorCalls(neu, cd.type(), (List) hashGet(newFields, cd.type(), Collections.EMPTY_LIST));
            return neu;
        }

        // Add any orphaned declarations created below to the class body
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            List o = (List) orphans.get(cd.type());
            if (o == null)
                return cd;
            ClassBody b = cd.body();
            List members = new ArrayList();
            members.addAll(b.members());
            members.addAll(o);
            b = b.members(members);
            return cd.body(b);
        }
        
        if (n instanceof SourceFile) {
            return n.visit(new InnerClassRemover2(job, ts, nf).context(context));
        }

        return n;
    }
    
    ClassDecl rewriteLocalClass(ClassDecl cd, List newFields) {
        return InnerClassRemover2.addFieldsToClass(cd, newFields, ts, nf, false);
    }

    Node rewriteConstructorCalls(Node s, final ClassType ct, final List fields) {
        s = s.visit(new NodeVisitor() {
            public Node leave(Node old, Node n, NodeVisitor v) {
                if (n instanceof New) {
                    New neu = (New) n;
                    ConstructorInstance ci = neu.constructorInstance();
                    ConstructorInstance nci = (ConstructorInstance) ci.declaration();
                    if (ts.typeEquals(nci.container(), ct))
                        neu = (New) neu.arguments(addArgs(neu, nci, fields));
                    return neu;
                }
                if (n instanceof ConstructorCall) {
                    ConstructorCall cc = (ConstructorCall) n;
                    ConstructorInstance ci = cc.constructorInstance();       
                    ConstructorInstance nci = (ConstructorInstance) ci.declaration();
                    if (ts.typeEquals(nci.container(), ct))
                        cc = (ConstructorCall) cc.arguments(addArgs(cc, nci, fields));
                    return cc;
                }

                return n;
            } 
        });

        return s;
    }
    
    // Create a new constructor for an anonymous class.
    ClassDecl addConstructor(ClassDecl cd, New neu) {
        // Build the list of formal parameters and list of arguments for the super call.
        List formals = new ArrayList();
        List args = new ArrayList();
        List argTypes = new ArrayList();
        int i = 1;
        for (Iterator j = neu.arguments().iterator(); j.hasNext(); ) {
            Expr e = (Expr) j.next();
            Position pos = e.position();
            Id name = nf.Id(pos, "a" + i);
            i++;
            Formal f = nf.Formal(pos, Flags.FINAL, nf.CanonicalTypeNode(pos, e.type()), name);
            Local l = nf.Local(pos, name);

            LocalInstance li = ts.localInstance(pos, f.flags(), f.declType(), name.id());
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
        ConstructorCall cc = nf.ConstructorCall(pos, ConstructorCall.SUPER, args);
        cc = cc.constructorInstance(neu.constructorInstance());
        
        List statements = new ArrayList();
        statements.add(cc);
        
        // Build the list of throw types, copied from the new expression's constructor (now the superclass constructor).
        List throwTypeNodes = new ArrayList();
        List throwTypes = new ArrayList();
        for (Iterator j = neu.constructorInstance().throwTypes().iterator(); j.hasNext(); ) {
            Type t = (Type) j.next();
            throwTypes.add(t);
            throwTypeNodes.add(nf.CanonicalTypeNode(pos, t));
        }
        
        // Create the constructor declaration node and the CI.
        ConstructorDecl td = nf.ConstructorDecl(pos, Flags.PRIVATE, cd.id(), formals, throwTypeNodes, nf.Block(pos, statements));
        ConstructorInstance ci = ts.constructorInstance(pos, context.currentClass(), Flags.PRIVATE, argTypes, throwTypes);
        td = td.constructorInstance(ci);
        
        // Append the constructor to the body.
        ClassBody b = cd.body();
        List members = new ArrayList();
        members.addAll(b.members());
        members.add(td);
        b = b.members(members);
        cd = cd.body(b);
        return cd;
    }

    // Add local variables to the argument list until it matches the declaration.
    List addArgs(ProcedureCall n, ConstructorInstance nci, List fields) {
        if (nci == null || fields == null || fields.isEmpty() || n.arguments().size() == nci.formalTypes().size())
            return n.arguments();
        List args = new ArrayList();
        for (Iterator i = fields.iterator(); i.hasNext(); ) {
            FieldInstance fi = (FieldInstance) i.next();
            LocalInstance li = (LocalInstance) localOfField.get(fi);
            if (li != null) {
                Local l = nf.Local(li.position(), nf.Id(li.position(), li.name()));
                l = l.localInstance(li);
                l = (Local) l.type(li.type());
                args.add(l);
            }
            else {
                throw new InternalCompilerError("field " + fi + " created with rev map to null", n.position());
            }
        }
        args.addAll(n.arguments());
        assert args.size() == nci.formalTypes().size();
        return args;
    }

    Map localOfField = new HashMap();

    // Create a field instance for a local.
    private FieldInstance boxLocal(LocalInstance li) {
        FieldInstance fi = (FieldInstance) fieldForLocal.get(li);
        if (fi != null) return fi;

        Position pos = li.position();

        fi = ts.fieldInstance(pos, context.currentClass(), li.flags().Private(), li.type(), li.name());
        fi.setNotConstant();
        
        ParsedClassType ct = context.currentClassScope();
        ct.addField(fi);

        List l = (List) hashGet(newFields, ct, new ArrayList());
        l.add(fi);
        
        localOfField.put(fi, li);
        fieldForLocal.put(li, fi);
        return fi;
    }
    
    protected Receiver makeMissingFieldTarget(FieldInstance fi, Position pos) throws SemanticException {
        Receiver r;

        Context c = context();

        if (fi.flags().isStatic()) {
            r = nf.CanonicalTypeNode(pos, fi.container());
        } else {
            // The field is non-static, so we must prepend with
            // "this", but we need to determine if the "this"
            // should be qualified.  Get the enclosing class which
            // brought the field into scope.  This is different
            // from fi.container().  fi.container() returns a super
            // type of the class we want.
            ClassType scope = c.findFieldScope(fi.name());

            if (! ts.equals(scope, c.currentClass())) {
                r = nf.This(pos.startOf(), nf.CanonicalTypeNode(pos, scope)).type(scope);
            } else {
                r = nf.This(pos.startOf()).type(scope);
            }
        }

        return r;
    }
    
    public static Object hashGet(Map map, Object k, Object v) {
        Object x = map.get(k);
        if (x != null)
            return x;
        map.put(k, v);
        return v;
    }
    
    public static void hashAdd(Map map, Object k, Object v) {
        List l = (List) map.get(k);
        if (l == null) {
            l = new ArrayList();
            map.put(k, l);
        }
        l.add(v);
    }
}
