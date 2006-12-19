/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/** 
 * An <code>CharLit</code> represents a literal in java of
 * <code>char</code> type.
 */
public interface CharLit extends NumLit
{    
    /**
     * The literal's value.
     */
    char value();

    /**
     * Set the literal's value.
     */
    CharLit value(char value);
}
