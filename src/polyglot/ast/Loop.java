/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public interface Loop extends CompoundStmt 
{    
    /** Loop condition */
    Expr cond();

    /** Returns true of cond() evaluates to a constant. */
    boolean condIsConstant();

    /** Returns true if cond() is a constant that evaluates to true. */
    boolean condIsConstantTrue();

    /** Loop body. */
    Stmt body();

    /** Target of a continue statement in the loop body. */
    Term continueTarget();
}
