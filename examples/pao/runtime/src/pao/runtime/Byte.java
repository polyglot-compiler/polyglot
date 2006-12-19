/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed bytes.
 */
public class Byte extends Integer {
    public Byte(byte value) {
        super(value);
    }

    public byte byteValue() {
        return (byte)value;
    }
}
