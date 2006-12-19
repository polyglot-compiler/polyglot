/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An <code>ArrayAccess</code> is an immutable representation of an
 * access of an array member.
 */
public interface ArrayAccess extends Variable
{
    /**
     * Array to access.
     */
    Expr array();

    /**
     * Set the array to access.
     */
    ArrayAccess array(Expr array);

    /**
     * Index into the array.
     */
    Expr index();

    /**
     * Set the index into the array.
     */
    ArrayAccess index(Expr index);
}
