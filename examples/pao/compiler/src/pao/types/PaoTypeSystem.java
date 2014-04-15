/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.types;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.PrimitiveType;
import polyglot.types.TypeSystem;

/**
 * The PAO type system interface. Several new methods are added to the
 * type system to facilitate the boxing and unboxing of primitive values.
 */
public interface PaoTypeSystem extends TypeSystem {
    /**
     * Returns the method instance for the runtime method that tests two boxed
     * primitive values for equality.
     * 
     * @return the method instance for the runtime method that tests two boxed
     *         primitive values for equality.
     * 
     * @see pao.runtime.Primitive#equals(Object, Object)
     */
    MethodInstance primitiveEquals();

    /**
     * Returns the method instance for getting the primitive value from a boxed
     * representation of primitive values of type <code>t</code>.
     * 
     * @param t the primitive type for which we want the getter method to access
     *            the primitive value of a boxed primitive value.
     * @return the method instance for getting the primitive value from a boxed
     *         representation of primitive values of type <code>t</code>.
     * 
     * @see pao.runtime.Boolean#booleanValue()
     * @see pao.runtime.Byte#byteValue()
     * @see pao.runtime.Character#charValue()
     * @see pao.runtime.Double#doubleValue()
     * @see pao.runtime.Float#floatValue()
     * @see pao.runtime.Integer#intValue()
     * @see pao.runtime.Long#longValue()
     * @see pao.runtime.Short#shortValue()
     */
    MethodInstance getter(PrimitiveType t);

    /**
     * Returns the constructor instance for the class used to represent boxed
     * values of type <code>t</code>.
     * 
     * @param t the <code>PrimitiveType</code> for which the constructor
     *            instance of the class representing boxed values is returned.
     * @return the constructor instance for the class used to represent boxed
     *         values of type <code>t</code>.
     */
    ConstructorInstance wrapper(PrimitiveType t);

    /**
     * Returns the class type used to represent boxed values of type
     * <code>t</code>.
     * 
     * @param t the <code>PrimitiveType</code> for which the type used to
     *            represent boxed values is returned.
     * @return the class type used to represent boxed values of type
     *         <code>t</code>.
     */
    ClassType boxedType(PrimitiveType t);
}
