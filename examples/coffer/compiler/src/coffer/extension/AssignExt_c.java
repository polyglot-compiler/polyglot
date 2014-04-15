/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.extension;

import polyglot.ast.ArrayAccess;
import polyglot.ast.Assign;
import polyglot.ast.Field;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;
import coffer.types.CofferClassType;
import coffer.types.KeySet;

public class AssignExt_c extends CofferExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Assign n = (Assign) super.typeCheck(tc);

        if (n.right().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.right().type();

            if (t.key() != null && n.left() instanceof Field) {
                throw new SemanticException("Cannot assign tracked value into a field.",
                                            n.position());
            }
            if (t.key() != null && n.left() instanceof ArrayAccess) {
                throw new SemanticException("Cannot assign tracked value into an array.",
                                            n.position());
            }
        }

        return n;
    }

    @Override
    public KeySet keyAlias(KeySet stored_keys, Type throwType) {
        Assign n = (Assign) node();

        if (n.right().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.right().type();

            if (t.key() != null && n.left() instanceof Local) {
                return stored_keys.add(t.key());
            }
        }

        return stored_keys;
    }

    @Override
    public void checkHeldKeys(KeySet held, KeySet stored)
            throws SemanticException {
        Assign n = (Assign) node();

        if (n.right().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.right().type();

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
