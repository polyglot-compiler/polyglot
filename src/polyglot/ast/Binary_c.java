package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.types.*;

import java.util.*;

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

    public Binary_c(Position pos, Expr left, Operator op, Expr right) {
	super(pos);
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

    public Object constantValue() {
        Object lv = left.constantValue();
        Object rv = right.constantValue();

        // if one of the operands is not constant, return null now.
        if (lv == null || rv == null) {
            return null;
        }

        if (op == ADD && (lv instanceof String || rv instanceof String)) {
            // toString() does what we want for String, Number, and Boolean
            return lv.toString() + rv.toString();
        }

        if (op == EQ && (lv instanceof String && rv instanceof String)) {
            return new Boolean(((String) lv).intern() == ((String) rv).intern());
        }

        if (op == NE && (lv instanceof String && rv instanceof String)) {
            return new Boolean(((String) lv).intern() != ((String) rv).intern());
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
                    if (op == EQ) return new Boolean(l == r);
                    if (op == NE) return new Boolean(l != r);
                    if (op == LT) return new Boolean(l < r);
                    if (op == LE) return new Boolean(l <= r);
                    if (op == GE) return new Boolean(l >= r);
                    if (op == GT) return new Boolean(l > r);
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
                    if (op == EQ) return new Boolean(l == r);
                    if (op == NE) return new Boolean(l != r);
                    if (op == LT) return new Boolean(l < r);
                    if (op == LE) return new Boolean(l <= r);
                    if (op == GE) return new Boolean(l >= r);
                    if (op == GT) return new Boolean(l > r);
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
                    if (op == EQ) return new Boolean(l == r);
                    if (op == NE) return new Boolean(l != r);
                    if (op == LT) return new Boolean(l < r);
                    if (op == LE) return new Boolean(l <= r);
                    if (op == GE) return new Boolean(l >= r);
                    if (op == GT) return new Boolean(l > r);
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
                if (op == EQ) return new Boolean(l == r);
                if (op == NE) return new Boolean(l != r);
                if (op == LT) return new Boolean(l < r);
                if (op == LE) return new Boolean(l <= r);
                if (op == GE) return new Boolean(l >= r);
                if (op == GT) return new Boolean(l > r);
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

            if (op == EQ) return new Boolean(l == r);
            if (op == NE) return new Boolean(l != r);
            if (op == BIT_AND) return new Boolean(l & r);
            if (op == BIT_OR) return new Boolean(l | r);
            if (op == BIT_XOR) return new Boolean(l ^ r);
            if (op == COND_AND) return new Boolean(l && r);
            if (op == COND_OR) return new Boolean(l || r);
        }

        return null;
    }

    protected Node num(NodeFactory nf, long value) {
        Position p = position();
        Type t = type();
        TypeSystem ts = t.typeSystem();

        // Binary promotion
        IntLit.Kind kind = IntLit.INT;

        if (left instanceof IntLit && ((IntLit) left).kind() == IntLit.LONG) {
            kind = IntLit.LONG;
        }

        if (right instanceof IntLit && ((IntLit) right).kind() == IntLit.LONG) {
            kind = IntLit.LONG;
        }

        return nf.IntLit(p, kind, value).type(t);
    }

    protected Node bool(NodeFactory nf, boolean value) {
        return nf.BooleanLit(position(), value).type(type());
    }

    /** Fold constants for the expression. */
    public Node foldConstants(ConstantFolder cf) {
      	NodeFactory nf = cf.nodeFactory();

        if (left instanceof NumLit && right instanceof NumLit) {
	    long l = ((NumLit) left).longValue();
	    long r = ((NumLit) right).longValue();

	    if (op == ADD) return num(nf, l + r);
	    if (op == SUB) return num(nf, l - r);
	    if (op == MUL) return num(nf, l * r);
	    if (op == DIV && r != 0) return num(nf, l / r);
	    if (op == MOD && r != 0) return num(nf, l % r);
	    if (op == BIT_OR) return num(nf, l | r);
	    if (op == BIT_AND) return num(nf, l & r);
	    if (op == BIT_XOR) return num(nf, l ^ r);
	    if (op == SHL) return num(nf, l << r);
	    if (op == SHR) return num(nf, l >> r);
	    if (op == USHR) return num(nf, l >>> r);
	    if (op == GT) return bool(nf, l > r);
	    if (op == LT) return bool(nf, l < r);
	    if (op == GE) return bool(nf, l >= r);
	    if (op == LE) return bool(nf, l <= r);
	    if (op == NE) return bool(nf, l != r);
	    if (op == EQ) return bool(nf, l == r);
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
	    if (ts.equals(l, ts.String()) || ts.equals(r, ts.String())) {
                if (!ts.canCoerceToString(r, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression " + 
                                "of type " + r + " to a String.", 
                                right.position());
                }
                if (!ts.canCoerceToString(l, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression " + 
                                "of type " + l + " to a String.", 
                                left.position());
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
            if (! ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric or boolean operands.",
                    left.position());
            }

            if (! ts.isImplicitCastValid(r, ts.Long())) {
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
            if (! ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The " + op +
                    " operator must have numeric operands.", left.position());
            }

            if (! ts.isImplicitCastValid(r, ts.Long())) {
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

        try {
            if (op == EQ || op == NE) {
                // Coercion to compatible types.
                if ((child.type().isReference() || child.type().isNull()) &&
                    (other.type().isReference() || other.type().isNull())) {
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

            if (op == ADD && ts.equals(type, ts.String())) {
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
      w.write("(type " + type + ")");
      w.end();
    }

    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(operator " + op + ")");
    w.end();
  }

  public Term entry() {
    return left.entry();
  }

  public List acceptCFG(CFGBuilder v, List succs) {
    if (op == COND_AND || op == COND_OR) {
      // short-circuit
      if (left instanceof BooleanLit) {
        BooleanLit b = (BooleanLit) left;
        if ((b.value() && op == COND_OR) || (! b.value() && op == COND_AND)) {
          v.visitCFG(left, this);
        }
        else {
          v.visitCFG(left, right.entry());
          v.visitCFG(right, this);
        }
      }
      else {
        if (op == COND_AND) {
          // AND operator
          // short circuit means that left is false
          v.visitCFG(left, FlowGraph.EDGE_KEY_TRUE, right.entry(), 
                           FlowGraph.EDGE_KEY_FALSE, this);
        }
        else {
          // OR operator
          // short circuit means that left is true
          v.visitCFG(left, FlowGraph.EDGE_KEY_FALSE, right.entry(), 
                           FlowGraph.EDGE_KEY_TRUE, this);            
        }
        v.visitCFG(right, this);
      }
    }
    else {
      v.visitCFG(left, right.entry());
      v.visitCFG(right, this);
    }

    return succs;
  }

  public List throwTypes(TypeSystem ts) {
    if (throwsArithmeticException()) {
      return Collections.singletonList(ts.ArithmeticException());
    }

    return Collections.EMPTY_LIST;
  }
}
