package polyglot.ext.jl5.types;

import java.util.LinkedList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.Type;

public interface JL5ClassType extends ClassType {

    /**
     * Is this class a Raw Class? See JLS 3rd ed., 4.8 
     */
    boolean isRawClass();
    
    EnumInstance enumConstantNamed(String name);
    List<EnumInstance> enumConstants();

    /**
     * Return a chain of types that show that this class can be implicitly cast
     * to toType.
     * @param toType
     * @return null if this class cannot be cast to toType. Otherwise, list where the
     *    first element is this and the last element is toType, and each conversion
     *    adds an element to the list.
     */
    LinkedList<Type> isImplicitCastValidChainImpl(Type toType);
}
