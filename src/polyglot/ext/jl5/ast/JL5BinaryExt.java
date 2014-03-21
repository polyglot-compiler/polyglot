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

import static polyglot.ast.Binary.ADD;
import static polyglot.ast.Binary.BIT_AND;
import static polyglot.ast.Binary.BIT_OR;
import static polyglot.ast.Binary.BIT_XOR;
import static polyglot.ast.Binary.COND_AND;
import static polyglot.ast.Binary.COND_OR;
import static polyglot.ast.Binary.DIV;
import static polyglot.ast.Binary.EQ;
import static polyglot.ast.Binary.GE;
import static polyglot.ast.Binary.GT;
import static polyglot.ast.Binary.LE;
import static polyglot.ast.Binary.LT;
import static polyglot.ast.Binary.MOD;
import static polyglot.ast.Binary.MUL;
import static polyglot.ast.Binary.NE;
import static polyglot.ast.Binary.SHL;
import static polyglot.ast.Binary.SHR;
import static polyglot.ast.Binary.SUB;
import static polyglot.ast.Binary.USHR;
import polyglot.ast.Binary;
import polyglot.ast.Binary.Operator;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.TypeChecker;

public class JL5BinaryExt extends JL5ExprExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Binary b = (Binary) this.node();

        Operator op = b.operator();
        Expr left = b.left();
        Expr right = b.right();

        Type l = left.type();
        Type r = right.type();

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!ts.isPrimitiveWrapper(l) && !ts.isPrimitiveWrapper(r)) {
            return superLang().typeCheck(this.node(), tc);
        }
        // at least one of l or r is a primitive wrapper
        // If both of them are non null, then just use their primitive types
        if (!l.isNull() && !r.isNull()) {
            l = ts.isPrimitiveWrapper(l) ? ts.primitiveTypeOfWrapper(l) : l;
            r = ts.isPrimitiveWrapper(r) ? ts.primitiveTypeOfWrapper(r) : r;
        }

        if (op == GT || op == LT || op == GE || op == LE) {
            if (!l.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }

            return b.type(ts.Boolean());
        }

        if (op == EQ || op == NE) {
            if (!ts.isCastValid(l, r) && !ts.isCastValid(r, l)) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have operands of similar type.",
                                            b.position());
            }

            return b.type(ts.Boolean());
        }

        if (op == COND_OR || op == COND_AND) {
            if (!l.isBoolean()) {
                throw new SemanticException("The " + op
                        + " operator must have boolean operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isBoolean()) {
                throw new SemanticException("The " + op
                        + " operator must have boolean operands, not type " + r
                        + ".", right.position());
            }

            return b.type(ts.Boolean());
        }

        if (op == ADD) {
            if (ts.isSubtype(l, ts.String()) || ts.isSubtype(r, ts.String())) {
                if (!ts.canCoerceToString(r, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression "
                                                        + "of type " + r
                                                        + " to a String.",
                                                right.position());
                }
                if (!ts.canCoerceToString(l, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression "
                            + "of type " + l + " to a String.", left.position());
                }

                return b.precedence(Precedence.STRING_ADD).type(ts.String());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (l.isBoolean() && r.isBoolean()) {
                return b.type(ts.Boolean());
            }
        }

        if (op == ADD) {
            if (!l.isNumeric()) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or String operands, not type "
                                                    + l + ".",
                                            left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or String operands, not type "
                                                    + r + ".",
                                            right.position());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (!ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or boolean operands, not type "
                                                    + l + ".",
                                            left.position());
            }

            if (!ts.isImplicitCastValid(r, ts.Long())) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or boolean operands, not type "
                                                    + r + ".",
                                            right.position());
            }
        }

        if (op == SUB || op == MUL || op == DIV || op == MOD) {
            if (!l.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }
        }

        if (op == SHL || op == SHR || op == USHR) {
            if (!ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!ts.isImplicitCastValid(r, ts.Long())) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }
        }

        if (op == SHL || op == SHR || op == USHR) {
            // For shift, only promote the left operand.
            return b.type(ts.promote(l));
        }

        return b.type(ts.promote(l, r));
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Binary b = (Binary) this.node();
        Operator op = b.operator();

        Expr left = b.left();
        Expr right = b.right();

        Expr other;

        if (child == left) {
            other = right;
        }
        else if (child == right) {
            other = left;
        }
        else {
            return child.type();
        }

        JL5TypeSystem ts = (JL5TypeSystem) av.typeSystem();

        Type childType = child.type();
        Type otherType = other.type();

        if (!ts.isPrimitiveWrapper(childType)
                && !ts.isPrimitiveWrapper(otherType)) {
            return superLang().childExpectedType(this.node(), child, av);
        }

        Type childUnboxedType = ts.unboxingConversion(childType);
        Type otherUnboxedType = ts.unboxingConversion(otherType);
        try {
            if (op == EQ || op == NE) {
                // Coercion to compatible types.
                if ((childType.isReference() || childType.isNull())
                        && (otherType.isReference() || otherType.isNull())) {
                    return ts.leastCommonAncestor(childType, otherType);
                }

                if (childUnboxedType.isBoolean()
                        && otherUnboxedType.isBoolean()) {
                    return ts.Boolean();
                }

                if (childUnboxedType.isNumeric()
                        && otherUnboxedType.isNumeric()) {
                    return ts.promote(childUnboxedType, otherUnboxedType);
                }

                if (childType.isImplicitCastValid(otherType)) {
                    return otherType;
                }

                return childType;
            }

            if (op == ADD && ts.typeEquals(b.type(), ts.String())) {
                // Implicit coercion to String.
                return ts.String();
            }

            if (op == GT || op == LT || op == GE || op == LE) {
                if (childUnboxedType.isNumeric()
                        && otherUnboxedType.isNumeric()) {
                    return ts.promote(childUnboxedType, otherUnboxedType);
                }

                return childType;
            }

            if (op == COND_OR || op == COND_AND) {
                return ts.Boolean();
            }

            if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
                if (otherUnboxedType.isBoolean()) {
                    return ts.Boolean();
                }

                if (childUnboxedType.isNumeric()
                        && otherUnboxedType.isNumeric()) {
                    return ts.promote(childUnboxedType, otherUnboxedType);
                }

                return childType;
            }

            if (op == ADD || op == SUB || op == MUL || op == DIV || op == MOD) {
                if (childUnboxedType.isNumeric()
                        && otherUnboxedType.isNumeric()) {
                    Type t = ts.promote(childUnboxedType, otherUnboxedType);
                    if (ts.isImplicitCastValid(t, av.toType())
                            || ts.String().equals(av.toType())) {
                        return t;
                    }
                    else {
                        return av.toType();
                    }
                }

                return childType;
            }

            if (op == SHL || op == SHR || op == USHR) {
                if (childUnboxedType.isNumeric()
                        && otherUnboxedType.isNumeric()) {
                    if (child == left) {
                        Type t = ts.promote(childUnboxedType);

                        if (ts.isImplicitCastValid(t, av.toType())
                                || ts.String().equals(av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                    else {
                        return ts.promote(childUnboxedType);
                    }
                }

                return childType;
            }

            return childType;
        }
        catch (SemanticException e) {
        }

        return childType;
    }

}
