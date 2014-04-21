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

import java.util.LinkedList;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code Assign} represents a Java assignment expression.
 */
public abstract class Assign_c extends Expr_c implements Assign {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr left;
    protected Operator op;
    protected Expr right;

    @Deprecated
    public Assign_c(Position pos, Expr left, Operator op, Expr right) {
        this(pos, left, op, right, null);
    }

    public Assign_c(Position pos, Expr left, Operator op, Expr right, Ext ext) {
        super(pos, ext);
        assert (left != null && op != null && right != null);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Precedence precedence() {
        return Precedence.ASSIGN;
    }

    @Override
    public Expr left() {
        return this.left;
    }

    @Override
    public Assign left(Expr left) {
        return left(this, left);
    }

    protected <N extends Assign_c> N left(N n, Expr left) {
        if (n.left == left) return n;
        n = copyIfNeeded(n);
        n.left = left;
        return n;
    }

    @Override
    public Operator operator() {
        return this.op;
    }

    @Override
    public Assign operator(Operator op) {
        return operator(this, op);
    }

    protected <N extends Assign_c> N operator(N n, Operator op) {
        if (n.op == op) return n;
        n = copyIfNeeded(n);
        n.op = op;
        return n;
    }

    @Override
    public Expr right() {
        return this.right;
    }

    @Override
    public Assign right(Expr right) {
        return right(this, right);
    }

    protected <N extends Assign_c> N right(N n, Expr right) {
        if (n.right == right) return n;
        n = copyIfNeeded(n);
        n.right = right;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Assign_c> N reconstruct(N n, Expr left, Expr right) {
        n = left(n, left);
        n = right(n, right);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr left = visitChild(this.left, v);
        Expr right = visitChild(this.right, v);
        return reconstruct(this, left, right);
    }

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
            if (!ts.isImplicitCastValid(s, t)
                    && !ts.typeEquals(s, t)
                    && !ts.numericConversionValid(t,
                                                  tc.lang()
                                                    .constantValue(right,
                                                                   tc.lang()))) {

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
        if (child == left) {
            return child.type();
        }

        // See JLS 2nd ed. 15.26.2
        TypeSystem ts = av.typeSystem();
        if (op == ASSIGN) {
            return left.type();
        }
        if (op == ADD_ASSIGN) {
            if (ts.typeEquals(ts.String(), left.type())) {
                return child.type();
            }
        }
        if (op == ADD_ASSIGN || op == SUB_ASSIGN || op == MUL_ASSIGN
                || op == DIV_ASSIGN || op == MOD_ASSIGN || op == SHL_ASSIGN
                || op == SHR_ASSIGN || op == USHR_ASSIGN) {
            if (left.type().isNumeric() && right.type().isNumeric()) {
                try {
                    return ts.promote(left.type(), child.type());
                }
                catch (SemanticException e) {
                    throw new InternalCompilerError(e);
                }
            }
            // Assume the typechecker knew what it was doing
            return child.type();
        }
        if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN || op == BIT_XOR_ASSIGN) {
            if (left.type().isBoolean()) {
                return ts.Boolean();
            }
            if (left.type().isNumeric() && right.type().isNumeric()) {
                try {
                    return ts.promote(left.type(), child.type());
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
        List<Type> l = new LinkedList<>();

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
