package polyglot.ext.pao.types;

import polyglot.types.*;

public interface PaoTypeSystem extends TypeSystem {
    /** Return the method instance for runtime.Primitive.equals */
    MethodInstance primitiveEquals();

    /** Return the method instance for runtime.T.tValue() */
    MethodInstance getter(PrimitiveType t);

    /** Return the constructor instance for runtime.T.T(t) */
    ConstructorInstance wrapper(PrimitiveType t);
}
