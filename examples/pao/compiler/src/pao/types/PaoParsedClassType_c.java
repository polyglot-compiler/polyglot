package polyglot.ext.pao.types;

import polyglot.ext.jl.types.*;
import polyglot.util.*;
import polyglot.types.*;
import java.util.*;

/**
 * A PAO class type.  This class overrides type checking of casts to
 * primitives.
 */
public class PaoParsedClassType_c extends ParsedClassType_c
{
    protected PaoParsedClassType_c() {
        super();
    }

    public PaoParsedClassType_c(TypeSystem ts, LazyClassInitializer init) {
        super(ts, init);
    }

    /** Returns true iff a cast from this to <code>toType</code> is valid. */
    public boolean isCastValidImpl(Type toType) {
        return toType.isPrimitive() && ts.equals(this, ts.Object()) ||
               super.isCastValidImpl(toType);
    }
}
