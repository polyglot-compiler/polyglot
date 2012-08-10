package polyglot.ext.jl5.types.inference;

import java.util.List;

import polyglot.types.Type;

public abstract class Constraint {

    protected Type actual;
    protected Type formal;
    protected InferenceSolver solver;

    public Constraint(Type actual, Type formal, InferenceSolver solver) {
        this.actual = actual;
        this.formal = formal;
        this.solver = solver;
    }

    public abstract List<Constraint> simplify();

    public abstract boolean canSimplify();

    public Type actual() {
        return actual;
    }

    public Type formal() {
        return formal;
    }

    public InferenceSolver solver() {
        return solver;
    }

}
