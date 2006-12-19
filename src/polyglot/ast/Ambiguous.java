/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * <code>Ambiguous</code> represents an ambiguous AST node.  This interface is
 * just a tag.  These nodes should not appear after the disambiguate
 * pass.
 */
public interface Ambiguous extends Node
{
}
