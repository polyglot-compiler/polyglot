package polyglot.ext.jl5.types.inference;

import java.util.ArrayList;
import java.util.List;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType_c;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.types.NullType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

public class EqualConstraint extends Constraint {

    public EqualConstraint(ReferenceType actual, ReferenceType formal,
            InferenceSolver solver) {
        super(actual, formal, solver);
    }

    @Override
    public List<Constraint> simplify() {
        List<Constraint> r = new ArrayList<Constraint>();
        if (actual instanceof NullType) {
            // no constraint implied!
        }
        else if (formal.isArray()) {
            if (actual.isArray() && actual.toArray().base().isReference()) {
                r.add(new EqualConstraint((ReferenceType) actual.toArray()
                                                                .base(),
                                          (ReferenceType) formal.toArray()
                                                                .base(),
                                          solver));
            }
            else if (actual instanceof TypeVariable) {
                TypeVariable actual_tv = (TypeVariable) actual;
                Type ub = actual_tv.upperBound(); // XXX do we also need to check if there is an intersection type?
                if (ub.isArray() && ub.toArray().base().isReference()) {
                    r.add(new EqualConstraint((ReferenceType) ub.toArray()
                                                                .base(),
                                              (ReferenceType) formal.toArray()
                                                                    .base(),
                                              solver));
                }
            }
        }
        else if (formal instanceof JL5SubstClassType_c
                && actual instanceof JL5SubstClassType_c) {
            // both formal and actual are parameterized class types
            JL5SubstClassType_c formal_pt = (JL5SubstClassType_c) formal;
            JL5SubstClassType_c actual_pt = (JL5SubstClassType_c) actual;
            if (formal_pt.base().equals(actual_pt.base())) {
                JL5ParsedClassType g = formal_pt.base();
                for (TypeVariable tv : g.typeVariables()) {

                    ReferenceType formal_targ =
                            (ReferenceType) formal_pt.subst().substType(tv);
                    ReferenceType actual_targ =
                            (ReferenceType) actual_pt.subst().substType(tv);
                    if (!(formal_targ instanceof WildCardType)
                            && !(actual_targ instanceof WildCardType)) {
                        r.add(new EqualConstraint(actual_targ,
                                                  formal_targ,
                                                  solver));
                    }
                    else if (formal_targ instanceof WildCardType
                            && actual_targ instanceof WildCardType) {
                        WildCardType formal_targ_wc =
                                (WildCardType) formal_targ;
                        WildCardType actual_targ_wc =
                                (WildCardType) actual_targ;
                        if (formal_targ_wc.isSuperConstraint()
                                && actual_targ_wc.isSuperConstraint()) {
                            r.add(new EqualConstraint(formal_targ_wc.lowerBound(),
                                                      actual_targ_wc.lowerBound(),
                                                      solver));
                        }
                        if (formal_targ_wc.isExtendsConstraint()
                                && actual_targ_wc.isExtendsConstraint()) {
                            r.add(new EqualConstraint(formal_targ_wc.upperBound(),
                                                      actual_targ_wc.upperBound(),
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
        return !solver.isTargetTypeVariable(formal);
    }

    @Override
    public String toString() {
        return actual + " = " + formal;
    }

}
