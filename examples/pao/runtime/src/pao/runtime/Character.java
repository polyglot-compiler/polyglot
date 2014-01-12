/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed chars.
 */
public class Character extends Integer {
    public Character(char value) {
        super(value);
    }

    public char charValue() {
        return (char) value;
    }

    @Override
    public String toString() {
        return "" + (char) value;
    }
}
