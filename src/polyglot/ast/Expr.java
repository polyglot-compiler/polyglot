/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/**
 * An {@code Expr} represents some Java expression.  All expressions
 * must be subtypes of Expr.
 */
public interface Expr extends Receiver, Term {
    /**
     * Return an equivalent expression, but with the type {@code type}.
     */
    Expr type(Type type);

    /** Get the precedence of the expression. */
    Precedence precedence();

    /** Return true iff the compiler has determined whether this expression has a
     * constant value.  The value returned by {@code isConstant()} is valid only if
     * {@code constantValueSet()} is true. */
    @Deprecated
    boolean constantValueSet();

    /**
     * Return whether the expression evaluates to a constant.
     * Requires that disambiguation has been done, and that
     * {@code constantValueSet()} is true.
     */
    @Deprecated
    boolean isConstant();

    /** Return the constant value of the expression, if any.
     *  Requires that {@code isConstant()} is true.
     */
    @Deprecated
    Object constantValue();

    /**
     * Correctly parenthesize the subexpression {@code expr}
     * based on its precedence and the precedence of this expression.
     *
     * If the sub-expression has the same precedence as this expression,
     * we parenthesize if the sub-expression does not associate.  For example,
     * we parenthesize the right subexpression of a left-associative operator.
     *
     * @param expr The subexpression.
     * @param associative Whether expr is the left (right) child of a left-
     * (right-) associative operator.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    void printSubExpr(Expr expr, boolean associative, CodeWriter w,
            PrettyPrinter pp);

    /**
     * Correctly parenthesize the subexpression {@code expr}
     * based on its precedence and the precedence of this expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we do not parenthesize.
     *
     * This is equivalent to {@code printSubexpr(expr, true, w, pp)}
     *
     * @param expr The subexpression.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp);
}
