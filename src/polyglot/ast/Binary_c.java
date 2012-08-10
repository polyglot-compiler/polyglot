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

import java.util.Collections;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Binary</code> represents a Java binary expression, an
 * immutable pair of expressions combined with an operator.
 */
public class Binary_c extends Expr_c implements Binary {
    protected Expr left;
    protected Operator op;
    protected Expr right;
    protected Precedence precedence;

    public Binary_c(Position pos, Expr left, Operator op, Expr right) {
        super(pos);
        assert (left != null && op != null && right != null);
        this.left = left;
        this.op = op;
        this.right = right;
        this.precedence = op.precedence();
        if (op == ADD
                && (left instanceof StringLit || right instanceof StringLit)) {
            this.precedence = Precedence.STRING_ADD;
        }
    }

    /** Get the left operand of the expression. */
    @Override
    public Expr left() {
        return this.left;
    }

    /** Set the left operand of the expression. */
    @Override
    public Binary left(Expr left) {
        Binary_c n = (Binary_c) copy();
        n.left = left;
        return n;
    }

    /** Get the operator of the expression. */
    @Override
    public Operator operator() {
        return this.op;
    }

    /** Set the operator of the expression. */
    @Override
    public Binary operator(Operator op) {
        Binary_c n = (Binary_c) copy();
        n.op = op;
        return n;
    }

    /** Get the right operand of the expression. */
    @Override
    public Expr right() {
        return this.right;
    }

    /** Set the right operand of the expression. */
    @Override
    public Binary right(Expr right) {
        Binary_c n = (Binary_c) copy();
        n.right = right;
        return n;
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return this.precedence;
    }

    @Override
    public Binary precedence(Precedence precedence) {
        Binary_c n = (Binary_c) copy();
        n.precedence = precedence;
        return n;
    }

    /** Reconstruct the expression. */
    protected Binary_c reconstruct(Expr left, Expr right) {
        if (left != this.left || right != this.right) {
            Binary_c n = (Binary_c) copy();
            n.left = left;
            n.right = right;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr left = (Expr) visitChild(this.left, v);
        Expr right = (Expr) visitChild(this.right, v);
        return reconstruct(left, right);
    }

    @Override
    public boolean constantValueSet() {
        return left.constantValueSet() && right.constantValueSet();
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public Object constantValue() {
        if (!isConstant()) {
            return null;
        }

        Object lv = left.constantValue();
        Object rv = right.constantValue();

        if (op == ADD && (lv instanceof String || rv instanceof String)) {
            // toString() does what we want for String, Number, and Boolean
            if (lv == null) lv = "null";
            if (rv == null) rv = "null";
            return lv.toString() + rv.toString();
        }

        if (op == EQ && (lv instanceof String && rv instanceof String)) {
            return Boolean.valueOf(((String) lv).intern() == ((String) rv).intern());
        }

        if (op == NE && (lv instanceof String && rv instanceof String)) {
            return Boolean.valueOf(((String) lv).intern() != ((String) rv).intern());
        }

        // promote chars to ints.
        if (lv instanceof Character) {
            lv = new Integer(((Character) lv).charValue());
        }

        if (rv instanceof Character) {
            rv = new Integer(((Character) rv).charValue());
        }

        try {
            if (lv instanceof Number && rv instanceof Number) {
                if (lv instanceof Double || rv instanceof Double) {
                    double l = ((Number) lv).doubleValue();
                    double r = ((Number) rv).doubleValue();
                    if (op == ADD) return new Double(l + r);
                    if (op == SUB) return new Double(l - r);
                    if (op == MUL) return new Double(l * r);
                    if (op == DIV) return new Double(l / r);
                    if (op == MOD) return new Double(l % r);
                    if (op == EQ) return Boolean.valueOf(l == r);
                    if (op == NE) return Boolean.valueOf(l != r);
                    if (op == LT) return Boolean.valueOf(l < r);
                    if (op == LE) return Boolean.valueOf(l <= r);
                    if (op == GE) return Boolean.valueOf(l >= r);
                    if (op == GT) return Boolean.valueOf(l > r);
                    return null;
                }

                if (lv instanceof Float || rv instanceof Float) {
                    float l = ((Number) lv).floatValue();
                    float r = ((Number) rv).floatValue();
                    if (op == ADD) return new Float(l + r);
                    if (op == SUB) return new Float(l - r);
                    if (op == MUL) return new Float(l * r);
                    if (op == DIV) return new Float(l / r);
                    if (op == MOD) return new Float(l % r);
                    if (op == EQ) return Boolean.valueOf(l == r);
                    if (op == NE) return Boolean.valueOf(l != r);
                    if (op == LT) return Boolean.valueOf(l < r);
                    if (op == LE) return Boolean.valueOf(l <= r);
                    if (op == GE) return Boolean.valueOf(l >= r);
                    if (op == GT) return Boolean.valueOf(l > r);
                    return null;
                }

                if (lv instanceof Long && rv instanceof Number) {
                    long l = ((Long) lv).longValue();
                    long r = ((Number) rv).longValue();
                    if (op == SHL) return new Long(l << r);
                    if (op == SHR) return new Long(l >> r);
                    if (op == USHR) return new Long(l >>> r);
                }

                if (lv instanceof Long || rv instanceof Long) {
                    long l = ((Number) lv).longValue();
                    long r = ((Number) rv).longValue();
                    if (op == ADD) return new Long(l + r);
                    if (op == SUB) return new Long(l - r);
                    if (op == MUL) return new Long(l * r);
                    if (op == DIV) return new Long(l / r);
                    if (op == MOD) return new Long(l % r);
                    if (op == EQ) return Boolean.valueOf(l == r);
                    if (op == NE) return Boolean.valueOf(l != r);
                    if (op == LT) return Boolean.valueOf(l < r);
                    if (op == LE) return Boolean.valueOf(l <= r);
                    if (op == GE) return Boolean.valueOf(l >= r);
                    if (op == GT) return Boolean.valueOf(l > r);
                    if (op == BIT_AND) return new Long(l & r);
                    if (op == BIT_OR) return new Long(l | r);
                    if (op == BIT_XOR) return new Long(l ^ r);
                    return null;
                }

                // At this point, both lv and rv must be ints.
                int l = ((Number) lv).intValue();
                int r = ((Number) rv).intValue();

                if (op == ADD) return new Integer(l + r);
                if (op == SUB) return new Integer(l - r);
                if (op == MUL) return new Integer(l * r);
                if (op == DIV) return new Integer(l / r);
                if (op == MOD) return new Integer(l % r);
                if (op == EQ) return Boolean.valueOf(l == r);
                if (op == NE) return Boolean.valueOf(l != r);
                if (op == LT) return Boolean.valueOf(l < r);
                if (op == LE) return Boolean.valueOf(l <= r);
                if (op == GE) return Boolean.valueOf(l >= r);
                if (op == GT) return Boolean.valueOf(l > r);
                if (op == BIT_AND) return new Integer(l & r);
                if (op == BIT_OR) return new Integer(l | r);
                if (op == BIT_XOR) return new Integer(l ^ r);
                if (op == SHL) return new Integer(l << r);
                if (op == SHR) return new Integer(l >> r);
                if (op == USHR) return new Integer(l >>> r);
                return null;
            }
        }
        catch (ArithmeticException e) {
            // ignore div by 0
            return null;
        }

        if (lv instanceof Boolean && rv instanceof Boolean) {
            boolean l = ((Boolean) lv).booleanValue();
            boolean r = ((Boolean) rv).booleanValue();

            if (op == EQ) return Boolean.valueOf(l == r);
            if (op == NE) return Boolean.valueOf(l != r);
            if (op == BIT_AND) return Boolean.valueOf(l & r);
            if (op == BIT_OR) return Boolean.valueOf(l | r);
            if (op == BIT_XOR) return Boolean.valueOf(l ^ r);
            if (op == COND_AND) return Boolean.valueOf(l && r);
            if (op == COND_OR) return Boolean.valueOf(l || r);
        }

        return null;
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type l = left.type();
        Type r = right.type();

        TypeSystem ts = tc.typeSystem();

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

            return type(ts.Boolean());
        }

        if (op == EQ || op == NE) {
            if (!ts.isCastValid(l, r) && !ts.isCastValid(r, l)) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have operands of similar type.",
                                            position());
            }

            return type(ts.Boolean());
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

            return type(ts.Boolean());
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

                return precedence(Precedence.STRING_ADD).type(ts.String());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (l.isBoolean() && r.isBoolean()) {
                return type(ts.Boolean());
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
            return type(ts.promote(l));
        }

        return type(ts.promote(l, r));
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
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

        TypeSystem ts = av.typeSystem();

        try {
            if (op == EQ || op == NE) {
                // Coercion to compatible types.
                if ((child.type().isReference() || child.type().isNull())
                        && (other.type().isReference() || other.type().isNull())) {
                    return ts.leastCommonAncestor(child.type(), other.type());
                }

                if (child.type().isBoolean() && other.type().isBoolean()) {
                    return ts.Boolean();
                }

                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }

                if (child.type().isImplicitCastValid(other.type())) {
                    return other.type();
                }

                return child.type();
            }

            if (op == ADD && ts.typeEquals(type, ts.String())) {
                // Implicit coercion to String. 
                return ts.String();
            }

            if (op == GT || op == LT || op == GE || op == LE) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }

                return child.type();
            }

            if (op == COND_OR || op == COND_AND) {
                return ts.Boolean();
            }

            if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
                if (other.type().isBoolean()) {
                    return ts.Boolean();
                }

                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }

                return child.type();
            }

            if (op == ADD || op == SUB || op == MUL || op == DIV || op == MOD) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    Type t = ts.promote(child.type(), other.type());

                    if (ts.isImplicitCastValid(t, av.toType())) {
                        return t;
                    }
                    else {
                        return av.toType();
                    }
                }

                return child.type();
            }

            if (op == SHL || op == SHR || op == USHR) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    if (child == left) {
                        Type t = ts.promote(child.type());

                        if (ts.isImplicitCastValid(t, av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                    else {
                        return ts.promote(child.type());
                    }
                }

                return child.type();
            }

            return child.type();
        }
        catch (SemanticException e) {
        }

        return child.type();
    }

    /** Get the throwsArithmeticException of the expression. */
    @Override
    public boolean throwsArithmeticException() {
        // conservatively assume that any division or mod may throw
        // ArithmeticException this is NOT true-- floats and doubles don't
        // throw any exceptions ever...
        return op == DIV || op == MOD;
    }

    @Override
    public String toString() {
        return left + " " + op + " " + right;
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printSubExpr(left, true, w, tr);
        w.write(" ");
        w.write(op.toString());
        w.allowBreak(type() == null || type().isPrimitive() ? 2 : 0, " ");
        printSubExpr(right, false, w, tr);
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (type != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(type " + type + ")");
            w.end();
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(operator " + op + ")");
        w.end();
    }

    @Override
    public Term firstChild() {
        return left;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (op == COND_AND || op == COND_OR) {
            // short-circuit
            if (left instanceof BooleanLit) {
                BooleanLit b = (BooleanLit) left;
                if ((b.value() && op == COND_OR)
                        || (!b.value() && op == COND_AND)) {
                    v.visitCFG(left, this, EXIT);
                }
                else {
                    v.visitCFG(left, right, ENTRY);
                    v.visitCFG(right, this, EXIT);
                }
            }
            else {
                if (op == COND_AND) {
                    // AND operator
                    // short circuit means that left is false
                    v.visitCFG(left,
                               FlowGraph.EDGE_KEY_TRUE,
                               right,
                               ENTRY,
                               FlowGraph.EDGE_KEY_FALSE,
                               this,
                               EXIT);
                }
                else {
                    // OR operator
                    // short circuit means that left is true
                    v.visitCFG(left,
                               FlowGraph.EDGE_KEY_FALSE,
                               right,
                               ENTRY,
                               FlowGraph.EDGE_KEY_TRUE,
                               this,
                               EXIT);
                }
                v.visitCFG(right,
                           FlowGraph.EDGE_KEY_TRUE,
                           this,
                           EXIT,
                           FlowGraph.EDGE_KEY_FALSE,
                           this,
                           EXIT);
            }
        }
        else {
            if (left.type().isBoolean() && right.type().isBoolean()) {
                v.visitCFG(left,
                           FlowGraph.EDGE_KEY_TRUE,
                           right,
                           ENTRY,
                           FlowGraph.EDGE_KEY_FALSE,
                           right,
                           ENTRY);
                v.visitCFG(right,
                           FlowGraph.EDGE_KEY_TRUE,
                           this,
                           EXIT,
                           FlowGraph.EDGE_KEY_FALSE,
                           this,
                           EXIT);
            }
            else {
                v.visitCFG(left, right, ENTRY);
                v.visitCFG(right, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        if (throwsArithmeticException()) {
            return Collections.singletonList((Type) ts.ArithmeticException());
        }

        return Collections.<Type> emptyList();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Binary(this.position, this.left, this.op, this.right);
    }

}
