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

import polyglot.util.Enum;

/**
 * A <code>Binary</code> represents a Java binary expression, an
 * immutable pair of expressions combined with an operator.
 */
public interface Binary extends Expr {
    /** Binary expression operator. */
    public static class Operator extends Enum {
        protected Precedence prec;

        public Operator(String name, Precedence prec) {
            super(name);
            this.prec = prec;
        }

        /** Returns the precedence of the operator. */
        public Precedence precedence() {
            return prec;
        }
    }

    public static final Operator GT = new Operator(">", Precedence.RELATIONAL);
    public static final Operator LT = new Operator("<", Precedence.RELATIONAL);
    public static final Operator EQ = new Operator("==", Precedence.EQUAL);
    public static final Operator LE = new Operator("<=", Precedence.RELATIONAL);
    public static final Operator GE = new Operator(">=", Precedence.RELATIONAL);
    public static final Operator NE = new Operator("!=", Precedence.EQUAL);
    public static final Operator COND_OR = new Operator("||",
                                                        Precedence.COND_OR);
    public static final Operator COND_AND = new Operator("&&",
                                                         Precedence.COND_AND);
    public static final Operator ADD = new Operator("+", Precedence.ADD);
    public static final Operator SUB = new Operator("-", Precedence.ADD);
    public static final Operator MUL = new Operator("*", Precedence.MUL);
    public static final Operator DIV = new Operator("/", Precedence.MUL);
    public static final Operator MOD = new Operator("%", Precedence.MUL);
    public static final Operator BIT_OR = new Operator("|", Precedence.BIT_OR);
    public static final Operator BIT_AND =
            new Operator("&", Precedence.BIT_AND);
    public static final Operator BIT_XOR =
            new Operator("^", Precedence.BIT_XOR);
    public static final Operator SHL = new Operator("<<", Precedence.SHIFT);
    public static final Operator SHR = new Operator(">>", Precedence.SHIFT);
    public static final Operator USHR = new Operator(">>>", Precedence.SHIFT);

    /**
     * Left child of the binary.
     */
    Expr left();

    /**
     * Set the left child of the binary.
     */
    Binary left(Expr left);

    /**
     * The binary's operator.
     */
    Operator operator();

    /**
     * Set the binary's operator.
     */
    Binary operator(Operator op);

    /**
     * Right child of the binary.
     */
    Expr right();

    /**
     * Set the right child of the binary.
     */
    Binary right(Expr right);

    /**
     * Returns true if the binary might throw an arithmetic exception,
     * such as division by zero.
     */
    boolean throwsArithmeticException();

    /**
     * Set the precedence of the expression.
     */
    Binary precedence(Precedence precedence);
}
