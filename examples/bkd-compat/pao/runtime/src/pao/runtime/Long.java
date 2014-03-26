/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed longs.
 */
public class Long extends Primitive {
    protected long value;

    public Long(long value) {
        this.value = value;
    }

    public long longValue() {
        return value;
    }

    public int hashCode() {
        return (int)value;
    }

    public boolean equals(Object o) {
        if (o instanceof Long) {
            return ((Long)o).value == value;
        }
        if (o instanceof Double) {
            return ((Double)o).value == value;
        }
        return false;
    }

    public String toString() {
        return "" + value;
    }
}
