/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.ext.param.types.InstType;
import polyglot.types.ClassType;

public interface CofferClassType extends ClassType, InstType<Key, Key> {
    Key key();
}
