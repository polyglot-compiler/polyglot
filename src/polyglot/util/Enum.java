package jltools.util;

import java.io.Serializable;

// Enum cannot implement Serializable.  When saved and restored a different
// object is created which doesn't equal the original (by ==, which is what
// we want).
public class Enum // implements Serializable
{
    private final String name;

    protected Enum(String name) {
	this.name = name;
    }

    public boolean equals(Object o) {
	return this == o;
    }

    public int hashCode() {
	return name.hashCode();
    }

    public String toString() {
	return name;
    }
}
