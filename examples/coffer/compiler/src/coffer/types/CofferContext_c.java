/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.HashMap;
import java.util.Map;

import polyglot.types.Context_c;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

public class CofferContext_c extends Context_c implements CofferContext {
    protected Map<String, Key> keys;

    public CofferContext_c(TypeSystem ts) {
        super(ts);
    }

    @Override
    protected Context_c push() {
        CofferContext_c c = (CofferContext_c) super.push();
        c.keys = null;
        return c;
    }

    @Override
    public void addKey(Key key) {
        if (keys == null) {
            keys = new HashMap<String, Key>();
        }

        keys.put(key.name(), key);
    }

    @Override
    public Key findKey(String name) throws SemanticException {
        Key key = null;

        if (keys != null) {
            key = keys.get(name);
        }

        if (key != null) {
            return key;
        }

        if (outer != null) {
            return ((CofferContext) outer).findKey(name);
        }

        throw new SemanticException("Key \"" + name + "\" not found.");
    }

    @Override
    protected String mapsToString() {
        return super.mapsToString() + " keys=" + keys;
    }
}
