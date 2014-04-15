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
package polyglot.ext.jl5.ast;

import static polyglot.ast.Unary.BIT_NOT;
import static polyglot.ast.Unary.NEG;
import static polyglot.ast.Unary.NOT;
import static polyglot.ast.Unary.POS;
import static polyglot.ast.Unary.POST_DEC;
import static polyglot.ast.Unary.POST_INC;
import static polyglot.ast.Unary.PRE_DEC;
import static polyglot.ast.Unary.PRE_INC;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.ast.Unary.Operator;
import polyglot.ast.Variable;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.TypeChecker;

public class JL5UnaryExt extends JL5ExprExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        Unary u = (Unary) this.node();
        Operator op = u.operator();
        Expr expr = u.expr();

        if (!ts.isPrimitiveWrapper(expr.type())) {
            return superLang().typeCheck(this.node(), tc);
        }

        if (op == POST_INC || op == POST_DEC || op == PRE_INC || op == PRE_DEC) {

            if (!expr.type().isNumeric()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isNumeric()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be numeric.", expr.position());
                }
            }

            if (!(expr instanceof Variable)) {
                throw new SemanticException("Operand of " + op
                        + " operator must be a variable.", expr.position());
            }

            if (((Variable) expr).flags().isFinal()) {
                throw new SemanticException("Operand of "
                                                    + op
                                                    + " operator must be a non-final variable.",
                                            expr.position());
            }

            return u.type(expr.type());
        }

        if (op == BIT_NOT) {
            if (!ts.isImplicitCastValid(expr.type(), ts.Long())) {
                throw new SemanticException("Operand of " + op
                        + " operator must be numeric.", expr.position());
            }

            if (ts.isPrimitiveWrapper(expr.type())) {
                return u.type(ts.promote(ts.primitiveTypeOfWrapper(expr.type())));
            }
            else {
                return u.type(ts.promote(expr.type()));
            }
        }

        if (op == NEG || op == POS) {
            if (!expr.type().isNumeric()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isNumeric()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be numeric.", expr.position());
                }
            }

            if (ts.isPrimitiveWrapper(expr.type())) {
                return u.type(ts.promote(ts.primitiveTypeOfWrapper(expr.type())));
            }
            else {
                return u.type(ts.promote(expr.type()));
            }
        }

        if (op == NOT) {
            if (!expr.type().isBoolean()) {
                if (!(ts.isPrimitiveWrapper(expr.type()) && (ts.primitiveTypeOfWrapper(expr.type()).isBoolean()))) {
                    throw new SemanticException("Operand of " + op
                            + " operator must be boolean.", expr.position());
                }
            }

            return u.type(ts.Boolean());
        }

        return u;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        JL5TypeSystem ts = (JL5TypeSystem) av.typeSystem();

        Unary u = (Unary) this.node();
        Operator op = u.operator();
        Expr expr = u.expr();

        try {
            if (child == expr) {
                Type childType = child.type();
                if (ts.isPrimitiveWrapper(childType))
                    childType = ts.primitiveTypeOfWrapper(childType);
                if (op == POST_INC || op == POST_DEC || op == PRE_INC
                        || op == PRE_DEC) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NEG || op == POS) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == BIT_NOT) {
                    if (ts.isImplicitCastValid(childType, av.toType())) {
                        return ts.promote(childType);
                    }
                    else {
                        return av.toType();
                    }
                }
                else if (op == NOT) {
                    return ts.Boolean();
                }
            }
        }
        catch (SemanticException e) {
        }

        return child.type();
    }

}
