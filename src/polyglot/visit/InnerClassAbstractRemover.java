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

    protected String namePrefix() {
        return "jl$";
    }
    
    /**
     * Mangles the name of the given class.  Used to produce names of fields.
     */
    String mangleClassName(ClassType ct) {
      return namePrefix() + ct.fullName().replace('.', '$');
    }

    /**
     * Returns the inner-class environment of the given class type and,
     * optionally, its super types. The "inner class environment" of a class
     * type is defined recursively as:
     * <ul>
     * <li>empty, if the class type is not an inner class; otherwise,</li>
     * <li>the class type itself and the inner class environment of its outer
     * class type.</li>
     * </ul>
     */
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
    
    /**
     * Turns an inner class's environment into a list of actual arguments to be
     * used when constructing the inner class.
     * 
     * @param env
     *          the inner class's environment.
     * @param outer
     *          the class enclosing the inner class.
     * @param qualifier
     *          the <code>new</code> expression's qualifier.
     * @see polyglot.visit.InnerClassAbstractRemover#env(ClassType, boolean)
     */
    List envAsActuals(List env, ClassType outer, Expr qualifier) {
        List actuals = new ArrayList();
        for (Iterator i = env.iterator(); i.hasNext(); ) {
            ClassType ct = (ClassType) i.next();
            if (outer != null && qualifier != null && ct.equals(outer)) {
                actuals.add(qualifier);
                continue;
            }
            
            if (outer != null) {
              // XXX Search "outer" for a field whose name and type matches "ct".
              String name = mangleClassName(ct);
              FieldInstance fi = outer.fieldNamed(name);
              if (fi != null && fi.type().equals(ct)) {
                // Use the field.
                Special this_ =
                  nf.Special(Position.compilerGenerated(), Special.THIS);
                this_ = (Special) this_.type(ct);
                Field field =
                  nf.Field(Position.compilerGenerated(), this_, nf.Id(Position
                      .compilerGenerated(), name));
                field = field.fieldInstance(fi);
                field = (Field) field.type(fi.type());
                actuals.add(field);
                continue;
              }
            }
            
            TypeNode tn = nf.CanonicalTypeNode(Position.compilerGenerated(), ct);
            Special this_ = nf.Special(Position.compilerGenerated(), Special.THIS, tn);
            this_ = (Special) this_.type(ct);
            actuals.add(this_);
        }
        return actuals;
    }
}
