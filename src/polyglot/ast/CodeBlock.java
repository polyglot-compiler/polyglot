/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 2007 IBM Corporation
 * 
 */

package polyglot.ast;


/**
 * A code node.  A "code" is the supertype of methods,
 * constructors, and initalizers.
 */
public interface CodeBlock extends CodeNode
{
    /** The body of the method, constructor, or initializer. */
    Block body();

    /** Set the body. */
    CodeBlock body(Block body);
}
