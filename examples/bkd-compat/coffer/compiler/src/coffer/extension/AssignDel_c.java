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
import polyglot.ast.JLDel_c;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.visit.TypeChecker;
import coffer.types.CofferClassType;

public class AssignDel_c extends JLDel_c {
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
}
