package polyglot.ext.jl7.types.inference;

import java.util.ArrayList;
import java.util.List;

import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.inference.InferenceSolver_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

public class JL7InferenceSolver_c extends InferenceSolver_c {

    public JL7InferenceSolver_c(JL5ProcedureInstance pi,
            List<? extends Type> actuals, JL5TypeSystem ts) {
        super(pi, actuals, ts);
    }

    @Override
    protected List<TypeVariable> typeVariablesToSolve(JL5ProcedureInstance pi) {
        if (pi instanceof JL5ConstructorInstance) {
            JL5ConstructorInstance ci = (JL5ConstructorInstance) pi;
            ReferenceType ct = ci.container();
            if (ct instanceof JL5ParsedClassType) {
                JL5ParsedClassType pct = (JL5ParsedClassType) ct;
                List<TypeVariable> result =
                        new ArrayList<TypeVariable>(pct.typeVariables().size()
                                + pi.typeParams().size());
                result.addAll(pct.typeVariables());
                result.addAll(pi.typeParams());
                return result;
            }
        }
        return super.typeVariablesToSolve(pi);
    }

    @Override
    protected Type returnType(JL5ProcedureInstance pi) {
        if (pi instanceof ConstructorInstance) {
            return ((ConstructorInstance) pi).container();
        }
        return super.returnType(pi);
    }
}
