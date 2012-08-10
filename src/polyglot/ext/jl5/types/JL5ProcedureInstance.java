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

    /**
     * Annotations on the declaration of this type such that the annotation type has
     * a retention policy of annotation.RetentionPolicy.CLASS or annotation.RetentionPolicy.RUNTIME.
     */
    RetainedAnnotations retainedAnnotations();

    void setRetainedAnnotations(RetainedAnnotations createRetainedAnnotations);

}
