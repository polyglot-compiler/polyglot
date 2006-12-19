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
import polyglot.visit.*;
import coffer.ast.*;
import coffer.types.*;

import java.util.*;

public class LocalDeclExt_c extends CofferExt_c {
    public KeySet keyFlow(KeySet held_keys, Type throwType) {
        LocalDecl n = (LocalDecl) node();
        return super.keyFlow(held_keys, throwType);
    }

    public KeySet keyAlias(KeySet stored_keys, Type throwType) {
        LocalDecl n = (LocalDecl) node();

        if (n.init() != null && n.init().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.init().type();

            if (t.key() != null) {
                return stored_keys.add(t.key());
            }
        }

        return stored_keys;
    }

    public void checkHeldKeys(KeySet held, KeySet stored) throws SemanticException {
        LocalDecl n = (LocalDecl) node();

        if (n.init() != null && n.init().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.init().type();

            if (t.key() != null && ! held.contains(t.key())) {
                throw new SemanticException("Cannot assign tracked value unless key \"" + t.key() + "\" held.", n.position());
            }

            if (t.key() != null && stored.contains(t.key())) {
                throw new SemanticException("Cannot assign tracked value with key \"" + t.key() + "\" more than once.", n.position());
            }
        }
    }
}
