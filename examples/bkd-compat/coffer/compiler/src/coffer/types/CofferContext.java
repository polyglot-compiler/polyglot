/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.Context;
import polyglot.types.SemanticException;

public interface CofferContext extends Context {
    void addKey(Key key);

    Key findKey(String name) throws SemanticException;
}
