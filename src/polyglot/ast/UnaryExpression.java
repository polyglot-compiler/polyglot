/*
 * UnaryExpression.java
 */

package jltools.ast;

/**
 * UnaryExpression
 * 
 * Overview: A BinaryExpression represents a Java unary expression, a
 * mutable pair of an expression and an an operator.
 */

public class UnaryExpression extends Expression {

    public static final int BITCOMP        = 29; // ~ operator
    public static final int NEGATIVE       = 30; // - operator
    public static final int POSTINCR       = 31; // <?>++ operator
    public static final int POSTDECR       = 32; // <?>-- operator
    public static final int PREINCR        = 33; // ++<?> operator
    public static final int PREDECR        = 34; // --<?> operator

    public static final int MIN_OPERATOR = BITCOMP;
    public static final int MAX_OPERATOR = PREDECR;  

    /** 
     * Requires: A valid value for <operator> as listed in public
     *    static ints in this class. 
     * Effects: Creates a new UnaryExpression of <operator> applied
     *    to <expr>.
     */ 
    public UnaryExpression(Expression expr, int operator) {
	this.expr = expr;
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
     *    defined in UnaryExpression.
     * Effects: Changes the operator of <this> to <newOperator>.
     */
    public void setOperator(int newOperator) {
	if (newOperator < MIN_OPERATOR || newOperator > MAX_OPERATOR) {
	    throw new IllegalArgumentException("Value for operator to " +
					       "UnaryExpression not valid.");
	}
	operator = newOperator;
    }

    /**
     * Effects: Returns the subexpression.
     */ 
    public Expression getExpression() {
	return expr;
    }

    /** 
     * Effects: Sets the subexpression to <newExp>
     */
    public void setExpression(Expression newExpr) {
	expr = newExpr;
    }

    public Node accept(NodeVisitor v) {
	return v.visitUnaryExpression(this);
    }

    /**
     * Requires: v will not transform an Expression into anything other
     *    then another Expression.
     * Effects:
     *    Visits the child of this.
     */ 
    public void visitChildren(NodeVisitor v) {
	expr  = (Expression) expr.accept(v);
    }

    public Node copy() {
      UnaryExpression ue = new UnaryExpression(expr, operator);
      ue.copyAnnotationsFrom(this);
      return ue;
    }

    public Node deepCopy() {
      UnaryExpression ue = new UnaryExpression( (Expression) expr.deepCopy(),
						operator);
      ue.copyAnnotationsFrom(this);
      return ue;
    }
      
    
    private Expression expr;
    private int operator;
}
