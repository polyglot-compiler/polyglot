package jltools.ext.carray.types;

import jltools.types.*;
import jltools.util.*;
import jltools.ext.jl.types.*;
import java.util.*;

/**
 * A <code>ConstArrayType</code> represents an array of base java types,
 * whose elements cannot change after initialization.
 */
public class ConstArrayType_c extends ArrayType_c implements ConstArrayType
{

    /** Used for deserializing types. */
    protected ConstArrayType_c() { }

    public ConstArrayType_c(TypeSystem ts, Position pos, Type base) {
        super(ts, pos, base);
    }

    public String toString() {
        return base.toString() + " const []";
    }

    public boolean equals(Object o) {
        if (o instanceof ConstArrayType) {
            ConstArrayType t = (ConstArrayType) o;
            return base.isSame(t.base());
        }

        return false;
    }
}
