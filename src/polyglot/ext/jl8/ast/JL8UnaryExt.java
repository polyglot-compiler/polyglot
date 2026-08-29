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
package polyglot.ext.jl8.ast;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

/**
 * Incrementing or decrementing a local variable assigns to it, so a local
 * variable of an enclosing method may not be the operand of ++ or -- inside a
 * nested class body or lambda expression. See JL8LocalAssignExt.
 */
public class JL8UnaryExt extends JL8Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Unary n = (Unary) node();
        if (isIncrementOrDecrement(n.operator()) && n.expr() instanceof Local) {
            String name = ((Local) n.expr()).name();
            if (!tc.context().isLocal(name)) {
                throw new SemanticException(
                        "Local variable \""
                                + name
                                + "\" is declared outside the enclosing class body or lambda "
                                + "expression, so it cannot be assigned to here.",
                        n.position());
            }
        }
        return super.typeCheck(tc);
    }

    public static boolean isIncrementOrDecrement(Unary.Operator op) {
        return op == Unary.POST_INC
                || op == Unary.POST_DEC
                || op == Unary.PRE_INC
                || op == Unary.PRE_DEC;
    }
}
