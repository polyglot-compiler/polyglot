/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

public class ThrowConstraint_c extends TypeObject_c implements ThrowConstraint {
    Type throwType;
    KeySet keys;

    public ThrowConstraint_c(CofferTypeSystem ts, Position pos,
                             Type throwType, KeySet keys) {
        super(ts, pos);
        this.throwType = throwType;
        this.keys = keys;
    }

    public KeySet keys() {
        return keys;
    }

    public void setKeys(KeySet keys) {
        this.keys = keys;
    }

    public Type throwType() {
        return throwType;
    }

    public void setThrowType(Type throwType) {
        this.throwType = throwType;
    }

    public boolean isCanonical() {
        return (keys == null || keys.isCanonical()) && throwType.isCanonical();
    }

    public String toString() {
        return throwType.toString() + (keys == null ? "" : keys.toString());
    }
}
