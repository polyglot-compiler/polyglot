/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.types.CodeInstance;

/**
 * A code declaration.  A "code" is the supertype of methods,
 * constructors, and initalizers.
 */
public interface CodeDecl extends ClassMember
{
    /** The body of the method, constructor, or initializer. */
    Block body();

    /** Set the body. */
    CodeDecl body(Block body);
    
    /** The CodeInstance of the method, constructor, or initializer. */
    CodeInstance codeInstance();
}
