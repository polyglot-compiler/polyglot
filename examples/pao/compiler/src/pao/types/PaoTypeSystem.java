package polyglot.ext.pao.types;

import polyglot.types.*;

/**
 * The PAO type system interface.
 */
public interface PaoTypeSystem extends TypeSystem {
    /** Return the method instance for runtime.Primitive.equals */
    MethodInstance primitiveEquals();

    /** Return the method instance for runtime.T.tValue() */
    MethodInstance getter(PrimitiveType t);

    /** Return the constructor instance for runtime.T.T(t) */
    ConstructorInstance wrapper(PrimitiveType t);

    /** Return boxed type runtime.T for primitive t. */
    Type boxedType(PrimitiveType t);
}
