package polyglot.ext.pao.types;

import polyglot.ext.jl.types.*;
import polyglot.util.*;
import polyglot.types.*;
import java.util.*;

/**
 * An <code>PrimitiveType_c</code> represents a primitive type.
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
    public boolean isCastValidImpl(Type t) {
        return t.isPrimitive() && ts.equals(this, ts.Object()) ||
               super.isCastValidImpl(t);
    }
}
