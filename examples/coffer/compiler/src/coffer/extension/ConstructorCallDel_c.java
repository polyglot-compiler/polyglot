package polyglot.ext.coffer.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.ast.*;
import polyglot.ext.coffer.types.*;

import java.util.*;

public class ConstructorCallDel_c extends CofferDel_c {
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        ConstructorCall n = (ConstructorCall) node();
        CofferConstructorInstance vci =
            (CofferConstructorInstance) n.constructorInstance();

        if (throwType == null) {
            return held_keys.removeAll(vci.entryKeys()).addAll(vci.returnKeys());
        }

        for (Iterator i = vci.throwConstraints().iterator(); i.hasNext(); ) {
            ThrowConstraint c = (ThrowConstraint) i.next();
            if (throwType.equals(c.throwType())) {
                return held_keys.removeAll(vci.entryKeys()).addAll(c.keys());
            }
        }

        // Probably a null pointer exception thrown before entry.
        return held_keys;
    }

    public void checkHeldKeys(KeySet held, KeySet stored) throws SemanticException {
        ConstructorCall n = (ConstructorCall) node();
        CofferConstructorInstance vci =
            (CofferConstructorInstance) n.constructorInstance();

        if (! held.containsAll(vci.entryKeys())) {
            throw new SemanticException("Constructor entry " +
                                        keysToString(vci.entryKeys()) +
                                        " not held.", n.position());
        }

        // Cannot hold (return - entry) before entry point.
        KeySet not_held = vci.returnKeys().removeAll(vci.entryKeys());
        not_held = not_held.retainAll(held);

        if (! not_held.isEmpty()) {
            throw new SemanticException("Cannot hold " +
                                        keysToString(not_held) +
                                        " before constructor entry.",
                                        n.position());
        }
    }
}
