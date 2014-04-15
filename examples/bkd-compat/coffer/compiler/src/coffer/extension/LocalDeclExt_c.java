/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.LocalDecl;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import coffer.types.CofferClassType;
import coffer.types.KeySet;

public class LocalDeclExt_c extends CofferExt_c {
    @Override
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

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        LocalDecl n = (LocalDecl) node();

        if (n.init() != null && n.init().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.init().type();

            if (t.key() != null && !held.contains(t.key())) {
                throw new SemanticException("Cannot assign tracked value unless key \""
                                                    + t.key() + "\" held.",
                                            n.position());
            }

            if (t.key() != null && stored.contains(t.key())) {
                throw new SemanticException("Cannot assign tracked value with key \""
                                                    + t.key()
                                                    + "\" more than once.",
                                            n.position());
            }
        }
    }
}
