package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A <code>Unary</code> represents a Java unary expression, an
 * immutable pair of an expression and an an operator.
 */
public class Unary_c extends Expr_c implements Unary
{
    protected Unary.Operator op;
    protected Expr expr;

    public Unary_c(Ext ext, Position pos, Unary.Operator op, Expr expr) {
	super(ext, pos);
	this.op = op;
	this.expr = expr;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return Precedence.UNARY;
    }

    /** Get the sub-expression of the expression. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the sub-expression of the expression. */
    public Unary expr(Expr expr) {
	Unary_c n = (Unary_c) copy();
	n.expr = expr;
	return n;
    }

    /** Get the operator. */
    public Unary.Operator operator() {
	return this.op;
    }

    /** Set the operator. */
    public Unary operator(Unary.Operator op) {
	Unary_c n = (Unary_c) copy();
	n.op = op;
	return n;
    }

    /** Reconstruct the expression. */
    protected Unary_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Unary_c n = (Unary_c) copy();
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) visitChild(this.expr, v);
	return reconstruct(expr);
    }

    /** Fold constants for the expression. */
    public Node foldConstants_(ConstantFolder cf) {
      	NodeFactory nf = cf.nodeFactory();

        if (expr instanceof NumLit) {
	    long x = ((NumLit) expr).longValue();

	    if (op == BIT_NOT) return nf.IntLit(position(), ~x).type(type());
	    if (op == NEG) return nf.IntLit(position(), -x).type(type());
	    if (op == POS) return nf.IntLit(position(), x).type(type());
	}
	else if (expr instanceof BooleanLit) {
	    boolean x = ((BooleanLit) expr).value();

	    if (op == NOT) return nf.BooleanLit(position(), !x).type(type());
	}

        return this;
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	if (op == POST_INC || op == POST_DEC ||
	    op == PRE_INC || op == PRE_DEC) {

	    if (! expr.type().isNumeric()) {
		throw new SemanticException("Operand of " + op +
		    " operator must be numeric.", expr.position());
	    }

	    return type(expr.type());
	}

	if (op == BIT_NOT) {
	    if (! expr.type().isImplicitCastValid(ts.Long())) {
		throw new SemanticException("Operand of " + op +
		    " operator must be numeric.", expr.position());
	    }

	    return type(ts.promote(expr.type()));
	}

	if (op == NEG || op == POS) {
	    if (! expr.type().isNumeric()) {
		throw new SemanticException("Operand of " + op +
		    " operator must be numeric.", expr.position());
	    }

	    return type(ts.promote(expr.type()));
	}

	if (op == NOT) {
	    if (! expr.type().isBoolean()) {
		throw new SemanticException("Operand of " + op +
		    " operator must be boolean.", expr.position());
	    }

	    return type(expr.type());
	}

	return this;
    }

    public Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == expr) {
            if (op == POST_INC || op == POST_DEC ||
                op == PRE_INC || op == PRE_DEC) {

                return child.expectedType(ts.Double());
            }
            else if (op == NEG || op == POS) {
                return child.expectedType(ts.Double());
            }
            else if (op == BIT_NOT) {
                return child.expectedType(ts.Long());
            }
            else if (op == NOT) {
                return child.expectedType(ts.Boolean());
            }
        }

        return child;
    }

    /** Check exceptions thrown by the statement. */
    public String toString() {
        if (op.isPrefix()) {
	    return op.toString() + expr.toString();
	}
	else {
	    return expr.toString() + op.toString();
	}
    }

    public void translate_(CodeWriter w, Translator tr) {
        if (op.isPrefix()) {
	    w.write(op.toString());
	    translateSubexpr(expr, w, tr);
	}
	else {
	    translateSubexpr(expr, w, tr);
	    w.write(op.toString());
	}
    }
}
