/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

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
public abstract class InnerClassAbstractRemover extends ContextVisitor
{
    public InnerClassAbstractRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    List env(ClassType ct, boolean includeSuper) {
        if (ct == null || ! ct.isInnerClass()) {
            return Collections.EMPTY_LIST;
        }
        
        List superEnv = Collections.EMPTY_LIST;
        if (includeSuper) {
            superEnv = env((ClassType) ct.superType(), true);
        }
        
        List env = new ArrayList();
        
        for (ClassType outer = ct.outer(); ; outer = outer.outer()) {
            env.add(outer);
            if (! outer.isInnerClass()) {
                break;
            }
        }
        
        env.removeAll(superEnv);
        
        if (env.isEmpty()) {
            return superEnv;
        }
        
        if (superEnv.isEmpty()) {
            return env;
        }
        
        List l = new ArrayList();
        l.addAll(env);
        l.addAll(superEnv);
        return l;
    }
    
    List envAsFormalTypes(List env) {
        return env;
     }
    
    List envAsFormals(List env) {
        List formals = new ArrayList();
        int arg = 1;
        for (Iterator i = env.iterator(); i.hasNext(); arg++) {
            ClassType ct = (ClassType) i.next();
            LocalInstance li = ts.localInstance(Position.compilerGenerated(),
                                                Flags.FINAL, ct, "arg" + arg);
            Formal f = nf.Formal(li.position(), li.flags(),
                                 nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                      li.type()),
                                 li.name());
            f = f.localInstance(li);
            formals.add(f);
        }
        return formals;
    }
    
    List envAsActuals(List env, ClassType outer, Expr qualifier) {
        List actuals = new ArrayList();
        for (Iterator i = env.iterator(); i.hasNext(); ) {
            ClassType ct = (ClassType) i.next();
            if (outer != null && qualifier != null && ct.equals(outer)) {
                actuals.add(qualifier);
                continue;
            }
            TypeNode tn = nf.CanonicalTypeNode(Position.compilerGenerated(), ct);
            Special this_ = nf.Special(Position.compilerGenerated(), Special.THIS, tn);
            this_ = (Special) this_.type(ct);
            actuals.add(this_);
        }
        return actuals;
    }
}
