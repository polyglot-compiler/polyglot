/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A <code>LocalAssign</code> represents a Java assignment expression
 * to an array element.  For instance, <code>x = e</code>.
 * 
 * The class of the <code>Expr</code> returned by
 * <code>LocalAssign.left()</code>is guaranteed to be an <code>Local</code>.
 */
public interface LocalAssign extends Assign
{
}
