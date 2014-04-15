/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import coffer.ast.Free;
import coffer.types.CofferClassType;
import coffer.types.KeySet;

public class FreeExt_c extends CofferExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        Free f = (Free) node();

        Type t = f.expr().type();

        if (!(t instanceof CofferClassType)) {
            return held_keys;
        }

        CofferClassType ct = (CofferClassType) t;

        return held_keys.remove(ct.key());
    }

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        Free f = (Free) node();

        Type t = f.expr().type();

        if (!(t instanceof CofferClassType)) {
            throw new SemanticException("Cannot free expression of "
                    + "non-tracked type \"" + t + "\".", f.position());
        }

        CofferClassType ct = (CofferClassType) t;

        if (!held.contains(ct.key())) {
            throw new SemanticException("Key \"" + ct.key() + "\" not held.",
                                        f.position());
        }
    }
}
