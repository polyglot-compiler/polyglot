/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Ambiguous;
import polyglot.ast.TypeNode;

/** Type node for a class instantiated with a key.
 */
public interface TrackedTypeNode extends TypeNode, Ambiguous {
    TypeNode base();

    TrackedTypeNode base(TypeNode base);

    KeyNode key();

    TrackedTypeNode key(KeyNode key);
}
