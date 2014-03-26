/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.ext.param.types.Param;
import polyglot.types.TypeObject;

public interface Key extends TypeObject, Param {
    String name();
}
