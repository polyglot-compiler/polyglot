/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.ast;

import java.util.LinkedHashSet;
import java.util.Set;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.IntersectionType;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.frontend.SchedulerException;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL5CanonicalTypeNodeExt extends JL5TermExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        CanonicalTypeNode n = (CanonicalTypeNode) this.node();
        Type t = n.type();
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        // check subtype constraints on type arguments are obeyed
        checkSubtypeConstraints(ts, t);

        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType st = (JL5SubstClassType) t;

            // Check for rare types: e.g., Outer<String>.Inner, where Inner has uninstantiated type variables
            // See JLS 3rd ed. 4.8
            if (st.isInnerClass() && !st.base().typeVariables().isEmpty()) {
                // st is an inner class, with type variables. Make sure that
                // these type variables are acutally instantiated
                for (TypeVariable tv : st.base().typeVariables()) {
                    if (!st.subst().substitutions().keySet().contains(tv)) {
                        throw new SemanticException("\"Rare\" types are not allowed: cannot "
                                                            + "use raw class "
                                                            + st.name()
                                                            + " when the outer class "
                                                            + st.outer()
                                                            + " has instantiated type variables.",
                                                    n.position());
                    }
                }

            }
        }

        // check for uses of type variables in static contexts
        if (tc.context().inStaticContext()
                && !((JL5Context) tc.context()).inCTORCall()) {
            for (TypeVariable tv : findInstanceTypeVariables(t)) {
                throw new SemanticException("Type variable " + tv
                        + " cannot be used in a static context", n.position());

            }
        }
        ClassType currentClass = tc.context().currentClass();
        JL5Context jc = (JL5Context) tc.context();
        if (jc.inExtendsClause()) {
            currentClass = jc.extendsClauseDeclaringClass();
        }
        if (currentClass != null) {
            if (currentClass.isNested() && !currentClass.isInnerClass()) {
                // the current class is static.
                for (TypeVariable tv : findInstanceTypeVariables(t)) {
                    if (!tv.declaringClass().equals(currentClass)) {
                        throw new SemanticException("Type variable "
                                                            + tv
                                                            + " of class "
                                                            + tv.declaringClass()
                                                            + " cannot be used in a nested class",
                                                    n.position());
                    }
                }
            }

        }

        return superLang().typeCheck(this.node(), tc);
    }

    private Set<TypeVariable> findInstanceTypeVariables(Type t) {
        Set<TypeVariable> s = new LinkedHashSet<>();
        findInstanceTypeVariables(t, s);
        return s;
    }

    private void findInstanceTypeVariables(Type t, Set<TypeVariable> tvs) {
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            if (tv.declaredIn() == TypeVariable.TVarDecl.CLASS_TYPE_VARIABLE) {
                tvs.add(tv);
            }
        }
        if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            findInstanceTypeVariables(at.base(), tvs);
        }
        if (t instanceof WildCardType) {
            WildCardType at = (WildCardType) t;
            findInstanceTypeVariables(at.upperBound(), tvs);
        }
        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType ct = (JL5SubstClassType) t;
            for (ReferenceType at : ct.actuals()) {
                findInstanceTypeVariables(at, tvs);
            }
        }
        if (t instanceof IntersectionType) {
            IntersectionType it = (IntersectionType) t;
            for (Type at : it.bounds()) {
                findInstanceTypeVariables(at, tvs);
            }
        }
        if (t.isClass() && t.toClass().isNested()) {
            findInstanceTypeVariables(t.toClass().outer(), tvs);
        }
    }

    protected void checkSubtypeConstraints(JL5TypeSystem ts, Type t)
            throws SemanticException {
        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType substClass = (JL5SubstClassType) t;
            JL5ParsedClassType pct = substClass.base();
            JL5SubstClassType capSC =
                    (JL5SubstClassType) ts.applyCaptureConversion(substClass,
                                                                  substClass.position());
            for (JL5ParsedClassType cur = pct; cur != null; cur =
                    (JL5ParsedClassType) cur.outer()) {

                for (TypeVariable tv : cur.typeVariables()) {
                    if (!tv.upperBound().isCanonical()) {
                        // need to disambiguate
                        throw new SchedulerException();
                    }

                    Type actual = capSC.subst().substType(tv);
                    Type upperBound =
                            capSC.subst().substType(tv.upperBound());
                    if (!ts.isSubtype(actual, upperBound)) {
                        throw new SemanticException("Type argument " + actual
                                + " is not a subtype of its declared bound "
                                + upperBound, actual.position());
                    }

                    checkSubtypeConstraints(ts, substClass.subst()
                                                          .substType(tv));
                }
            }
        }
        else if (t instanceof ArrayType) {
            checkSubtypeConstraints(ts, ((ArrayType) t).base());
        }
        else if (t instanceof WildCardType) {
            checkSubtypeConstraints(ts, ((WildCardType) t).upperBound());
            checkSubtypeConstraints(ts, ((WildCardType) t).lowerBound());
        }
    }
}
