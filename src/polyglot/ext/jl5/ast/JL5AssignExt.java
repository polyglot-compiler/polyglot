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

import polyglot.ast.Assign;
import polyglot.ast.Assign.Operator;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Variable;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.TypeChecker;

public class JL5AssignExt extends JL5ExprExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Assign a = (Assign) this.node();
        Expr left = a.left();
        Expr right = a.right();
        Operator op = a.operator();
        if (child == left) {
            return child.type();
        }

        // See JLS 2nd ed. 15.26.2
        TypeSystem ts = av.typeSystem();
        if (op == Assign.ASSIGN) {
            return left.type();
        }
        if (op == Assign.ADD_ASSIGN) {
            if (ts.typeEquals(ts.String(), left.type())) {
                return child.type();
            }
        }
        if (op == Assign.ADD_ASSIGN || op == Assign.SUB_ASSIGN
                || op == Assign.MUL_ASSIGN || op == Assign.DIV_ASSIGN
                || op == Assign.MOD_ASSIGN || op == Assign.SHL_ASSIGN
                || op == Assign.SHR_ASSIGN || op == Assign.USHR_ASSIGN) {
            if (isNumeric(left.type()) && isNumeric(right.type())) {
                try {
                    return ts.promote(numericType(left.type()),
                                      numericType(child.type()));
                }
                catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
            // Assume the typechecker knew what it was doing
            return child.type();
        }
        if (op == Assign.BIT_AND_ASSIGN || op == Assign.BIT_OR_ASSIGN
                || op == Assign.BIT_XOR_ASSIGN) {
            if (left.type().isBoolean()) {
                return ts.Boolean();
            }
            if (isNumeric(left.type()) && isNumeric(right.type())) {
                try {
                    return ts.promote(numericType(left.type()),
                                      numericType(child.type()));
                }
                catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
            // Assume the typechecker knew what it was doing
            return child.type();
        }

        throw new InternalCompilerError("Unrecognized assignment operator "
                + op + ".");
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Assign a = (Assign) this.node();
        Type t = a.left().type();
        Type s = a.right().type();

        TypeSystem ts = tc.typeSystem();

        if (!(a.left() instanceof Variable)) {
            throw new SemanticException("Target of assignment must be a variable.",
                                        a.position());
        }

        if (a.operator() == Assign.ASSIGN) {
            if (!ts.isImplicitCastValid(s, t)
                    && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t,
                                                  tc.lang()
                                                    .constantValue(a.right(),
                                                                   tc.lang()))) {

                throw new SemanticException("Cannot assign " + s + " to " + t
                        + ".", a.position());
            }

            return a.type(t);
        }

        if (a.operator() == Assign.ADD_ASSIGN) {
            // t += s
            if (ts.typeEquals(t, ts.String())
                    && ts.canCoerceToString(s, tc.context())) {
                return a.type(ts.String());
            }

            if (isNumeric(t) && isNumeric(s)) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "numeric or String operands.",
                                        a.position());
        }

        if (a.operator() == Assign.SUB_ASSIGN
                || a.operator() == Assign.MUL_ASSIGN
                || a.operator() == Assign.DIV_ASSIGN
                || a.operator() == Assign.MOD_ASSIGN) {
            if (isNumeric(t) && isNumeric(s)) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "numeric operands.",
                                        a.position());
        }

        if (a.operator() == Assign.BIT_AND_ASSIGN
                || a.operator() == Assign.BIT_OR_ASSIGN
                || a.operator() == Assign.BIT_XOR_ASSIGN) {
            if (isBoolean(t) && isBoolean(s)) {
                return a.type(ts.Boolean());
            }

            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                return a.type(ts.promote(numericType(t), numericType(s)));
            }

            throw new SemanticException("The "
                                                + a.operator()
                                                + " operator must have "
                                                + "integral or boolean operands.",
                                        a.position());
        }

        if (a.operator() == Assign.SHL_ASSIGN
                || a.operator() == Assign.SHR_ASSIGN
                || a.operator() == Assign.USHR_ASSIGN) {
            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                // Only promote the left of a shift.
                return a.type(ts.promote(numericType(t)));
            }

            throw new SemanticException("The " + a.operator()
                                                + " operator must have "
                                                + "integral operands.",
                                        a.position());
        }

        throw new InternalCompilerError("Unrecognized assignment operator "
                + a.operator() + ".");
    }

    public boolean isNumeric(Type t) {
        if (t.isNumeric()) return true;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t).isNumeric();
        }
        return false;
    }

    public boolean isBoolean(Type t) {
        if (t.isBoolean()) return true;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t).isBoolean();
        }
        return false;
    }

    public Type numericType(Type t) {
        if (t.isNumeric()) return t;
        JL5TypeSystem ts = (JL5TypeSystem) t.typeSystem();

        if (ts.isPrimitiveWrapper(t)) {
            return ts.primitiveTypeOfWrapper(t);
        }
        return t;
    }

}
