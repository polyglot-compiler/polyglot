/*
 * BinaryExpression.java
 */

package jltools.ast;

/**
 * BinaryExpression
 * 
 * Overview: A BinaryExpression represents a Java binary expression, a
 * mutable pair of expressions combined with an operator.
 */

public class BinaryExpression extends Expression {

    public static final int ASSIGN         = 0; // = operator
    public static final int GT             = 1; // > operator
    public static final int LT             = 2; // < opereator
    public static final int EQUAL          = 3; // == operator
    public static final int LE             = 4; // <= operator
    public static final int GE             = 5; // >= operator
    public static final int NE             = 6; // != operator
    public static final int LOGIC_OR       = 7; // || operator
    public static final int LOGIC_AND      = 8; // && operator
    public static final int MULT           = 9; // * operator
    public static final int DIV            = 10; // / operator
    public static final int BIT_OR         = 11; // | operator
    public static final int BIT_AND        = 12; // & operator
    public static final int BIT_XOR        = 13; // ^ operator
    public static final int MOD            = 14; // % operator
    public static final int LSHIFT         = 15; // << operator
    public static final int RSHIFT         = 16; // >> operator
    public static final int RUSHIFT        = 17; // >>> operator
    public static final int PLUSASSIGN     = 18; // += operator
    public static final int SUBASSIGN      = 19; // -= operator
    public static final int MULTASSIGN     = 20; // *= operator
    public static final int DIVASSIGN      = 21; // /= operator
    public static final int ANDASSIGN      = 22; // &= operator
    public static final int ORASSIGN       = 23; // |= operator
    public static final int XORASSIGN      = 24; // ^= operator
    public static final int MODASSIGN      = 25; // %= operator
    public static final int LSHIFTASSIGN   = 26; // <<= operator
    public static final int RSHIFTASSIGN   = 27; // >>= operator
    public static final int RUSHIFTASSIGN  = 28; // >>>= operator

    // Largest operator used.
    public static final int MAX_OPERATOR   = RUSHIFTASSIGN; 

    /** 
     * Requires: A valid value for <operator> as listed in public
     *    static ints in this class. 
     * Effects: Creates a new BinaryExpression of <operator> applied
     *    to <left> and <right>.
     */ 
    public BinaryExpression(Expression left, int operator, Expression right) {
	if (left == null || right == null) {
	    throw new NullPointerException ("BinaryExpression cannot " +
					    "take null Expressions");
	}
	this.left = left;
	this.right = right;
	setOperator(operator);
    }

    /**
     * Effects: Returns the operator corresponding to <this>.
     */ 
    public int getOperator() {
	return operator;
    }

    /**
     * Requires: <newOperator> to be one of the valid operators
     *    defined in BinaryExpression.
     * Effects: Changes the operator of <this> to <newOperator>.
     */
    public void setOperator(int newOperator) {
	if (newOperator < 0 || newOperator > MAX_OPERATOR) {
	    throw new IllegalArgumentException("Value for operator to " +
					       "BinaryExpression not valid.");
	}
	operator = newOperator;
    }

    /**
     * Effects: Returns the left-hand subexpression.
     */ 
    public Expression getLeftExpression() {
	return left;
    }

    /** 
     * Effects: Sets the left-hand subexpression to <newExp>
     */
    public void setLeftExpression(Expression newExp) {
	if (newExp == null) {
	    throw new NullPointerException("BinaryExpression does not " +
					   "allow null subexpressions.");
	}
	left = newExp;
    }

    /**
     * Effects: Returns the right-hand subexpression.
     */ 
    public Expression getRightExpression() {
	return right;
    }

    /** 
     * Effects: Sets the right-hand subexpression to <newExp>
     */
    public void setRightExpression(Expression newExp) {
	if (newExp == null) {
	    throw new NullPointerException("BinaryExpression does not " +
					   "allow null subexpressions.");
	}
	right = newExp;
    }

    public Node accept(NodeVisitor v) {
	return v.visitBinaryExpression(this);
    }

    /**
     * Requires: v will not transform an Expression into anything other
     *    then another Expression.
     * Effects:
     *    Visits both of the children from left to right.
     */ 
    public void visitChildren(NodeVisitor v) {
	left  = (Expression) left.accept(v);
	right = (Expression) right.accept(v);
    }
    

    public Node copy() {
      BinaryExpression be = new BinaryExpression(left, operator, right);
      be.copyAnnotationsFrom(this);
      return be;
    }

    public Node deepCopy() {
      BinaryExpression be = new BinaryExpression((Expression) left.deepCopy(),
						operator, 
						(Expression) right.deepCopy());
      be.copyAnnotationsFrom(this);
      return be;
    }

    private Expression left, right;
    private int operator;
}
