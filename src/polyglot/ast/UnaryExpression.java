/*
 * UnaryExpression.java
 */

package jltools.ast;
import jltools.util.CodeWriter;
import jltools.types.Context;
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
    public static final int POSITIVE       = 35; // + operator
    public static final int LOGICALNOT     = 36; // ! operator

    public static final int MIN_OPERATOR = BITCOMP;
    public static final int MAX_OPERATOR = LOGICALNOT;

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

   /**
    *
    */
   void visitChildren(NodeVisitor vis)
   {
      expr = (Expression)expr.visit(vis);
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void  translate(Context c, CodeWriter w)
   {
      if (operator <= NEGATIVE ||
          operator >= PREINCR)
      {
         // prefix op.
         if (operator == NEGATIVE) 
            w.write("-");
         if (operator == BITCOMP)
            w.write("~");
         if (operator == PREINCR)
            w.write("++");
         if (operator == PREDECR)
            w.write("--");
         expr.translate(c, w);
      }
      else
      {
         expr.translate(c, w);
         if (operator == POSTINCR)
            w.write("++");
         if (operator == POSTDECR)
            w.write("--");
      }
   }

   public void  dump(Context c, CodeWriter w)
   {
      if (operator == NEGATIVE) 
         w.write("(NEGATIVE ");
      if (operator == BITCOMP)
         w.write("(BIT-COMPL");
      if (operator == PREINCR)
         w.write("(PRE-INCR ");
      if (operator == PREDECR)
         w.write("(PRE-DECR ");
      if (operator == POSTINCR)
         w.write("(POST-INCR ");
      if (operator == POSTDECR)
         w.write("(POST-DECR ");
      dumpNodeInfo(c, w);
      expr.dump(c, w);
      w.write(")");
      
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
