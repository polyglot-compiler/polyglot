/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A local class declaration statement.  The node is just a wrapper around
 * a class declaration.
 */
public interface LocalClassDecl extends CompoundStmt
{
    /** The class declaration. */
    ClassDecl decl();
}
