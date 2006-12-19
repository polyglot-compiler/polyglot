/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A <code>BooleanLit</code> represents a boolean literal expression.
 */
public interface BooleanLit extends Lit
{
    /**
     * The literal's value.
     */
    boolean value();

    /**
     * Set the literal's value.
     */
    BooleanLit value(boolean value);
}
