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

public class AssignDel_c extends JL_c {
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Assign n = (Assign) super.typeCheck(tc);

        if (n.right().type() instanceof CofferClassType) {
            CofferClassType t = (CofferClassType) n.right().type();

            if (t.key() != null && n.left() instanceof Field) {
                throw new SemanticException("Cannot assign tracked value into a field.", n.position());
            }
            if (t.key() != null && n.left() instanceof ArrayAccess) {
                throw new SemanticException("Cannot assign tracked value into an array.", n.position());
            }
        }

        return n;
    }
}
