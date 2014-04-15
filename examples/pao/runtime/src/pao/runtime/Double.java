/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.runtime;

/**
 * Boxed doubles.
 */
public class Double extends Primitive {
    protected double value;

    public Double(double value) {
        this.value = value;
    }

    public double doubleValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Double) {
            return ((Double) o).value == value;
        }
        if (o instanceof Long) {
            return ((Long) o).value == value;
        }
        return false;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
