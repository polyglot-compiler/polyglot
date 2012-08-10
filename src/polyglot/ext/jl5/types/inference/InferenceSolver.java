package polyglot.ext.jl5.types.inference;

import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

public interface InferenceSolver {

    List<TypeVariable> typeVariablesToSolve();

    boolean isTargetTypeVariable(Type t);

    Map<TypeVariable, ReferenceType> solve(Type expectedReturnType);

    JL5TypeSystem typeSystem();

}
