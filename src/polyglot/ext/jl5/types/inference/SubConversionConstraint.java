package polyglot.ext.jl5.types.inference;

import java.util.ArrayList;
import java.util.List;

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
        List<Constraint> r = new ArrayList<Constraint>();
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
                for (TypeVariable tv : formal_pt.base().typeVariables()) {
                    ReferenceType formal_targ =
                            (ReferenceType) formal_pt.subst().substType(tv);
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
