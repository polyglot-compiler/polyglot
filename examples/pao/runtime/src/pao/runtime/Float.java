/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed floats.
 */
public class Float extends Double {
    public Float(float value) {
        super(value);
    }

    public float floatValue() {
        return (float)value;
    }
}
