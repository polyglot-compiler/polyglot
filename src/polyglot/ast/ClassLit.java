/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * A <code>ClassLit</code> represents a class literal expression. 
 * A class literal expressions is an expression consisting of the 
 * name of a class, interface, array, or primitive type followed by a period (.) 
 * and the token class. 
 */
public interface ClassLit extends Lit {
    TypeNode typeNode();
}
