/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import coffer.ast.*;
import coffer.types.*;

import java.util.*;

public class NewExt_c extends ProcedureCallExt_c {
    public void checkHeldKeys(KeySet held, KeySet stored)
        throws SemanticException
    {
        New n = (New) node();

        CofferConstructorInstance vci =
            (CofferConstructorInstance) n.constructorInstance();

        super.checkHeldKeys(held, stored);

        if (n.type() instanceof CofferClassType) {
            Key key = ((CofferClassType) n.type()).key();

            if (key != null) {
                if (held.contains(key) || stored.contains(key)) {
                    throw new SemanticException(
                        "Can evaluate \"new\" expression of type \"" +
                        n.type() + "\" only if key \"" + key +
                        "\" is not held.", n.position());
                }
            }
        }
    }
}
