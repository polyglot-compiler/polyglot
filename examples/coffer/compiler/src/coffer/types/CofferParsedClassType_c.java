package polyglot.ext.coffer.types;

import polyglot.types.*;
import polyglot.ext.jl.types.*;
import polyglot.ext.param.types.*;
import polyglot.util.*;
import java.util.*;

public class CofferParsedClassType_c extends ParsedClassType_c
                                         implements CofferParsedClassType
{
    PClass instantiatedFrom;
    Key key;

    public CofferParsedClassType_c(TypeSystem ts, LazyClassInitializer init) {
        super(ts, init);
    }

    public PClass instantiatedFrom() {
        return instantiatedFrom;
    }

    public void setInstantiatedFrom(PClass pc) {
        this.instantiatedFrom = pc;
    }

    public List actuals() {
        if (key != null) {
            return Collections.singletonList(key);
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    public Key key() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
