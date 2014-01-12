/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class InstKey_c extends Key_c implements InstKey {
    public InstKey_c(TypeSystem ts, Position pos, String name) {
        super(ts, pos, name);
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof InstKey) {
            return name.equals(((InstKey) o).name());
        }
        return false;
    }
}
