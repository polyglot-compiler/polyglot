/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.TypeObject;
import polyglot.types.TypeObject_c;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public abstract class Key_c extends TypeObject_c implements Key {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String name;

    public Key_c(TypeSystem ts, Position pos, String name) {
        super(ts, pos);
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    public Key name(String name) {
        Key_c n = (Key_c) copy();
        n.name = name;
        return n;
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
        // return getClass().getName() + "(" + name + "@" + System.identityHashCode(this) + ")";
    }

    @Override
    public boolean isCanonical() {
        return true;
    }
}
