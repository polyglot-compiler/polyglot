package polyglot.ext.jl5.types.inference;

import java.util.Collections;
import java.util.List;

import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.Type;

/**
 * Represents a constraint A <: F
 * See JLS 3rd ed. 15.12.2.7
 *
 */
public class SubTypeConstraint extends Constraint {

    public SubTypeConstraint(Type actual, Type formal, InferenceSolver solver) {
        super(actual, formal, solver);
    }

    @Override
    public List<Constraint> simplify() {
        return Collections.emptyList();
    }

    @Override
    public boolean canSimplify() {
        return formal instanceof TypeVariable
                && !solver.isTargetTypeVariable(formal);
    }

    @Override
    public String toString() {
        return actual + " <: " + formal;
    }

}
