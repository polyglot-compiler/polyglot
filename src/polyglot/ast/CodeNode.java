/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.types.CodeInstance;

/**
 * A code node.  A "code" is the supertype of methods,
 * constructors, and initalizers.
 */
public interface CodeNode extends Term
{
    /** The body of the method, constructor, initializer, or field initializer. */
    Term codeBody();

    /** The CodeInstance of the method, constructor, or initializer. */
    CodeInstance codeInstance();
}
