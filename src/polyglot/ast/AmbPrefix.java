/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An <code>AmbPrefix</code> is an ambiguous AST node composed of dot-separated
 * list of identifiers that must resolve to a prefix.
 */
public interface AmbPrefix extends Prefix, Ambiguous
{
    /**
     * Prefix of the prefix.
     */
    Prefix prefix();

    /**
     * Ambiguous name.
     */
    String name();
    
    Id nameNode();
}
