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
import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassConstructorFixer extends InnerClassAbstractRemover {
    public InnerClassConstructorFixer(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof New) {
            New newExp = (New) n;
            ClassType ct = (ClassType) newExp.objectType().type();

            // If instantiating an inner class, pass in the environment at
            // the class declaration.  env(ct) will be empty of the class
            // was not inner.
            List<Expr> newArgs = new ArrayList<Expr>(newExp.arguments());
            newArgs.addAll(envAsActuals(env(ct, true),
                                        ct.outer(),
                                        newExp.qualifier()));
            newExp = (New) newExp.arguments(newArgs);

            // Remove the qualifier.
            // FIXME: should pass in with arguments.
            // FIXME: need a barrier after this pass.
            // FIXME: should rewrite "new" after the barrier.
            // or should pass in all enclosing classes
            newExp = newExp.qualifier(null);

            n = newExp;
        }

        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;

            ClassType ct = context.currentClass();

            if (cc.kind() == ConstructorCall.THIS) {
                List<Expr> newArgs = new ArrayList<Expr>();
                newArgs.addAll(cc.arguments());
                newArgs.addAll(envAsActuals(env(ct, true),
                                            ct.outer(),
                                            cc.qualifier()));

                ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
                newCC = newCC.qualifier(null);
                n = newCC;
            }
            else {
                // adjust the super call arguments
                List<Expr> newArgs = new ArrayList<Expr>();
                newArgs.addAll(cc.arguments());
                ClassType sup = (ClassType) ct.superType();
                if (sup.isInnerClass()) {
                    newArgs.addAll(envAsActuals(env(sup, true),
                                                sup.outer(),
                                                cc.qualifier()));
                }
                else {
                    newArgs.addAll(envAsActuals(env(sup, true), null, null));
                }

                ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
                newCC = newCC.qualifier(null);
                n = newCC;
            }
        }

        n = super.leaveCall(old, n, v);
        return n;
    }
}
