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

import java.util.LinkedList;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 */
public abstract class Assign_c extends Expr_c implements Assign {
    protected Expr left;
    protected Operator op;
    protected Expr right;

    public Assign_c(Position pos, Expr left, Operator op, Expr right) {
        super(pos);
        assert (left != null && op != null && right != null);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.ASSIGN;
    }

    /** Get the left operand of the expression. */
    @Override
    public Expr left() {
        return this.left;
    }

    /** Set the left operand of the expression. */
    @Override
    public Assign left(Expr left) {
        Assign_c n = (Assign_c) copy();
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
    public Assign operator(Operator op) {
        Assign_c n = (Assign_c) copy();
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
    public Assign right(Expr right) {
        Assign_c n = (Assign_c) copy();
        n.right = right;
        return n;
    }

    /** Reconstruct the expression. */
    protected Assign_c reconstruct(Expr left, Expr right) {
        if (left != this.left || right != this.right) {
            Assign_c n = (Assign_c) copy();
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

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type t = left.type();
        Type s = right.type();

        TypeSystem ts = tc.typeSystem();

        if (!(left instanceof Variable)) {
            throw new SemanticException("Target of assignment must be a variable.",
                                        position());
        }

        if (op == ASSIGN) {
            if (!ts.isImplicitCastValid(s, t) && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t, right.constantValue())) {

                throw new SemanticException("Cannot assign " + s + " to " + t
                        + ".", position());
            }

            return type(t);
        }

        if (op == ADD_ASSIGN) {
            // t += s
            if (ts.typeEquals(t, ts.String())
                    && ts.canCoerceToString(s, tc.context())) {
                return type(ts.String());
            }

            if (t.isNumeric() && s.isNumeric()) {
                return type(ts.promote(t, s));
            }

            throw new SemanticException("The " + op + " operator must have "
                    + "numeric or String operands.", position());
        }

        if (op == SUB_ASSIGN || op == MUL_ASSIGN || op == DIV_ASSIGN
                || op == MOD_ASSIGN) {
            if (t.isNumeric() && s.isNumeric()) {
                return type(ts.promote(t, s));
            }

            throw new SemanticException("The " + op + " operator must have "
                    + "numeric operands.", position());
        }

        if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN || op == BIT_XOR_ASSIGN) {
            if (t.isBoolean() && s.isBoolean()) {
                return type(ts.Boolean());
            }

            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                return type(ts.promote(t, s));
            }

            throw new SemanticException("The " + op + " operator must have "
                    + "integral or boolean operands.", position());
        }

        if (op == SHL_ASSIGN || op == SHR_ASSIGN || op == USHR_ASSIGN) {
            if (ts.isImplicitCastValid(t, ts.Long())
                    && ts.isImplicitCastValid(s, ts.Long())) {
                // Only promote the left of a shift.
                return type(ts.promote(t));
            }

            throw new SemanticException("The " + op + " operator must have "
                    + "integral operands.", position());
        }

        throw new InternalCompilerError("Unrecognized assignment operator "
                + op + ".");
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == right) {
            TypeSystem ts = av.typeSystem();

            // If the RHS is an integral constant, we can relax the expected
            // type to the type of the constant.
            if (ts.numericConversionValid(left.type(), child.constantValue())) {
                return child.type();
            }
            else {
                return left.type();
            }
        }

        return child.type();
    }

    /** Get the throwsArithmeticException of the expression. */
    @Override
    public boolean throwsArithmeticException() {
        // conservatively assume that any division or mod may throw
        // ArithmeticException this is NOT true-- floats and doubles don't
        // throw any exceptions ever...
        return op == DIV_ASSIGN || op == MOD_ASSIGN;
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
        w.allowBreak(2, 2, " ", 1); // miser mode
        w.begin(0);
        printSubExpr(right, false, w, tr);
        w.end();
    }

    /** Dumps the AST. */
    @Override
    public void dump(CodeWriter w) {
        super.dump(w);
        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(operator " + op + ")");
        w.end();
    }

    @Override
    abstract public Term firstChild();

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (operator() == ASSIGN) {
            acceptCFGAssign(v);
        }
        else {
            acceptCFGOpAssign(v);
        }
        return succs;
    }

    /**
     * Construct a CFG for this assignment when the assignment operator
     * is ASSIGN (i.e., the normal, simple assignment =).
     */
    protected abstract void acceptCFGAssign(CFGBuilder<?> v);

    /**
     * Construct a CFG for this assignment when the assignment operator
     * is of the form op= for some operation op.
     */
    protected abstract void acceptCFGOpAssign(CFGBuilder<?> v);

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<Type>();

        if (throwsArithmeticException()) {
            l.add(ts.ArithmeticException());
        }

        return l;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Assign(this.position, this.left, this.op, this.right);
    }

}
