/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An <code>ArrayTypeNode</code> is a type node for a non-canonical
 * array type.
 */
public interface ArrayTypeNode extends TypeNode, Ambiguous
{
    /**
     * Base of the array.
     */
    TypeNode base();

    /**
     * Set the base of the array.
     */
    ArrayTypeNode base(TypeNode base);
}
