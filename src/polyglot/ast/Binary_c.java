package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.types.*;

/**
 * A <code>Binary</code> represents a Java binary expression, an
 * immutable pair of expressions combined with an operator.
 */
public class Binary_c extends Expr_c implements Binary
{
    protected Expr left;
    protected Operator op;
    protected Expr right;
    protected Precedence precedence;

    public Binary_c(Ext ext, Position pos, Expr left, Operator op, Expr right) {
	super(ext, pos);
	this.left = left;
	this.op = op;
	this.right = right;
	this.precedence = op.precedence();
    }

    /** Get the left operand of the expression. */
    public Expr left() {
	return this.left;
    }

    /** Set the left operand of the expression. */
    public Binary left(Expr left) {
	Binary_c n = (Binary_c) copy();
	n.left = left;
	return n;
    }

    /** Get the operator of the expression. */
    public Operator operator() {
	return this.op;
    }

    /** Set the operator of the expression. */
    public Binary operator(Operator op) {
	Binary_c n = (Binary_c) copy();
	n.op = op;
	return n;
    }

    /** Get the right operand of the expression. */
    public Expr right() {
	return this.right;
    }

    /** Set the right operand of the expression. */
    public Binary right(Expr right) {
	Binary_c n = (Binary_c) copy();
	n.right = right;
	return n;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return this.precedence;
    }

    protected Binary precedence(Precedence precedence) {
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
    public Node visitChildren(NodeVisitor v) {
	Expr left = (Expr) this.left.visit(v);
	Expr right = (Expr) this.right.visit(v);
	return reconstruct(left, right);
    }

    /** Fold constants for the expression. */
    public Node foldConstants_(ConstantFolder cf) {
      	NodeFactory nf = cf.nodeFactory();

        if (left instanceof NumLit && right instanceof NumLit) {
	    long l = ((NumLit) left).longValue();
	    long r = ((NumLit) right).longValue();

	    if (op == ADD) return nf.IntLit(position(), l + r);
	    if (op == SUB) return nf.IntLit(position(), l - r);
	    if (op == MUL) return nf.IntLit(position(), l * r);
	    if (op == DIV && r != 0) return nf.IntLit(position(), l / r);
	    if (op == MOD && r != 0) return nf.IntLit(position(), l % r);
	    if (op == BIT_OR) return nf.IntLit(position(), l | r);
	    if (op == BIT_AND) return nf.IntLit(position(), l & r);
	    if (op == BIT_XOR) return nf.IntLit(position(), l ^ r);
	    if (op == SHL) return nf.IntLit(position(), l << r);
	    if (op == SHR) return nf.IntLit(position(), l >> r);
	    if (op == USHR) return nf.IntLit(position(), l >>> r);
	    if (op == GT) return nf.BooleanLit(position(), l > r);
	    if (op == LT) return nf.BooleanLit(position(), l < r);
	    if (op == GE) return nf.BooleanLit(position(), l >= r);
	    if (op == LE) return nf.BooleanLit(position(), l <= r);
	    if (op == NE) return nf.BooleanLit(position(), l != r);
	    if (op == EQ) return nf.BooleanLit(position(), l == r);
	}
	else if (left instanceof NumLit) {
	    long l = ((NumLit) left).longValue();

	    if (op == ADD && l == 0L) return right;
	    if (op == SUB && l == 0L) return right;
	    if (op == MUL && l == 1L) return right;
	    if (op == BIT_OR && l == 0L) return right;
	    if (op == BIT_XOR && l == 0L) return right;
	}
	else if (right instanceof NumLit) {
	    long r = ((NumLit) right).longValue();

	    if (op == ADD && r == 0L) return left;
	    if (op == SUB && r == 0L) return left;
	    if (op == MUL && r == 1L) return left;
	    if (op == DIV && r == 1L) return left;
	    if (op == MOD && r == 1L) return left;
	    if (op == BIT_OR && r == 0L) return left;
	    if (op == BIT_XOR && r == 0L) return left;
	    if (op == SHL && r == 0L) return left;
	    if (op == SHR && r == 0L) return left;
	    if (op == USHR && r == 0L) return left;
	}
	else if (left instanceof BooleanLit && right instanceof BooleanLit) {
	    boolean l = ((BooleanLit) left).value();
	    boolean r = ((BooleanLit) right).value();

	    if (op == BIT_OR) return nf.BooleanLit(position(), l | r);
	    if (op == BIT_AND) return nf.BooleanLit(position(), l & r);
	    if (op == BIT_XOR) return nf.BooleanLit(position(), l ^ r);
	    if (op == COND_OR) return nf.BooleanLit(position(), l || r);
	    if (op == COND_AND) return nf.BooleanLit(position(), l && r);
	    if (op == NE) return nf.BooleanLit(position(), l != r);
	    if (op == EQ) return nf.BooleanLit(position(), l == r);
	}
	else if (left instanceof BooleanLit) {
	    boolean l = ((BooleanLit) left).value();

	    // These are safe because the right expression would have been
	    // short-circuited.  BIT_OR and BIT_AND are not safe here.
	    if (op == COND_OR && l) return nf.BooleanLit(position(), true);
	    if (op == COND_AND && ! l) return nf.BooleanLit(position(), false);

	    // Here, the non-literal is always evaluated, so this is safe.
	    if (op == COND_OR && ! l) return right;
	    if (op == COND_AND && l) return right;
	    if (op == BIT_OR && ! l) return right;
	    if (op == BIT_AND && l) return right;
	}
	else if (left instanceof StringLit && right instanceof StringLit) {
	    String l = ((StringLit) left).value();
	    String r = ((StringLit) right).value();

	    // Don't do this.  Strings literals are usually broken for
	    // formatting reasons.
	    /*
	    if (op == ADD) return nf.StringLit(position(), l + r);
	    */
	}

        return this;
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        Type l = left.type();
	Type r = right.type();

	TypeSystem ts = tc.typeSystem();
	    
	if (op == GT || op == LT || op == GE || op == LE) {
	    if (! l.isNumeric() || ! r.isNumeric()) {
		throw new SemanticException("The " + op +
		    "operator must have numeric operands.", position());
	    }

	    return type(ts.Boolean());
	}

	if (op == EQ || op == NE) {
	    if (l.isNumeric() && ! r.isNumeric()) {
		throw new SemanticException("The " + op +
		    "operator must have operands of similar type.",
		    position());
	    }

	    if (l.isBoolean() && ! r.isBoolean()) {
		throw new SemanticException("The " + op +
		    "operator must have operands of similar type.",
		    position());
	    }

	    if (l.isReference() && ! (r.isReference() || r.isNull())) {
		throw new SemanticException("The " + op +
		    "operator must have operands of similar type.",
		    position());
	    }

	    return type(ts.Boolean());
	}
	    
	if (op == COND_OR || op == COND_AND) {
	    if (! l.isBoolean() || ! r.isBoolean()) {
		throw new SemanticException("The " + op +
		    "operator must have boolean operands.",
		    position());
	    }

	    return type(ts.Boolean());
	}

	if (op == ADD) {
	    if (l.isSame(ts.String()) || r.isSame(ts.String())) {
		return precedence(Precedence.STRING_ADD).type(ts.String());
	    }
	}

	if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
	    if (l.isBoolean() && r.isBoolean()) {
		return type(ts.Boolean());
	    }
	}

	if (! l.isNumeric() || ! r.isNumeric()) {
	    if (op == ADD) {
		throw new SemanticException("The " + op +
		    " operator must have numeric or String operands.",
		    position());
	    }

	    if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
		throw new SemanticException("The " + op +
		    " operator must have numeric or boolean operands.",
		    position());
	    }

	    if (op == SUB || op == MUL || op == DIV || op == MOD ||
		op == SHL || op == SHR || op == USHR) {
		throw new SemanticException("The " + op +
		    " operator must have numeric operands.",
		    position());
	    }
	}

	if (op == SHL || op == SHR || op == USHR) {
	    // For shift, only promote the left operand.
	    return type(ts.promote(l));
	}

	return type(ts.promote(l, r));
    }
  
    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	if (throwsArithmeticException()) {
	    ec.throwsException(ts.ArithmeticException());
	}

	return this;
    }

    /** Get the throwsArithmeticException of the expression. */
    public boolean throwsArithmeticException() {
	// conservatively assume that any division or mod may throw
	// ArithmeticException this is NOT true-- floats and doubles don't
	// throw any exceptions ever...
	return op == DIV || op == MOD;
    }

    public String toString() {
	return left + " " + op + " " + right;
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	TypeSystem ts = tr.typeSystem();

	translateSubexpr(left, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(type().isSame(ts.String()) ? 0 : 2, " ");
	translateSubexpr(right, w, tr);
    }
}
