package polyglot.ext.pao.types;

import polyglot.ext.jl.types.*;
import polyglot.types.*;
import java.util.*;

/**
 * A PAO primitive type.  A primitive is now considered a subtype of Object.
 */
public class PaoPrimitiveType_c extends PrimitiveType_c
{
    /** Used for deserializing types. */
    protected PaoPrimitiveType_c() { }

    public PaoPrimitiveType_c(TypeSystem ts, Kind kind) {
        super(ts, kind);
    }

    /**
     * Return true if this type strictly descends from <code>ancestor</code>.
     */
    public boolean descendsFromImpl(Type ancestor) {
        return ts.equals(ancestor, ts.Object());
    }

    /** Return true if this type can be assigned to <code>toType</code>. */
    public boolean isImplicitCastValidImpl(Type toType) {
        return ts.equals(toType, ts.Object()) ||
               super.isImplicitCastValidImpl(toType);
    }

    /** Returns true iff a cast from this to <code>toType</code> is valid. */
    public boolean isCastValidImpl(Type toType) {
        return ts.equals(toType, ts.Object()) || super.isCastValidImpl(toType);
    }
}
