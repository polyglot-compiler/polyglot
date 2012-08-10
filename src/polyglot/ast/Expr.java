/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/**
 * An <code>Expr</code> represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public interface Expr extends Receiver, Term {
    /**
     * Return an equivalent expression, but with the type <code>type</code>.
     */
    Expr type(Type type);

    /** Get the precedence of the expression. */
    Precedence precedence();

    boolean constantValueSet();

    /**
     * Return whether the expression evaluates to a constant.
     * This is not valid until after disambiguation.
     */
    boolean isConstant();

    /** Returns the constant value of the expression, if any. */
    Object constantValue();

    /**
     * Correctly parenthesize the subexpression <code>expr<code>
     * based on its precedence and the precedence of this expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we parenthesize if the sub-expression does not associate; e.g.,
     * we parenthesis the right sub-expression of a left-associative
     * operator.
     */
    void printSubExpr(Expr expr, boolean associative, CodeWriter w,
            PrettyPrinter pp);

    /**
     * Correctly parenthesize the subexpression <code>expr<code>
     * based on its precedence and the precedence of this expression.
     *
     * This is equivalent to <code>printSubexpr(expr, true, w, pp)</code>
     */
    void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp);
}
