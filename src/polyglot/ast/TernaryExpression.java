/* 
 * TernaryExpression.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;

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


   void visitChildren(NodeVisitor vis)
   {
	conditional  = (Expression) conditional.visit(vis);
	trueResult   = (Expression) trueResult.visit(vis);
	falseResult  = (Expression) falseResult.visit(vis);
   }

   public Node typeCheck(LocalContext c) throws TypeCheckException
   {
     if (! conditional.getCheckedType().equals ( c.getTypeSystem().getBoolean() ) ) 
       throw new TypeCheckException( "The conditional must be of type boolean.");

     setCheckedType ( c.getTypeSystem().leastCommonAncestor( trueResult.getCheckedType(), 
                                                             falseResult.getCheckedType()));
     addThrows ( conditional.getThrows() );
     addThrows ( trueResult.getThrows() );
     addThrows ( falseResult.getThrows() );

     return this;
   }

   public void translate(LocalContext c, CodeWriter w)
   {
      w.write("( " );
      conditional.translate(c, w);
      w.write(" ) ? ( ");
      trueResult.translate(c, w);
      w.write(" ) : ( " );
      falseResult.translate(c, w);
      w.write(" )");
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( TERNARY ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
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
