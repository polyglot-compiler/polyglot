package polyglot.ext.coffer.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.ast.*;
import polyglot.ext.coffer.types.*;

import java.util.*;

public class CallDel_c extends CofferDel_c {
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        Call n = (Call) node();
        CofferMethodInstance vmi = (CofferMethodInstance) n.methodInstance();

        if (throwType == null) {
            return held_keys.removeAll(vmi.entryKeys()).addAll(vmi.returnKeys());
        }

        for (Iterator i = vmi.throwConstraints().iterator(); i.hasNext(); ) {
            ThrowConstraint c = (ThrowConstraint) i.next();
            if (throwType.equals(c.throwType())) {
                return held_keys.removeAll(vmi.entryKeys()).addAll(c.keys());
            }
        }

        // Probably a null pointer exception thrown before entry.
        return held_keys;
    }

    public void checkHeldKeys(KeySet held, KeySet stored) throws SemanticException {
        Call n = (Call) node();
        CofferMethodInstance vmi = (CofferMethodInstance) n.methodInstance();

        if (! held.containsAll(vmi.entryKeys())) {
            throw new SemanticException("Method entry " +
                                        keysToString(vmi.entryKeys()) +
                                        " not held.", n.position());
        }

        // Cannot hold (return - entry) before entry point.
        KeySet not_held = vmi.returnKeys().removeAll(vmi.entryKeys());
        not_held = not_held.retainAll(held);

        if (! not_held.isEmpty()) {
            throw new SemanticException("Cannot hold " +
                                        keysToString(not_held) +
                                        " before method entry.",
                                        n.position());
        }
    }
}
