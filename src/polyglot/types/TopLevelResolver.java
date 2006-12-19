/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

public interface TopLevelResolver extends Resolver {
    /**
     * Check if a package exists.
     */
    public boolean packageExists(String name);
}
