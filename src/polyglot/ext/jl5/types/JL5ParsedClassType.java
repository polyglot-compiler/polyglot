package polyglot.ext.jl5.types;

import java.util.List;

import polyglot.ext.param.types.PClass;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.util.CodeWriter;

/*
 * A JL5ParsedClassType represents a class with uninstantiated
 * type parameters. In some ways it corresponds to a raw type.
 */
public interface JL5ParsedClassType extends ParsedClassType, JL5ClassType {
    /**
     * Get the pclass for this class. The pclass is used by the param type system to keep
     * track of instantiated types.
     */
    PClass<TypeVariable, ReferenceType> pclass();

    void setPClass(PClass<TypeVariable, ReferenceType> pc);

    void setTypeVariables(List<TypeVariable> typeVars);

    List<TypeVariable> typeVariables();

    void addEnumConstant(EnumInstance ei);

    @Override
    List<EnumInstance> enumConstants();

    @Override
    EnumInstance enumConstantNamed(String name);

    // find methods with compatible name and formals as the given one
    List<? extends JL5MethodInstance> methods(JL5MethodInstance mi);

    //    boolean wasGeneric();

    /**
     * Returns a subst suitable for the erased type: the subst
     * maps any type variables to their erasure. Will return null
     * if the substitution is empty.
     * @return
     */
    JL5Subst erasureSubst();

    void printNoParams(CodeWriter w);

    String toStringNoParams();

    /**
     * Add an AnnotationElemInstance. Should only be used if the
     * ClassType is an Annotation type (i.e., declared using "@interface")
     */
    void addAnnotationElem(AnnotationTypeElemInstance ai);

    void setRetainedAnnotations(RetainedAnnotations createRetainedAnnotations);

}
