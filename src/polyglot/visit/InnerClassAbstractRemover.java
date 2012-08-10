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
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Formal;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public abstract class InnerClassAbstractRemover extends ContextVisitor {
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
    List<ClassType> env(ClassType ct, boolean includeSuper) {
        if (ct == null || !ct.isInnerClass()) {
            return Collections.emptyList();
        }

        List<ClassType> superEnv = Collections.emptyList();
        if (includeSuper) {
            superEnv = env((ClassType) ct.superType(), true);
        }

        List<ClassType> env = new ArrayList<ClassType>();

        for (ClassType outer = ct.outer();; outer = outer.outer()) {
            env.add(outer);
            if (!outer.isInnerClass()) {
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

        List<ClassType> l = new ArrayList<ClassType>();
        l.addAll(env);
        l.addAll(superEnv);
        return l;
    }

    List<ClassType> envAsFormalTypes(List<ClassType> env) {
        return env;
    }

    List<Formal> envAsFormals(List<ClassType> env) {
        List<Formal> formals = new ArrayList<Formal>();
        int arg = 1;
        for (ClassType ct : env) {
            LocalInstance li =
                    ts.localInstance(Position.compilerGenerated(),
                                     Flags.FINAL,
                                     ct,
                                     "arg" + arg);
            Formal f =
                    nf.Formal(li.position(),
                              li.flags(),
                              nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                   li.type()),
                              nf.Id(li.position(), li.name()));
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
    List<Expr> envAsActuals(List<ClassType> env, ClassType outer, Expr qualifier) {
        List<Expr> actuals = new ArrayList<Expr>();
        for (ClassType ct : env) {
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
                            nf.Special(Position.compilerGenerated(),
                                       Special.THIS);
                    this_ = (Special) this_.type(ct);
                    Field field =
                            nf.Field(Position.compilerGenerated(),
                                     this_,
                                     nf.Id(Position.compilerGenerated(), name));
                    field = field.fieldInstance(fi);
                    field = (Field) field.type(fi.type());
                    actuals.add(field);
                    continue;
                }
            }

            TypeNode tn =
                    nf.CanonicalTypeNode(Position.compilerGenerated(), ct);
            Special this_ =
                    nf.Special(Position.compilerGenerated(), Special.THIS, tn);
            this_ = (Special) this_.type(ct);
            actuals.add(this_);
        }
        return actuals;
    }
}
