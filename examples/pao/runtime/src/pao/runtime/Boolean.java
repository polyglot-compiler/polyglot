/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed booleans.
 */
public class Boolean extends Primitive {
    private boolean value;

    public Boolean(boolean value) {
        this.value = value;
    }

    public boolean booleanValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Boolean) {
            return ((Boolean) o).value == value;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
