/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.ProcedureCall;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import coffer.types.CofferProcedureInstance;
import coffer.types.KeySet;
import coffer.types.ThrowConstraint;

public class ProcedureCallExt_c extends CofferExt_c {
    @Override
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        ProcedureCall n = (ProcedureCall) node();
        CofferProcedureInstance vmi =
                (CofferProcedureInstance) n.procedureInstance();

        if (throwType == null) {
            return held_keys.removeAll(vmi.entryKeys())
                            .addAll(vmi.returnKeys());
        }

        for (ThrowConstraint c : vmi.throwConstraints()) {
            if (throwType.equals(c.throwType())) {
                return held_keys.removeAll(vmi.entryKeys()).addAll(c.keys());
            }
        }

        // Probably a null pointer exception thrown before entry.
        return held_keys;
    }

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        ProcedureCall n = (ProcedureCall) node();
        CofferProcedureInstance vmi =
                (CofferProcedureInstance) n.procedureInstance();

        if (!held.containsAll(vmi.entryKeys())) {
            throw new SemanticException("Entry "
                                                + keysToString(vmi.entryKeys())
                                                + " not held.",
                                        n.position());
        }

        // Cannot hold (return - entry) before entry point.
        KeySet not_held = vmi.returnKeys().removeAll(vmi.entryKeys());
        not_held = not_held.retainAll(held);

        if (!not_held.isEmpty()) {
            throw new SemanticException("Cannot hold " + keysToString(not_held)
                    + " before " + vmi.designator() + " entry.", n.position());
        }
    }
}
