package polyglot.ext.carray.types;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.types.*;
import polyglot.ext.jl.types.TypeSystem_c;

public class CarrayTypeSystem extends TypeSystem_c
{
    // ************************************************
    // Methods to give back ConstArrayType objects
    // ************************************************
    public ConstArrayType constArrayOf(Type type) {
        return constArrayOf(type.position(), type);
    }

    public ConstArrayType constArrayOf(Position pos, Type type) {
        return new ConstArrayType_c(this, pos, type, true);
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

    public ArrayType arrayOf(Position pos, Type type) {
        return new ConstArrayType_c(this, pos, type, false);
    }
}
