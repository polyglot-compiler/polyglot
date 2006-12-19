/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed shorts.
 */
public class Short extends Integer {
    public Short(short value) {
        super(value);
    }

    public short shortValue() {
        return (short)value;
    }
}
