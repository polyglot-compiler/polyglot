/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class UnknownKey_c extends Key_c implements UnknownKey {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public UnknownKey_c(TypeSystem ts, Position pos, String name) {
        super(ts, pos, name);
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public boolean isCanonical() {
        return false;
    }
}
