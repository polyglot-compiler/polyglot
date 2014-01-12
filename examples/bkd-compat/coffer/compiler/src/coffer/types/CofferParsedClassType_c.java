/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.Collections;
import java.util.List;

import polyglot.ext.param.types.PClass;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.ParsedClassType_c;
import polyglot.types.TypeSystem;

public class CofferParsedClassType_c extends ParsedClassType_c implements
        CofferParsedClassType {
    PClass<Key, Key> instantiatedFrom;
    Key key;

    public CofferParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
            Source fromSource) {
        super(ts, init, fromSource);
    }

    @Override
    public PClass<Key, Key> instantiatedFrom() {
        return instantiatedFrom;
    }

    @Override
    public void setInstantiatedFrom(PClass<Key, Key> pc) {
        this.instantiatedFrom = pc;
    }

    @Override
    public List<Key> actuals() {
        if (key != null) {
            return Collections.singletonList(key);
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }
}
