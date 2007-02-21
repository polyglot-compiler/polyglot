/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A code declaration.  A "code" is the supertype of methods,
 * constructors, and initalizers.
 */
public interface CodeDecl extends CodeBlock, ClassMember
{
}
