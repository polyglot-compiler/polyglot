/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

import polyglot.ast.*;

/**
 * A <code>Resolver</code> is responsible for looking up types and
 * packages by name.
 */
public interface Resolver {

    /**
     * Find a type object by name.
     */
    public Named find(String name) throws SemanticException;
}
