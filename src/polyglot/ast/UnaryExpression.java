/*
 * UnaryExpression.java
 */

package jltools.ast;
import jltools.util.*;
import jltools.types.*;
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
  Object visitChildren(NodeVisitor vis)
  {
    expr = (Expression)expr.visit(vis);
    return vis.mergeVisitorInfo( Annotate.getVisitorInfo( this),
                                 Annotate.getVisitorInfo( expr));
  }

  public int getPrecedence()
  {
    return PRECEDENCE_UNARY;
  }

  public Node typeCheck(LocalContext c) throws TypeCheckException
  {
    Type type = expr.getCheckedType();
    switch( operator)
    {
    case POSTINCR:
    case POSTDECR:
    case PREINCR:
    case PREDECR:
      if( !type.isPrimitive()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      if( !((PrimitiveType)type).isNumeric()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      setCheckedType( type);
      break;

    case BITCOMP:
    case NEGATIVE:
    case POSITIVE:
      if( !type.isPrimitive()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      if( !((PrimitiveType)type).isNumeric()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      setCheckedType( PrimitiveType.unaryPromotion( (PrimitiveType)type));
      break;

    case LOGICALNOT:
      if( !type.isPrimitive()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be boolean.");
      }
      if( !((PrimitiveType)type).isBoolean()) {
        throw new TypeCheckException( "Operand of " 
                + getOperatorName() + " operator must be boolean.");
      }
      setCheckedType( type);
      break;

    default:
      throw new InternalCompilerError( "Unknown unary operator: " + operator);
    }
     
    return this;
  }

  public void  translate(LocalContext c, CodeWriter w)
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
      if (operator == POSITIVE)
        w.write("+");
      if (operator == LOGICALNOT)
        w.write("!");
      
      translateExpression( expr, c, w);
    }
    else {
      
      // postfix op.
      translateExpression( expr, c, w);
      
      if (operator == POSTINCR)
        w.write("++");
      if (operator == POSTDECR)
        w.write("--");
    }
  }

   public Node dump( CodeWriter w)
   {
      if (operator == NEGATIVE) 
         w.write("( NEGATIVE ");
      if (operator == BITCOMP)
         w.write("( BIT-COMPL");
      if (operator == PREINCR)
         w.write("( PRE-INCR ");
      if (operator == PREDECR)
         w.write("( PRE-DECR ");
      if (operator == POSTINCR)
         w.write("( POST-INCR ");
      if (operator == POSTDECR)
         w.write("( POST-DECR ");
      if (operator == POSITIVE)
         w.write("( POSITIVE ");
      if (operator == LOGICALNOT)
         w.write("( LOGICAL-NOT ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
   }

  protected String getOperatorName()
  {
    if (operator == NEGATIVE) 
      return "numeric negation";
    if (operator == BITCOMP)
      return "bitwise complement";
    if (operator == PREINCR)
      return "prefix increment";
    if (operator == PREDECR)
      return "prefix decrement";
    if (operator == POSTINCR)
      return "postfix increment";
    if (operator == POSTDECR)
      return "postfix decrement";
    if (operator == POSITIVE)
      return "unary plus";
    if (operator == LOGICALNOT)
      return "logical negation";
    else
      return "unknown";
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
