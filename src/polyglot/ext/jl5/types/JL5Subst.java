package polyglot.ext.jl5.types;

import polyglot.ext.param.types.Subst;

public interface JL5Subst extends Subst {

	JL5ProcedureInstance substProcedure(JL5ProcedureInstance mi);

}
