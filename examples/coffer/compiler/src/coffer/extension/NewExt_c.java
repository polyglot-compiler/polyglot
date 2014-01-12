/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.New;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import coffer.types.CofferClassType;
import coffer.types.Key;
import coffer.types.KeySet;

public class NewExt_c extends ProcedureCallExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        New n = (New) node();

        super.checkHeldKeys(held, stored);

        if (n.type() instanceof CofferClassType) {
            Key key = ((CofferClassType) n.type()).key();

            if (key != null) {
                if (held.contains(key) || stored.contains(key)) {
                    throw new SemanticException("Can evaluate \"new\" expression of type \""
                                                        + n.type()
                                                        + "\" only if key \""
                                                        + key
                                                        + "\" is not held.",
                                                n.position());
                }
            }
        }
    }
}
