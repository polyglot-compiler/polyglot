/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * ClassResolver
 *
 * Overview:
 *    A ClassResolver is responsible for taking in the name of a class and
 *    returning a ClassType corresponding to that name.  
 * 
 *    Differing concrete implementations of ClassResolver may obey
 *    slightly different contracts in terms of which names they
 *    accept; it is the responsibility of the user to make sure they
 *    have one whose behavior is reasonable.
 */
public abstract class AbstractAccessControlResolver implements AccessControlResolver {
    protected TypeSystem ts;
    
    public AbstractAccessControlResolver(TypeSystem ts) {
        this.ts = ts;
    }
    
    public final Named find(String name) throws SemanticException {
        return find(name, null);
    }
}
