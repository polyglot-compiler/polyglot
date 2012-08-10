package polyglot.ext.jl5.types;

import polyglot.ext.param.types.Param;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;

public interface TypeVariable extends ReferenceType, Param {

    public static enum TVarDecl {
        CLASS_TYPE_VARIABLE, PROCEDURE_TYPE_VARIABLE, SYNTHETIC_TYPE_VARIABLE
    }

    TVarDecl declaredIn();

    void declaringProcedure(JL5ProcedureInstance pi);

    void declaringClass(ClassType ct);

    ClassType declaringClass();

    JL5ProcedureInstance declaringProcedure();

    int uniqueIdentifier();

    void name(String name);

    String name();

//	void setBounds(List<ReferenceType> newBounds);
//	List<ReferenceType> bounds();
    ReferenceType erasureType();

//	/**
//	 * Non-destructive update of the bounds list
//	 */
//    Type bounds(List<ReferenceType> newbounds);

    /**
     * Does this type variable have a lower bound? See JLS 3rd ed 4.10.2 and 5.1.10
     * @return
     */
    boolean hasLowerBound();

    ReferenceType upperBound();

    ReferenceType lowerBound();

    void setUpperBound(ReferenceType upperBound);

    void setLowerBound(ReferenceType lowerBound);

    TypeVariable upperBound(ReferenceType upperBound);
}
