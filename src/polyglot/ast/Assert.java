/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An immutable representation of a <code>assert</code> statement.
 */
public interface Assert extends Stmt
{
    /** The condition to check. */
    Expr cond();

    /** Set the condition to check. */
    Assert cond(Expr cond);

    /** The error message expression, or null. */
    Expr errorMessage();

    /** Set the error message expression. */
    Assert errorMessage(Expr errorMessage);
}
