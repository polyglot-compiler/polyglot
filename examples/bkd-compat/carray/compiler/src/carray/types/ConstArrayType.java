package carray.types;

import polyglot.types.*;
/**
 * A <code>ConstArrayType</code> represents an array of base java types,
 * whose elements cannot change after initialization.
 */
public interface ConstArrayType extends ArrayType
{
    boolean isConst();
}
