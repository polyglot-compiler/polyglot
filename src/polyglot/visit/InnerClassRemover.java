/*
 * Created on May 18, 2005
 */
package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.util.*;

import java.util.*;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassRemover extends ContextVisitor
{
    Map envMap;
    int[] count;
    
    public InnerClassRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        envMap = new HashMap();
        count = new int[1];
    }

    protected String newFieldName(String name) {
        return namePrefix() + name;
    }

    /*
     * Generate the new name for a field that comes from a final local variable.
     */
    protected String namePrefix() {
        return "jl$";
    }

    class EnvCollector extends ContextVisitor {
        List env;
        Context c;

        EnvCollector(Job job, TypeSystem ts, NodeFactory nf, Context context) {
            super(job, ts, nf);
            env = new ArrayList();
            c = context;
        }

        List env() {
            return env;
        }

        public NodeVisitor begin() {
            ContextVisitor v = (ContextVisitor) super.begin();
            v.context = c;
            return v;
        }

        public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
            if (n instanceof Special) {
                Special s = (Special) n;
                if (s.qualifier() != null && ! s.qualifier().type().equals(context.currentClass())) {
                    ClassType t = (ClassType) s.qualifier().type();
                    LocalInstance li = ts.localInstance(s.position(), Flags.FINAL, t, namePrefix() + t.name());
                    env.add(li);
                }
            }

            return super.leaveCall(old, n, v);
        }
    }
     
    List computeClosure(ClassBody body, Context context) {
        EnvCollector v = new EnvCollector(job, ts, nf, context);
        v = (EnvCollector) v.begin();
        body.visit(v);
        v.finish();
        return v.env();
    }
    
    FieldInstance localToField(ParsedClassType ct, LocalInstance li) {
        FieldInstance fi = ts.fieldInstance(li.position(), ct, li.flags().Private(), li.type(), li.name());
        return fi;
    }
    
    FieldDecl createFieldDecl(FieldInstance fi) {
        FieldDecl fd = nf.FieldDecl(fi.position(), fi.flags(), nf.CanonicalTypeNode(fi.position(), fi.type()), fi.name());
        fd = fd.fieldInstance(fi);
        return fd;
    }
    
    class ClassBodyTranslator extends NodeVisitor { 
        ParsedClassType ct;
        Map m;
        Context context;
        
        ClassBodyTranslator(ParsedClassType ct, Map m, Context context) {
            this.ct = ct;
            this.m = m;
            this.context = context;
        }
        
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Special) {
                Special s = (Special) n;
                if (s.qualifier() != null && ! s.qualifier().type().equals(context.currentClass())) {
                    FieldInstance fi = (FieldInstance) m.get(s.qualifier().type());
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
                    ctd = translateConstructorDecl(ct, ctd, m);
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
        List env = env(ct);

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

        if (cc != null && cc.kind() == ConstructorCall.THIS) {
            List newArgs = new ArrayList();
            newArgs.addAll(cc.arguments());
            newArgs.addAll(envAsActuals(env));

            ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
            newStmts.add(newCC);
        }
        else if (cc != null) {
            // adjust the super call arguments
            List newArgs = new ArrayList();
            newArgs.addAll(cc.arguments());
            newArgs.addAll(envAsActuals(env((ClassType) ct.superType())));

            ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
            newStmts.add(newCC);
        }

        // Initialize the new fields.
        if (cc == null || cc.kind() == ConstructorCall.SUPER) {
            for (Iterator i = env.iterator(); i.hasNext(); ) {
                LocalInstance li = (LocalInstance) i.next();
                FieldInstance fi = (FieldInstance) m.get(li.type());
                
                Special this_ = nf.Special(cd.position(), Special.THIS);
                this_ = (Special) this_.type(ct);
                
                Field target = nf.Field(cd.position(), this_, fi.name());
                target = target.fieldInstance(fi);
                target = (Field) target.type(fi.type());
                
                Local source = nf.Local(cd.position(), li.name());
                source = source.localInstance(li);
                source = (Local) source.type(li.type());
                
                FieldAssign assign = nf.FieldAssign(cd.position(), target, Assign.ASSIGN, source);
                assign = (FieldAssign) assign.type(target.type());
                
                newStmts.add(nf.Eval(cd.position(), assign));
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
    
    List env(ClassType ct) {
        if (ct != null) {
            List superEnv = env((ClassType) ct.superType());
            List env = (List) envMap.get(ct);
            if (env == null || env.isEmpty()) {
                return superEnv;
            }
            if (superEnv.isEmpty()) {
                return env;
            }
            List l = new ArrayList();
            l.addAll(superEnv);
            l.removeAll(env);
            l.addAll(env);
            return l;
        }
        return Collections.EMPTY_LIST;
    }
    
    List envAsFormalTypes(List env) {
        List formals = new ArrayList();
        int arg = 1;
        for (Iterator i = env.iterator(); i.hasNext(); ) {
            LocalInstance li = (LocalInstance) i.next();
            formals.add(li.type());
        }
        return formals;
     }
    
    List envAsFormals(List env) {
        List formals = new ArrayList();
        int arg = 1;
        for (Iterator i = env.iterator(); i.hasNext(); ) {
            LocalInstance li = (LocalInstance) i.next();
            Formal f = nf.Formal(li.position(), li.flags(),
                                 nf.CanonicalTypeNode(li.position(), li.type()),
                                 li.name());
            f = f.localInstance(li);
            formals.add(f);
        }
        return formals;
    }
    
    List envAsActuals(List env) {
        List actuals = new ArrayList();
        for (Iterator i = env.iterator(); i.hasNext(); ) {
            LocalInstance li = (LocalInstance) i.next();
            TypeNode tn = nf.CanonicalTypeNode(li.position(), li.type());
            Special this_ = nf.Special(li.position(), Special.THIS, tn);
            this_ = (Special) this_.type(li.type());
            actuals.add(this_);
        }
        return actuals;
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
          if (n instanceof New) {
              New newExp = (New) n;
              ClassType ct = (ClassType) newExp.objectType().type();
              
              // If instantiating an inner class, pass in the environment at
              // the class declaration.  env(ct) will be empty of the class
              // was not inner.
              List newArgs = new ArrayList(newExp.arguments());
              newArgs.addAll(envAsActuals(env(ct)));
              newExp = (New) newExp.arguments(newArgs);
              
              n = newExp;
          }
          
          if (n instanceof ClassDecl) {
              ClassDecl cd = (ClassDecl) n;
              
              ParsedClassType ct = cd.type();

              if (ct.isMember() && ! ct.flags().isStatic()) {
                  ct.flags(ct.flags().Static());
                  cd = cd.flags(ct.flags());
                  
                  Context innerContext = context.pushClass(ct, ct);
                  cd = cd.body(translateClassBody(ct, cd.body(), innerContext));
              }
              
              n = cd;
          }

          n = super.leaveCall(old, n, v);
          return n;
    }

    protected ClassBody translateClassBody(ParsedClassType ct, ClassBody body, Context context) {
        List members = new ArrayList(body.members());

        List env = computeClosure(body, context);
        envMap.put(ct, env);
        env = env(ct);

        Map m = new HashMap();

        for (Iterator i = env.iterator(); i.hasNext(); ) {
            LocalInstance li = (LocalInstance) i.next();
            FieldInstance fi = localToField(ct, li);
            m.put(li.type(), fi);
            ct.addField(fi);
            members.add(createFieldDecl(fi));
        }

        body = body.members(members);

        // Rewrite the class body.
        ClassBodyTranslator v = new ClassBodyTranslator(ct, m, context);
        v = (ClassBodyTranslator) v.begin();
        body = (ClassBody) body.visit(v);

        return body;
    }
}
