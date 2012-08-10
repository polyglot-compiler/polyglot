/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.ProcedureDecl;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import coffer.types.CofferProcedureInstance;
import coffer.types.KeySet;
import coffer.types.ThrowConstraint;

/** The Coffer extension of the <code>ProcedureDecl</code> node. 
 * 
 *  @see polyglot.ast.ProcedureDecl
 */
public class ProcedureDeclExt_c extends CofferExt_c {
    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        ProcedureDecl n = (ProcedureDecl) node();

        CofferProcedureInstance pi =
                (CofferProcedureInstance) n.procedureInstance();

        checkHeldKeys(held, pi.returnKeys(), n.position());

    }

    public void checkHeldKeysThrowConstraint(ThrowConstraint tc, KeySet held,
            KeySet stored) throws SemanticException {
        checkHeldKeys(held, tc.keys(), tc.throwType().position());

    }

    private void checkHeldKeys(KeySet held, KeySet returnKeys, Position pos)
            throws SemanticException {
        if (!held.equals(returnKeys)) {
            KeySet too_much = held.removeAll(returnKeys);
            returnKeys.removeAll(held);

            if (too_much.size() == 1) {
                too_much.iterator().next();
                throw new SemanticException(KeysToString(too_much)
                        + " not freed at return.", pos);
            }
            else if (!too_much.isEmpty()) {
                throw new SemanticException(KeysToString(too_much)
                        + " not freed at return.", pos);
            }

            /*
            if (! not_enough.isEmpty())
                throw new SemanticException(KeysToString(not_enough) +
                                            " not held at return.",
                                            n.position());
                                            */
        }
    }
}
