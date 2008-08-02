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
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.SwitchBlock;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Declaration;
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

public class InnerClassRemover2 extends ContextVisitor {
    // Name of field used to carry a pointer to the enclosing class.
    private static final String OUTER_FIELD_NAME = "outer$this";

    public InnerClassRemover2(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    Map outerFieldInstance = new HashMap();
    
    Expr getContainer(Position pos, Expr this_, ClassType curr, ClassType ct) {
        if (ct == curr)
            return this_;
        FieldInstance fi = boxThis(curr, ct);
        Field f = nf.Field(pos, this_, nf.Id(pos, OUTER_FIELD_NAME));
        return getContainer(pos, f, curr.outer(), ct);
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        Context c = this.context();

        Position pos = n.position();
        
        if (n instanceof Special) {
            Special s = (Special) n;
            if (s.qualifier() == null)
                return s;
            assert s.qualifier().type().toClass() != null;
            if (s.qualifier().type().toClass().declaration() == context.currentClassScope())
                return s;
            return getContainer(pos, nf.This(pos).type(context.currentClass()), context.currentClass(), s.qualifier().type().toClass());
        }
        
        // Add the qualifier as an argument to constructor calls.
        if (n instanceof New) {
            New neu = (New) n;

            if (neu.qualifier() != null) {
                Expr q = neu.qualifier();
                neu = neu.qualifier(null);
                ConstructorInstance ci = neu.constructorInstance();
                // Fix the ci if a copy; otherwise, let the ci be modified at the declaration node.
                if (ci != ci.declaration()) {
                    List args = new ArrayList();
                    args.add(ci.container());
                    args.addAll(ci.formalTypes());
                    ci = ci.formalTypes(args);
                    neu = neu.constructorInstance(ci);
                }
                List args = new ArrayList();
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
            
            ClassType ct = ci.container().toClass();
            
            Expr q = cc.qualifier();
            cc = cc.qualifier(null);
            boolean fixCI = ((ConstructorInstance) ci.declaration()).formalTypes().size() != ci.formalTypes().size();
            
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

            if (q != null) {
                // Fix the ci if a copy; otherwise, let the ci be modified at the declaration node.
                if (ci != ci.declaration() && fixCI) {
                    List args = new ArrayList();
                    args.add(ci.container());
                    args.addAll(ci.formalTypes());
                    ci = ci.formalTypes(args);
                    cc = cc.constructorInstance(ci);
                }
                List args = new ArrayList();
                args.add(q);
                args.addAll(cc.arguments());
                cc = (ConstructorCall) cc.arguments(args);
            }

            return cc;
        }

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            
            if (cd.type().isMember() && ! cd.type().flags().isStatic()) {
                cd.type().flags(cd.type().flags().Static());
                cd = cd.flags(cd.type().flags());

                // Add a field for the enclosing class.
                ClassType ct = (ClassType) cd.type().container();
                FieldInstance fi = boxThis(cd.type(), ct);
                
                cd = addFieldsToClass(cd, Collections.singletonList(fi), ts, nf, true);
                
                return cd;
            }
            
            return cd;
        }

        return n;
    }
    
    public static ClassDecl addFieldsToClass(ClassDecl cd, List newFields, TypeSystem ts, NodeFactory nf, boolean rewriteMembers) {
        if (newFields.isEmpty()) {
            return cd;
        }

        ClassBody b = cd.body();
        
        // Add the new fields to the class.
        List newMembers = new ArrayList();
        for (Iterator i = newFields.iterator(); i.hasNext(); ) {
            FieldInstance fi = (FieldInstance) i.next();
            Position pos = fi.position();
            FieldDecl fd = nf.FieldDecl(pos, fi.flags(), nf.CanonicalTypeNode(pos, fi.type()), nf.Id(pos, fi.name()));
            fd = fd.fieldInstance(fi);
            newMembers.add(fd);
        }

        for (Iterator i = b.members().iterator(); i.hasNext(); ) {
            ClassMember m = (ClassMember) i.next();
            if (m instanceof ConstructorDecl) {
                ConstructorDecl td = (ConstructorDecl) m;

                // Create a list of formals to add to the constructor.
                List formals = new ArrayList();
                List locals = new ArrayList();
                
                for (Iterator j = newFields.iterator(); j.hasNext(); ) {
                    FieldInstance fi = (FieldInstance) j.next();
                    Position pos = fi.position();
                    LocalInstance li = ts.localInstance(pos, Flags.FINAL, fi.type(), fi.name());
                    li.setNotConstant();
                    Formal formal = nf.Formal(pos, li.flags(), nf.CanonicalTypeNode(pos, li.type()), nf.Id(pos, li.name()));
                    formal = formal.localInstance(li);
                    formals.add(formal);
                    locals.add(li);
                }
                
                List newFormals = new ArrayList();
                newFormals.addAll(formals);
                newFormals.addAll(td.formals());
                td = td.formals(newFormals);

                // Create a list of field assignments.
                List statements = new ArrayList();

                for (int j = 0; j < newFields.size(); j++) {
                    FieldInstance fi = (FieldInstance) newFields.get(j);
                    LocalInstance li = ((Formal) formals.get(j)).localInstance();

                    Position pos = fi.position();

                    Field f = nf.Field(pos, nf.This(pos).type(fi.container()), nf.Id(pos, fi.name()));
                    f = (Field) f.type(fi.type());
                    f = (Field) f.fieldInstance(fi);
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
                    Stmt s0 = (Stmt) block.statements().get(0);
                    if (s0 instanceof ConstructorCall) {
                        ConstructorCall cc = (ConstructorCall) s0;
                        ConstructorInstance ci = cc.constructorInstance();
                        if (cc.kind() == ConstructorCall.THIS) {
                            // Not a super call.  Pass the locals as arguments.
                            List arguments = new ArrayList();
                            for (Iterator j = statements.iterator(); j.hasNext(); ) {
                                Stmt si = (Stmt) j.next();
                                Eval e = (Eval) si;
                                Assign a = (Assign) e.expr();
                                arguments.add(a.right());
                            }
                            
                            // Modify the CI if it is a copy of the declaration CI.
                            // If not a copy, it will get modified at the declaration.
                            if (ci != ci.declaration()) {
                                List newFormalTypes = new ArrayList();
                                for (int j = 0; j < newFields.size(); j++) {
                                    FieldInstance fi = (FieldInstance) newFields.get(j);
                                    newFormalTypes.add(fi.type());
                                }
                                newFormalTypes.addAll(ci.formalTypes());
                                ci.setFormalTypes(newFormalTypes);
                            }
                            
                            arguments.addAll(cc.arguments());
                            cc = (ConstructorCall) cc.arguments(arguments);
                            statements.add(0, cc);
                        }
                        else if (rewriteMembers) {
                            // A super call.  Don't rewrite it.
                            ClassType superclass = ci.container().toClass();
                            ConstructorInstance superctor = (ConstructorInstance) ci.declaration();
                            if (superclass.isMember()) {
                                boolean rewrite = false;
                                if (superctor.formalTypes().size() != cc.arguments().size()) {
                                    // constructor declaration was already rewritten
                                    rewrite = true;
                                }
                                else if (! superclass.flags().isStatic()) {
                                    // constructor declaration was not rewritten, but will be.
                                    rewrite = true;
                                }
                                if (rewrite) {
                                    LocalInstance li = ((Formal) formals.get(0)).localInstance();
                                    Position pos = cc.position();
                                    Local l = nf.Local(pos, nf.Id(pos, li.name()));
                                    l = (Local) l.type(li.type());
                                    l = l.localInstance(li);
                                    Expr q = l;
                                    
                                    if (ci != ci.declaration()) {
                                        List args = new ArrayList();
                                        args.add(ci.container());
                                        args.addAll(ci.formalTypes());
                                        ci = ci.formalTypes(args);
                                        cc = cc.constructorInstance(ci);
                                    }
                                    List args = new ArrayList();
                                    args.add(q);
                                    args.addAll(cc.arguments());
                                    cc = (ConstructorCall) cc.arguments(args);
                                }
                            }
                            statements.add(0, cc);
                        }
                        else {
                            statements.add(0, cc);
                        }
                    }
                    
                    statements.addAll(block.statements().subList(1, block.statements().size()));
                }
                else {
                    statements.addAll(block.statements());
                }

                block = block.statements(statements);
                td = (ConstructorDecl) td.body(block);

                newMembers.add(td);

                List newFormalTypes = new ArrayList();
                for (Iterator j = newFormals.iterator(); j.hasNext(); ) {
                    Formal f = (Formal) j.next();
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

    // Add local variables to the argument list until it matches the declaration.
    List addArgs(ProcedureCall n, ConstructorInstance nci, List fields) {
        if (nci == null || fields == null || fields.isEmpty() || n.arguments().size() == nci.formalTypes().size())
            return n.arguments();
        List args = new ArrayList();
        for (Iterator i = fields.iterator(); i.hasNext(); ) {
            FieldInstance fi = (FieldInstance) i.next();
            args.add(nf.This(fi.position()).type(fi.container()));
        }
        args.addAll(n.arguments());
        assert args.size() == nci.formalTypes().size();
        return args;
    }

    // Create a field instance for a qualified this.
    private FieldInstance boxThis(ClassType curr, ClassType t) {
        FieldInstance fi = (FieldInstance) outerFieldInstance.get(curr);
        if (fi != null) return fi;
        
        Position pos = t.position();
        
        fi = ts.fieldInstance(pos, curr, Flags.FINAL.Private(), t, OUTER_FIELD_NAME);
        fi.setNotConstant();
        
        ParsedClassType currDecl = (ParsedClassType) curr.declaration();
        currDecl.addField(fi);
        
        outerFieldInstance.put(currDecl, fi);
        return fi;
    }

    public static Object hashGet(Map map, Object k, Object v) {
        return LocalClassRemover.hashGet(map, k, v);
    }
}
