/* 
 * TernaryExpression.java
 */

package jltools.ast;


/**
 * TernaryExpression
 * 
 * Overview: A TernayExpression represents a Java ternay (conditional)
 * expression as mutable triple of expressions.
 */

public class TernaryExpression extends Expression {
    
    /**
     * Effects: Creates a new TernaryExpression consisting of
     *    <conditionalExpression>, <trueExpression>, and
     *    <falseExpression> coresponding to
     *    <conditionalExpression>?<trueExpression>:<falseExpression>.
     */ 
    public TernaryExpression(Expression conditionalExpression,
			     Expression trueResult,
			     Expression falseResult) {
	this.conditional = conditionalExpression;
	this.trueResult = trueResult;
	this.falseResult = falseResult;
    }

    /**
     * Effects: Returns the conditional subexpression
     */ 
    public Expression getConditionalExpression() {
	return conditional;
    }

    /**
     * Effects: Sets the conditional subexpression to <newConditional>.
     */
    public void setConditionalExpression(Expression newConditional) {
	conditional = newConditional;
    }

    /**
     * Effects: Returns the true result
     */ 
    public Expression getTrueResult() {
	return trueResult;
    }

    /**
     * Effects: Sets the true result to <newTrueResult>.
     */
    public void setTrueResult(Expression newTrueResult) {
	trueResult = newTrueResult;
    }
    
    /**
     * Effects: Returns the false result
     */ 
    public Expression getFalseResult() {
	return falseResult;
    }

    /**
     * Effects: Sets the false result to <newFalseResult>.
     */
    public void setFalseResult(Expression newFalseResult) {
	falseResult = newFalseResult;
    }

    public Node accept(NodeVisitor v) {
	return v.visitTernaryExpression(this);
    }

    /**
     * Requires: v will not transform an Expression into anything other
     *    than another Expression.
     * Effects:
     *    Visits all three children of this in order from left to right.
     */
    public void visitChildren(NodeVisitor v) {
	conditional  = (Expression) conditional.accept(v);
	trueResult   = (Expression) trueResult.accept(v);
	falseResult  = (Expression) falseResult.accept(v);
    }

    public Node copy() {
      TernaryExpression te = 
	new TernaryExpression(conditional, trueResult, falseResult);
      te.copyAnnotationsFrom(this);
      return te;						   
    }

    public Node deepCopy() {
      TernaryExpression te = 
	new TernaryExpression((Expression) conditional.deepCopy(), 
			      (Expression) trueResult.deepCopy(), 
			      (Expression) falseResult.deepCopy());
      te.copyAnnotationsFrom(this);
      return te;
    }


    private Expression conditional;
    private Expression trueResult;
    private Expression falseResult;
}
