package carray_jl5.types;

import polyglot.ext.jl5.types.JL5ArrayType;

/**
 * A <code>ConstArrayType</code> represents an array of base java types,
 * whose elements cannot change after initialization.
 */
public interface ConstArrayType extends JL5ArrayType,
        carray.types.ConstArrayType {
}
