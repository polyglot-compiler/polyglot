/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An integer literal: longs, ints, shorts, bytes, and chars.
 */
public interface NumLit extends Lit
{
    /** The literal's value. */
    long longValue();
}
