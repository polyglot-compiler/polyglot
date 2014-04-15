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
 * A {@code Unary} represents a Java unary expression, an
 * immutable pair of an expression and an an operator.
 */
public interface Unary extends Expr {
    /** Unary expression operator. */
    public static class Operator extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        protected boolean prefix;
        protected String name;

        public Operator(String name, boolean prefix) {
            super(name + (prefix ? "" : "post"));
            this.name = name;
            this.prefix = prefix;
        }

        /** Returns true of the operator is a prefix operator, false if
         * postfix. */
        public boolean isPrefix() {
            return prefix;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final Operator BIT_NOT = new Operator("~", true);
    public static final Operator NEG = new Operator("-", true);
    public static final Operator POST_INC = new Operator("++", false);
    public static final Operator POST_DEC = new Operator("--", false);
    public static final Operator PRE_INC = new Operator("++", true);
    public static final Operator PRE_DEC = new Operator("--", true);
    public static final Operator POS = new Operator("+", true);
    public static final Operator NOT = new Operator("!", true);

    /** The sub-expression on which to apply the operator. */
    Expr expr();

    /** Set the sub-expression on which to apply the operator. */
    Unary expr(Expr e);

    /** The operator to apply on the sub-expression. */
    Operator operator();

    /** Set the operator to apply on the sub-expression. */
    Unary operator(Operator o);
}
