package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.types.*;

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

    public Binary_c(Del ext, Position pos, Expr left, Operator op, Expr right) {
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
    public Node visitChildren(NodeVisitor v) {
	Expr left = (Expr) visitChild(this.left, v);
	Expr right = (Expr) visitChild(this.right, v);
	return reconstruct(left, right);
    }

    /** Fold constants for the expression. */
    public Node foldConstants(ConstantFolder cf) {
      	NodeFactory nf = cf.nodeFactory();

        if (left instanceof NumLit && right instanceof NumLit) {
	    long l = ((NumLit) left).longValue();
	    long r = ((NumLit) right).longValue();

	    if (op == ADD) return nf.IntLit(position(), l + r).type(type());
	    if (op == SUB) return nf.IntLit(position(), l - r).type(type());
	    if (op == MUL) return nf.IntLit(position(), l * r).type(type());
	    if (op == DIV && r != 0) return nf.IntLit(position(), l / r).type(type());
	    if (op == MOD && r != 0) return nf.IntLit(position(), l % r).type(type());
	    if (op == BIT_OR) return nf.IntLit(position(), l | r).type(type());
	    if (op == BIT_AND) return nf.IntLit(position(), l & r).type(type());
	    if (op == BIT_XOR) return nf.IntLit(position(), l ^ r).type(type());
	    if (op == SHL) return nf.IntLit(position(), l << r).type(type());
	    if (op == SHR) return nf.IntLit(position(), l >> r).type(type());
	    if (op == USHR) return nf.IntLit(position(), l >>> r).type(type());
	    if (op == GT) return nf.BooleanLit(position(), l > r).type(type());
	    if (op == LT) return nf.BooleanLit(position(), l < r).type(type());
	    if (op == GE) return nf.BooleanLit(position(), l >= r).type(type());
	    if (op == LE) return nf.BooleanLit(position(), l <= r).type(type());
	    if (op == NE) return nf.BooleanLit(position(), l != r).type(type());
	    if (op == EQ) return nf.BooleanLit(position(), l == r).type(type());
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

	    if (op == BIT_OR) return nf.BooleanLit(position(), l | r).type(type());
	    if (op == BIT_AND) return nf.BooleanLit(position(), l & r).type(type());
	    if (op == BIT_XOR) return nf.BooleanLit(position(), l ^ r).type(type());
	    if (op == COND_OR) return nf.BooleanLit(position(), l || r).type(type());
	    if (op == COND_AND) return nf.BooleanLit(position(), l && r).type(type());
	    if (op == NE) return nf.BooleanLit(position(), l != r).type(type());
	    if (op == EQ) return nf.BooleanLit(position(), l == r).type(type());
	}
	else if (left instanceof BooleanLit) {
	    boolean l = ((BooleanLit) left).value();

	    // These are safe because the right expression would have been
	    // short-circuited.  BIT_OR and BIT_AND are not safe here.
	    if (op == COND_OR && l) return nf.BooleanLit(position(), true).type(type());
	    if (op == COND_AND && ! l) return nf.BooleanLit(position(), false).type(type());

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
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type l = left.type();
	Type r = right.type();

	TypeSystem ts = tc.typeSystem();

	if (op == GT || op == LT || op == GE || op == LE) {
	    if (! l.isNumeric()) {
		throw new SemanticException("The " + op +
		    " operator must have numeric operands.", left.position());
	    }

            if (! r.isNumeric()) {
		throw new SemanticException("The " + op +
		    " operator must have numeric operands.", right.position());
	    }

	    return type(ts.Boolean());
	}

	if (op == EQ || op == NE) {
            if (! ts.isCastValid(l, r) && ! ts.isCastValid(r, l)) {
		throw new SemanticException("The " + op +
		    " operator must have operands of similar type.",
		    position());
	    }

	    return type(ts.Boolean());
	}

	if (op == COND_OR || op == COND_AND) {
	    if (! l.isBoolean()) {
		throw new SemanticException("The " + op +
		    " operator must have boolean operands.", left.position());
	    }

	    if (! r.isBoolean()) {
		throw new SemanticException("The " + op +
		    " operator must have boolean operands.", right.position());
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

        if (op == ADD) {
            if (! l.isNumeric()) {
                throw new SemanticException("The " + op +
                    " operator must have numeric or String operands.",
                    left.position());
            }

            if (! r.isNumeric()) {
                throw new SemanticException("The " + op +
                    " operator must have numeric or String operands.",
                    right.position());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (! l.isImplicitCastValid(ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric or boolean operands.",
                    left.position());
            }

            if (! r.isImplicitCastValid(ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric or boolean operands.",
                    right.position());
            }
        }

        if (op == SUB || op == MUL || op == DIV || op == MOD) {
            if (! l.isNumeric()) {
                throw new SemanticException("The " + op +
                    " operator must have numeric operands.", left.position());
            }

            if (! r.isNumeric()) {
                throw new SemanticException("The " + op +
                    " operator must have numeric operands.", right.position());
            }
        }

        if (op == SHL || op == SHR || op == USHR) {
            if (! l.isImplicitCastValid(ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric operands.", left.position());
            }

            if (! r.isImplicitCastValid(ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric operands.", right.position());
            }
        }

	if (op == SHL || op == SHR || op == USHR) {
	    // For shift, only promote the left operand.
	    return type(ts.promote(l));
	}

	return type(ts.promote(l, r));
    }

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

	if (op == EQ || op == NE) {
            // Coercion to compatible types.
            if (other.type().isReference() || other.type().isNull()) {
                return ts.Object();
            }

            if (other.type().isBoolean()) {
                return ts.Boolean();
            }

            if (other.type().isNumeric()) {
                return ts.Double();
            }
        }

        if (op == ADD && type.isSame(ts.String())) {
            // Implicit coercion to String.
            return ts.String();
        }

        if (op == GT || op == LT || op == GE || op == LE) {
            if (other.type().isNumeric()) {
                return ts.Double();
            }
        }

        if (op == COND_OR || op == COND_AND) {
            return ts.Boolean();
        }

	if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (other.type().isBoolean()) {
                return ts.Boolean();
            }
            return ts.Long();
        }

        if (op == SUB || op == MUL || op == DIV || op == MOD) {
            return ts.Double();
        }

        if (op == SHL || op == SHR || op == USHR) {
            return ts.Long();
        }

        return child.type();
    }

    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
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
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(left, true, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(type() == null || type().isPrimitive() ? 2 : 0, " ");
	printSubExpr(right, false, w, tr);
    }

  public void dump(CodeWriter w) {
    super.dump(w);

    if (type != null) {
      w.allowBreak(4, " ");
      w.begin(0);
      w.write("(operator " + op + ")");
      w.end();
    }
  }
}
