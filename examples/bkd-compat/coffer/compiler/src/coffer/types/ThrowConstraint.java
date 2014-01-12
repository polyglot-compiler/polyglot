/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.Type;
import polyglot.types.TypeObject;

public interface ThrowConstraint extends TypeObject {
    public KeySet keys();

    public void setKeys(KeySet keys);

    public Type throwType();

    public void setThrowType(Type throwType);
}
