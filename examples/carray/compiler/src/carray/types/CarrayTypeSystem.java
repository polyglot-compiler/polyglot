package jltools.ext.carray.types;

import jltools.util.Position;
import jltools.util.InternalCompilerError;
import jltools.types.*;
import jltools.ext.jl.types.TypeSystem_c;

public class CarrayTypeSystem extends TypeSystem_c
{
    // ************************************************
    // Methods to give back ConstArrayType objects
    // ************************************************
    public ConstArrayType constArrayOf(Type type) {
        return new ConstArrayType_c(this, type.position(), type);
    }

    public ConstArrayType constArrayOf(Position pos, Type type) {
        return new ConstArrayType_c(this, pos, type);
    }

    public ConstArrayType constArrayOf(Type type, int dims) {
        return constArrayOf(null, type, dims);
    }

    public ConstArrayType constArrayOf(Position pos, Type type, int dims) {
    if (dims > 1) {
        return constArrayOf(pos, constArrayOf(pos, type, dims-1));
    }
    else if (dims == 1) {
        return constArrayOf(pos, type);
    }
    else {
        throw new InternalCompilerError(
        "Must call constArrayOf(type, dims) with dims > 0");
    }
    }

    // ************************************************
    // Override methods to specify where const arrays can and can't be used.
    // ************************************************

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff child and ancestor are non-primitive
     * types, and a variable of type child may be legally assigned
     * to a variable of type ancestor.
     */
    public boolean isAssignableSubtype(Type child, Type ancestor) {
        if (child instanceof ConstArrayType && !(ancestor instanceof ConstArrayType)) {
            return false;
        }
        return super.isAssignableSubtype(child, ancestor);
    }
}
