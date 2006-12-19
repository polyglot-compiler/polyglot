/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An immutable representation of a Java language <code>synchronized</code>
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is <code>true</code>.
 */
public interface Synchronized extends CompoundStmt
{
    /** The expression to lock. */
    Expr expr();

    /** Set the expression to lock. */
    Synchronized expr(Expr expr);

    /** The body in that the lock is held. */
    Block body();

    /** Set the body in that the lock is held. */
    Synchronized body(Block body);
}
