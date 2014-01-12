/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.Special;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import coffer.types.CofferClassType;
import coffer.types.Key;
import coffer.types.KeySet;

public class SpecialExt_c extends CofferExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        Special e = (Special) node();

        if (e.kind() == Special.THIS) {
            if (e.type() instanceof CofferClassType) {
                Key key = ((CofferClassType) e.type()).key();

                if (key != null) {
                    if (!stored.contains(key)) {
                        throw new SemanticException("Can evaluate expression of type \""
                                                            + e.type()
                                                            + "\" only if key \""
                                                            + key
                                                            + "\" is held by \"this\".",
                                                    e.position());

                    }
                }
            }
        }
    }
}
