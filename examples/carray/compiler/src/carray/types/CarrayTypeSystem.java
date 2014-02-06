package carray.types;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public interface CarrayTypeSystem extends TypeSystem {
    ConstArrayType constArrayOf(Type type);

    ConstArrayType constArrayOf(Position pos, Type type);

    ConstArrayType constArrayOf(Type type, int dims);

    ConstArrayType constArrayOf(Position pos, Type type, int dims);
}
