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
package polyglot.ext.jl5.types.inference;

import java.util.ArrayList;
import java.util.List;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5PrimitiveType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.types.NullType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

/**
 * Represents a constraint A << F
 * See JLS 3rd ed. 15.12.2.7
 *
 */
public class SubConversionConstraint extends Constraint {

    public SubConversionConstraint(Type actual, Type formal,
            InferenceSolver solver) {
        super(actual, formal, solver);
    }

    @Override
    public List<Constraint> simplify() {
        List<Constraint> r = new ArrayList<>();
        if (actual instanceof JL5PrimitiveType) {
            JL5PrimitiveType prim_actual = (JL5PrimitiveType) actual;
            r.add(new SubConversionConstraint(solver().typeSystem()
                                                      .wrapperClassOfPrimitive(prim_actual),
                                              formal,
                                              solver));
        }
        else if (actual instanceof NullType) {
            // no constraint implied!
        }
        else if (solver().isTargetTypeVariable(formal)) {
            r.add(new SubTypeConstraint(actual, formal, solver));
        }
        else if (formal.isArray()) {
            if (actual.isArray() && actual.toArray().base().isReference()) {
                r.add(new SubConversionConstraint(actual.toArray().base(),
                                                  formal.toArray().base(),
                                                  solver));
            }
            else if (actual instanceof TypeVariable) {
                TypeVariable actual_tv = (TypeVariable) actual;
                Type b = actual_tv.upperBound();
                if (b.isArray() && b.toArray().base().isReference()) {
                    r.add(new SubConversionConstraint(b.toArray().base(),
                                                      formal.toArray().base(),
                                                      solver));
                }
            }
        }
        else if (formal instanceof JL5SubstClassType) {
            JL5SubstClassType formal_pt = (JL5SubstClassType) formal;
            JL5SubstClassType s =
                    solver.typeSystem()
                          .findGenericSupertype(formal_pt.base(),
                                                (ReferenceType) actual);
            if (s != null) {
                JL5ParsedClassType g = formal_pt.base();
                for (JL5ParsedClassType cur = g; cur != null; cur =
                        (JL5ParsedClassType) cur.outer()) {
                    for (TypeVariable tv : cur.typeVariables()) {
                        ReferenceType formal_targ =
                                (ReferenceType) formal_pt.subst()
                                                         .substType(tv);
                        ReferenceType actual_targ =
                                (ReferenceType) s.subst().substType(tv);
                        if (!(formal_targ instanceof WildCardType)) {
                            r.add(new EqualConstraint(actual_targ,
                                                      formal_targ,
                                                      solver));
                        }
                        else if (formal_targ instanceof WildCardType
                                && ((WildCardType) formal_targ).isExtendsConstraint()) {
                            WildCardType formal_targ_wc =
                                    (WildCardType) formal_targ;
                            if (!(actual_targ instanceof WildCardType)) {
                                r.add(new SubConversionConstraint(actual_targ,
                                                                  formal_targ_wc.upperBound(),
                                                                  solver));
                            }
                            else if (actual_targ instanceof WildCardType
                                    && ((WildCardType) actual_targ).isExtendsConstraint()) {
                                WildCardType actual_targ_wc =
                                        (WildCardType) actual_targ;
                                r.add(new SubConversionConstraint(actual_targ_wc.upperBound(),
                                                                  formal_targ_wc.upperBound(),
                                                                  solver));
                            }
                        }
                        else if (formal_targ instanceof WildCardType
                                && ((WildCardType) formal_targ).isSuperConstraint()) {
                            WildCardType formal_targ_wc =
                                    (WildCardType) formal_targ;
                            if (!(actual_targ instanceof WildCardType)) {
                                r.add(new SuperConversionConstraint(actual_targ,
                                                                    formal_targ_wc.lowerBound(),
                                                                    solver));
                            }
                            else if (actual_targ instanceof WildCardType
                                    && ((WildCardType) actual_targ).isSuperConstraint()) {
                                WildCardType actual_targ_wc =
                                        (WildCardType) actual_targ;
                                r.add(new SuperConversionConstraint(actual_targ_wc.lowerBound(),
                                                                    formal_targ_wc.lowerBound(),
                                                                    solver));
                            }
                        }
                    }
                }
            }
        }
        return r;
    }

    @Override
    public boolean canSimplify() {
        return true;
    }

    @Override
    public String toString() {
        return actual + " << " + formal;
    }

}
