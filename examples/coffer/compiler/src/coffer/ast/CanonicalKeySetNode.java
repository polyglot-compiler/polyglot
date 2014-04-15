/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import coffer.types.KeySet;

/**
 * A canonical key set AST node.  This is just an AST node
 * veneer around a <code>KeySet</code> type object.
 */
public interface CanonicalKeySetNode extends KeySetNode {
    public CanonicalKeySetNode keys(KeySet keys);
}
