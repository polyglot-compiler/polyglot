package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * A <code>UnaryExpression</code> represents a Java unary expression, an
 * immutable pair of an expression and an an operator.
 */
public class UnaryExpression extends Expression 
{
  public static final int BITCOMP        = 29; // ~ operator
  public static final int NEGATIVE       = 30; // - operator
  public static final int POSTINCR       = 31; // <?>++ operator
  public static final int POSTDECR       = 32; // <?>-- operator
  public static final int PREINCR        = 33; // ++<?> operator
  public static final int PREDECR        = 34; // --<?> operator
  public static final int POSITIVE       = 35; // + operator
  public static final int LOGICALNOT     = 36; // ! operator
  
  protected static final int MIN_OPERATOR = BITCOMP;
  protected static final int MAX_OPERATOR = LOGICALNOT;
      
  protected final Expression expr;
  protected final int operator;

  /** 
   * Requires: A valid value for <operator> as listed in public
   *    static ints in this class. 
   * Effects: Creates a new UnaryExpression of <operator> applied
   *    to <expr>.
   */ 
  public UnaryExpression(Node ext, Expression expr, int operator) 
  {
    if( operator < MIN_OPERATOR || operator > MAX_OPERATOR) {
      throw new IllegalArgumentException( "Value for operator to " +
                                          "UnaryExpression not valid.");
    }
    this.ext = ext;
    this.expr = expr;
    this.operator = operator;
  }
  
    public UnaryExpression(Expression expr, int operator) {
	this(null, expr, operator);
    }

  /**
   * Lazily reconstruct this node. 
   */
  public UnaryExpression reconstruct( Node ext, Expression expr, int operator)
  {
    if( this.expr == expr && this.operator == operator && this.ext == ext) {
      return this;
    }
    else {
      UnaryExpression n = new UnaryExpression( ext, expr, operator);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public UnaryExpression reconstruct( Expression expr, int operator) {
	return reconstruct(this.ext, expr, operator);
    }

  /**
   * Returns the operator corresponding for this expression.
   */ 
  public int getOperator() 
  {
    return operator;
  }

  /**
   * Returns the subexpression.
   */ 
  public Expression getExpression() 
  {
    return expr;
  }

  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v)
  {
    return reconstruct( Node.condVisit(this.ext, v), (Expression)expr.visit( v), operator);
  }

  /**
   * Fold all constants.
   *
   * @return The node with all constants folded in.
   */
  public Node foldConstants() 
  {
    Node newNode = this;
    if (expr instanceof IntLiteral)
    {
      long lValue = ((IntLiteral)expr).getValue();
      switch(operator)
      {
        // FIXME: PROMOTION MAY NOT BE CORRECT
      case BITCOMP:
        newNode = new IntLiteral(null,  ~ lValue);
        break;
      case NEGATIVE:
        newNode = new IntLiteral(null,  - lValue);
        break;
      case POSITIVE:
        newNode = expr;
        break;
      }
    }
    else if (expr instanceof BooleanLiteral && operator == LOGICALNOT)
      newNode = new BooleanLiteral ( ! ((BooleanLiteral)expr).getBooleanValue() );

    // copy the line number:
    if ( newNode != this)
      Annotate.setLineNumber( newNode, Annotate.getLineNumber ( expr ));
    return newNode;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type type = expr.getCheckedType();
    switch( operator)
    {
    case POSTINCR:
    case POSTDECR:
    case PREINCR:
    case PREDECR:
      if( !type.isPrimitive()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      if( !(type.toPrimitiveType()).isNumeric()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      setCheckedType( type);
      break;

    case BITCOMP:
    case NEGATIVE:
    case POSITIVE:
      if( !type.isPrimitive()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      if( !(type.toPrimitiveType()).isNumeric()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be numeric.");
      }
      setCheckedType( PrimitiveType.unaryPromotion( type.toPrimitiveType()));
      break;

    case LOGICALNOT:
      if( !type.isPrimitive()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be boolean.");
      }
      if( !(type.toPrimitiveType()).isBoolean()) {
        throw new SemanticException( "Operand of " 
                + getOperatorName() + " operator must be boolean.");
      }
      setCheckedType( type);
      break;

    default:
      throw new InternalCompilerError( "Unknown unary operator: " + operator);
    }
     
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    if (operator <= NEGATIVE || operator >= PREINCR) {
      /* Prefix operator. */
      if( operator == NEGATIVE) {
        w.write( "-");
      }
      else if( operator == BITCOMP) {
        w.write( "~");
      }
      else if( operator == PREINCR) {
        w.write( "++");
      }
      else if( operator == PREDECR) {
        w.write( "--");
      }
      else if( operator == POSITIVE) {
        w.write( "+");
      }
      else if( operator == LOGICALNOT) {
        w.write( "!");
      }
      
      translateExpression( expr, c, w);
    }
    else {
      /* Postfix operator. */
      translateExpression( expr, c, w);
      
      if( operator == POSTINCR) {
        w.write( "++");
      }
      if( operator == POSTDECR) {
        w.write( "--");
      }
    }
  }

  public void dump( CodeWriter w)
  {
    if( operator == NEGATIVE) {
      w.write( "NEGATIVE ");
    }
    if( operator == BITCOMP) {
      w.write( "BIT-COMPL");
    }
    if( operator == PREINCR) {
      w.write( "PRE-INCR ");
    }
    if( operator == PREDECR) {
      w.write( "PRE-DECR ");
    }
    if( operator == POSTINCR) {
      w.write( "POST-INCR ");
    }
    if( operator == POSTDECR) {
      w.write( "POST-DECR ");
    }
    if( operator == POSITIVE) {
      w.write( "POSITIVE ");
    }
    if( operator == LOGICALNOT) {
      w.write( "LOGICAL-NOT ");
    }
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_UNARY;
  }

  protected String getOperatorName()
  {
    if( operator == NEGATIVE) {
      return "numeric negation";
    }
    if( operator == BITCOMP) {
      return "bitwise complement";
    }
    if( operator == PREINCR) {
      return "prefix increment";
    }
    if( operator == PREDECR) {
      return "prefix decrement";
    }
    if( operator == POSTINCR) {
      return "postfix increment";
    }
    if( operator == POSTDECR) {
      return "postfix decrement";
    }
    if( operator == POSITIVE) {
      return "unary plus";
    }
    if( operator == LOGICALNOT) {
      return "logical negation";
    }
    else {
      return "unknown";
    }
  }
}
