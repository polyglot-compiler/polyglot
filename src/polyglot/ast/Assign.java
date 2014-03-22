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

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

/**
 * An {@code Assign} represents a Java assignment expression.
 */
public interface Assign extends Expr {
    /** Assignment operator. */
    public static class Operator extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        private final Binary.Operator binOp;

        public Operator(String name, Binary.Operator binOp) {
            super(name);
            this.binOp = binOp;
        }

        public Binary.Operator binaryOperator() {
            return binOp;
        }
    }

    public static final Operator ASSIGN = new Operator("=", null);
    public static final Operator ADD_ASSIGN = new Operator("+=", Binary.ADD);
    public static final Operator SUB_ASSIGN = new Operator("-=", Binary.SUB);
    public static final Operator MUL_ASSIGN = new Operator("*=", Binary.MUL);
    public static final Operator DIV_ASSIGN = new Operator("/=", Binary.DIV);
    public static final Operator MOD_ASSIGN = new Operator("%=", Binary.MOD);
    public static final Operator BIT_AND_ASSIGN = new Operator("&=",
                                                               Binary.BIT_AND);
    public static final Operator BIT_OR_ASSIGN = new Operator("|=",
                                                              Binary.BIT_OR);
    public static final Operator BIT_XOR_ASSIGN = new Operator("^=",
                                                               Binary.BIT_XOR);
    public static final Operator SHL_ASSIGN = new Operator("<<=", Binary.SHL);
    public static final Operator SHR_ASSIGN = new Operator(">>=", Binary.SHR);
    public static final Operator USHR_ASSIGN =
            new Operator(">>>=", Binary.USHR);

    /**
     * Left child (target) of the assignment.
     * The target must be a Variable, but this is not enforced
     * statically to keep Polyglot backward compatible.
     */
    Expr left();

    /**
     * Set the left child (target) of the assignment.
     * The target must be a Variable, but this is not enforced
     * statically to keep Polyglot backward compatible.
     */
    Assign left(Expr left);

    /**
     * The assignment's operator.
     */
    Operator operator();

    /**
     * Set the assignment's operator.
     */
    Assign operator(Operator op);

    /**
     * Right child (source) of the assignment.
     */
    Expr right();

    /**
     * Set the right child (source) of the assignment.
     */
    Assign right(Expr right);

    /** Get the throwsArithmeticException of the expression. */
    boolean throwsArithmeticException();
}
