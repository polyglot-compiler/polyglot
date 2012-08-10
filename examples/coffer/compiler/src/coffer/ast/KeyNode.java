/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.Node;
import coffer.types.Key;

/**
 * An AST node for a <code>Key</code>.  The key may be ambiguous. 
 */
public interface KeyNode extends Node {
    public Key key();

    public String name();

    public KeyNode key(Key key);
}
