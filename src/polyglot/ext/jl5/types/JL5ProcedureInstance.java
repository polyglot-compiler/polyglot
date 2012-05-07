package polyglot.ext.jl5.types;

import java.util.List;

import polyglot.types.MemberInstance;
import polyglot.types.ProcedureInstance;

public interface JL5ProcedureInstance extends ProcedureInstance, MemberInstance {    
    boolean isVariableArity();    
    
    void setTypeParams(List<TypeVariable> typeParams);
    List<TypeVariable> typeParams();

    /**
     * Returns a subst suitable for the erased type: the subst
     * maps any type variables to their erasure. Will return null
     * if the substitution is empty.
     * @return
     */
    JL5Subst erasureSubst();

}
