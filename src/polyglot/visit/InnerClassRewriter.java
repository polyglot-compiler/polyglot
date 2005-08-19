/*
 * Created on May 18, 2005
 */
package polyglot.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.Position;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassRewriter extends InnerClassAbstractRemover
{
    public InnerClassRewriter(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected String namePrefix() {
        return "jl$";
    }

    FieldInstance localToField(ParsedClassType ct, ClassType outer, int i) {
        FieldInstance fi = ts.fieldInstance(Position.compilerGenerated(), ct,
                                            Flags.FINAL.Protected(), outer,
                                            namePrefix() + outer.fullName().replace('.', '$'));
        return fi;
    }
    
    FieldDecl createFieldDecl(FieldInstance fi) {
        FieldDecl fd = nf.FieldDecl(fi.position(), fi.flags(),
                                    nf.CanonicalTypeNode(fi.position(), fi.type()), fi.name());
        fd = fd.fieldInstance(fi);
        return fd;
    }
    
    class ClassBodyTranslator extends NodeVisitor { 
        ParsedClassType ct;
        Map fieldMap;
        Context outerContext;
        
        ClassBodyTranslator(ParsedClassType ct, Map fieldMap, Context context) {
            this.ct = ct;
            this.fieldMap = fieldMap;
            this.outerContext = context;
        }
        
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Special) {
                Special s = (Special) n;
                if (s.qualifier() != null) {
                    FieldInstance fi = (FieldInstance) fieldMap.get(s.qualifier().type());
                    if (fi != null) {
                        Special this_ = nf.Special(s.position(), Special.THIS);
                        this_ = (Special) this_.type(ct);
                        Field f = nf.Field(s.position(), this_, fi.name());
                        f = f.fieldInstance(fi);
                        f = (Field) f.type(fi.type());
                        n = f;
                    }
                }
            }
            if (n instanceof ConstructorDecl) {
                ConstructorDecl ctd = (ConstructorDecl) n;
                ClassType ct2 = (ClassType)
                    ctd.constructorInstance().container();
                if (ct2.equals(ct)) {
                    ctd = translateConstructorDecl(ct, ctd, fieldMap);
                }
                n = ctd;
            }
            return super.leave(old, n, v);
        }
    }
    
    void addEnvToCI(ConstructorInstance ci, List env) {
        List formals = new ArrayList(ci.formalTypes());
        formals.addAll(envAsFormalTypes(env));
        ci.setFormalTypes(formals);
    }

    ConstructorDecl translateConstructorDecl(ParsedClassType ct, ConstructorDecl cd, Map m) {
        List env = env(ct, true);

        addEnvToCI(cd.constructorInstance(), env);

        cd = cd.name(ct.name());

        // Add the new formals.
        List newFormals = new ArrayList();
        newFormals.addAll(cd.formals());
        newFormals.addAll(envAsFormals(env));
        cd = cd.formals(newFormals);

        if (cd.body() == null) {
            // Must be a native constructor; just let the programmer
            // deal with it.
            return cd;
        }

        List oldStmts = cd.body().statements();
        List newStmts = new ArrayList();

        // Check if this constructor invokes another with a this call.
        // If so, don't initialize the fields, but do pass the environment
        // to the other constructor.
        ConstructorCall cc = null;

        if (oldStmts.size() >= 1) {
            Stmt s = (Stmt) oldStmts.get(0);
            if (s instanceof ConstructorCall) {
                cc = (ConstructorCall) s;
            }
        }
        
        if (cc != null) {
            newStmts.add(cc);
        }
        
        // Initialize the new fields.
        if (cc == null || cc.kind() == ConstructorCall.SUPER) {
            for (Iterator i = envAsFormals(env).iterator(); i.hasNext(); ) {
                Formal f = (Formal) i.next();
                LocalInstance li = f.localInstance();
                FieldInstance fi = (FieldInstance) m.get(li.type());
                
                if (fi == null) {
                    // Not a enclosing class of ct, so must be an enclosing class
                    // of a supertype.  The supertype will initialize.
                    continue;
                }
                
                Special this_ = nf.Special(Position.compilerGenerated(), Special.THIS);
                this_ = (Special) this_.type(ct);
                
                Field target = nf.Field(Position.compilerGenerated(), this_, fi.name());
                target = target.fieldInstance(fi);
                target = (Field) target.type(fi.type());
                
                Local source = nf.Local(Position.compilerGenerated(), li.name());
                source = source.localInstance(li);
                source = (Local) source.type(li.type());
                
                FieldAssign assign = nf.FieldAssign(Position.compilerGenerated(), target, Assign.ASSIGN, source);
                assign = (FieldAssign) assign.type(target.type());
                
                newStmts.add(nf.Eval(Position.compilerGenerated(), assign));
            }
        }

        if (cc != null) {
            for (int i = 1; i < oldStmts.size(); i++) {
                newStmts.add(oldStmts.get(i));
            }
        }
        else {
            newStmts.addAll(oldStmts);
        }

        Block b = cd.body().statements(newStmts);
        cd = (ConstructorDecl) cd.body(b);
        return cd;
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            
            ParsedClassType ct = cd.type();
         
            List env = env(ct, true);
            
            if (! env.isEmpty()) {
                // Translate the class body if any supertype (including ct itself)
                // is an inner class.
                Context innerContext = cd.enterChildScope(cd.body(), context);
                cd = cd.body(translateClassBody(ct, cd.body(), innerContext));
            }
            
            n = cd;
        }
        
        n = super.leaveCall(old, n, v);
        return n;
    }

    protected ClassBody translateClassBody(ParsedClassType ct, ClassBody body, Context context) {
        List members = new ArrayList();

        List env = env(ct, false);

        Map fieldMap = new HashMap();

        int index = 1;
        for (Iterator i = env.iterator(); i.hasNext(); index++) {
            ClassType outer = (ClassType) i.next();
            FieldInstance fi = localToField(ct, outer, index);
            fieldMap.put(outer, fi);
            ct.addField(fi);
            members.add(createFieldDecl(fi));
        }

        // Now add existing members, making sure constructors appear
        // first.  The constructors may have field
        // initializers which must be run before other initializers.
        List ctors = new ArrayList();
        List others = new ArrayList();
        for (Iterator i = body.members().iterator(); i.hasNext(); ) {
            ClassMember cm = (ClassMember) i.next();
            if (cm instanceof ConstructorDecl) {
                ctors.add(cm);
            }
            else {
                others.add(cm);
            }
        }
        
        members.addAll(ctors);
        members.addAll(others);

        body = body.members(members);

        // Rewrite the class body.
        ClassBodyTranslator v = new ClassBodyTranslator(ct, fieldMap, context);
        v = (ClassBodyTranslator) v.begin();
        body = (ClassBody) body.visit(v);

        return body;
    }
}
