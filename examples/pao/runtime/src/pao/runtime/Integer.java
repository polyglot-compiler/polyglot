/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed ints.
 */
public class Integer extends Long {
    public Integer(int value) {
        super(value);
    }

    public int intValue() {
        return (int)value;
    }
}
