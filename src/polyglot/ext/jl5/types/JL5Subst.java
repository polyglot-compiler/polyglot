package polyglot.ext.jl5.types;

import polyglot.ext.param.types.Subst;
import polyglot.types.ReferenceType;

public interface JL5Subst extends Subst<TypeVariable, ReferenceType> {

    JL5ProcedureInstance substProcedure(JL5ProcedureInstance mi);

}
