package polyglot.ext.pao.types;

import polyglot.ext.jl.types.*;
import polyglot.util.*;
import polyglot.types.*;
import java.util.*;

/**
 * An <code>PrimitiveType_c</code> represents a primitive type.
 */
public class PaoParsedTopLevelClassType_c extends ParsedTopLevelClassType_c
{
    protected PaoParsedTopLevelClassType_c() {
        super();
    }

    public PaoParsedTopLevelClassType_c(TypeSystem ts,
                                        LazyClassInitializer init) {
        super(ts, init);
    }

    /** Returns true iff a cast from this to <code>toType</code> is valid. */
    public boolean isCastValid(Type t) {
        return t.isPrimitive() && ts.isSame(this, ts.Object()) ||
               super.isCastValid(t);
    }
}
