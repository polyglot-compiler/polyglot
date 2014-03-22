/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.Type;
import polyglot.types.TypeObject_c;
import polyglot.util.Position;

public class ThrowConstraint_c extends TypeObject_c implements ThrowConstraint {
    Type throwType;
    KeySet keys;

    public ThrowConstraint_c(CofferTypeSystem ts, Position pos, Type throwType,
            KeySet keys) {
        super(ts, pos);
        this.throwType = throwType;
        this.keys = keys;
    }

    @Override
    public KeySet keys() {
        return keys;
    }

    @Override
    public void setKeys(KeySet keys) {
        this.keys = keys;
    }

    @Override
    public Type throwType() {
        return throwType;
    }

    @Override
    public void setThrowType(Type throwType) {
        this.throwType = throwType;
    }

    @Override
    public boolean isCanonical() {
        return (keys == null || keys.isCanonical()) && throwType.isCanonical();
    }

    @Override
    public String toString() {
        return throwType.toString() + (keys == null ? "" : keys.toString());
    }
}
