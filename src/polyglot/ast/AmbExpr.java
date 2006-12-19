/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public interface AmbExpr extends Expr, Ambiguous
{
    /**
     * Ambiguous name.
     */
    String name();
    AmbExpr name(String name);
}
