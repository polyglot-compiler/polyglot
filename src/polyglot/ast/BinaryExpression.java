package jltools.ast;

import jltools.util.*;
import jltools.types.*;

/**
 * A <code>BinaryExpression</code> represents a Java binary expression, a
 * immutable pair of expressions combined with an operator.
 */
public class BinaryExpression extends Expression 
{
  public static final int ASSIGN         = 0; // = operator
  public static final int GT             = 1; // > operator
  public static final int LT             = 2; // < opereator
  public static final int EQUAL          = 3; // == operator
  public static final int LE             = 4; // <= operator
  public static final int GE             = 5; // >= operator
  public static final int NE             = 6; // != operator
  public static final int LOGIC_OR       = 7; // || operator
  public static final int LOGIC_AND      = 8; // && operator
  public static final int PLUS           = 9; // + operator
  public static final int SUB            = 10; // - operator
  public static final int MULT           = 11; // * operator
  public static final int DIV            = 12; // / operator
  public static final int BIT_OR         = 13; // | operator
  public static final int BIT_AND        = 14; // & operator
  public static final int BIT_XOR        = 15; // ^ operator
  public static final int MOD            = 16; // % operator
  public static final int LSHIFT         = 17; // << operator
  public static final int RSHIFT         = 18; // >> operator
  public static final int RUSHIFT        = 19; // >>> operator
  public static final int PLUSASSIGN     = 20; // += operator
  public static final int SUBASSIGN      = 21; // -= operator
  public static final int MULTASSIGN     = 22; // *= operator
  public static final int DIVASSIGN      = 23; // /= operator
  public static final int ANDASSIGN      = 24; // &= operator
  public static final int ORASSIGN       = 25; // |= operator
  public static final int XORASSIGN      = 26; // ^= operator
  public static final int MODASSIGN      = 27; // %= operator
  public static final int LSHIFTASSIGN   = 28; // <<= operator
  public static final int RSHIFTASSIGN   = 29; // >>= operator
  public static final int RUSHIFTASSIGN  = 30; // >>>= operator
  
  // Largest operator used.
  protected static final int MAX_OPERATOR   = RUSHIFTASSIGN;

  protected final Expression left, right;
  protected final int operator;

  /**
   * Requires: A valid value for <operator> as listed in public
   *    static ints in this class.
   * Effects: Creates a new BinaryExpression of <operator> applied
   *    to <left> and <right>.
   */
  public BinaryExpression( Expression left, int operator, Expression right) {
    if( left == null || right == null) {
      throw new NullPointerException ("BinaryExpression cannot " +
                                      "take null Expressions.");
    }
    this.left = left;
    this.right = right;
    
    if (operator < 0 || operator > MAX_OPERATOR) {
      throw new IllegalArgumentException("Value for operator to " +
                                         "BinaryExpression not valid.");
    }

    this.operator = operator;
  }

  
  /**
   * Lazily reconstruct this node.
   * <p>
   * If either of the children change (upon visitation) contruct a new node
   * and return it. Otherwise, return <code>this</code>. 
   *
   * @param left The new left-hand side expression.
   * @param right The new right-hand side expression.
   * @param operator The new operator.
   * @return A <code>BinaryExpression</code> with the given expressions and 
   *  operator.
   */
  public BinaryExpression reconstruct( Expression left, int operator,
                                       Expression right)
  {
    if( this.left == left && this.operator == operator && this.right == right)
    {
      return this;
    }
    else {
      BinaryExpression n = new BinaryExpression( left, operator, right);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the left-hand subexpression.
   */
  public Expression getLeftExpression() {
    return left;
  }
  
  /**
   * Returns the right-hand subexpression.
   */
  public Expression getRightExpression() {
    return right;
  }

  /**
   * Returns the operator corresponding to <code>this</code>.
   */
  public int getOperator() {
    return operator;
  }

  /*
   * Visit the children of this node.
   * 
   * @pre Requires that <code>left.visit</code> and <code>right.visit</code> 
   *  both return objects of type <code>Expression</code>.
   * @post Returns <code>this</code> if no changes are made, otherwise a copy
   *  is made and returned.
   */  
  Node visitChildren( NodeVisitor v)
  {
    return reconstruct( (Expression)left.visit( v),
                        operator,
                        (Expression)right.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type ltype = left.getCheckedType(), rtype = right.getCheckedType();
 
    switch(operator)
    {
    case ASSIGN:
      /* See (5.2). */
      if( !rtype.isAssignableSubtype(ltype)) {
        throw new SemanticException( "Unable to assign \"" + 
                                      rtype.getTypeString() + "\" to \""
                                      + ltype.getTypeString() + "\".");
      }
      setCheckedType( ltype);
      right.setExpectedType( ltype);
      break;
      
    case GT:
    case LT:
    case GE:
    case LE:
      /* See (15.19.1). */
      if( !ltype.isPrimitive() || !rtype.isPrimitive()) {
        throw new SemanticException( 
                  "Operands of numeric comparison operators must be numeric.");
      }
      if( !((PrimitiveType)ltype).isNumeric() ||
          !((PrimitiveType)rtype).isNumeric()) {
        throw new SemanticException(
                 "Operands of numeric comparison operators must be numeric.");
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case EQUAL:
    case NE:
      /* See (15.19). */
      if( ltype.isPrimitive()) {
        if( ((PrimitiveType)ltype).isNumeric()) {
          if( !rtype.isPrimitive()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.");
          }
          else if( !((PrimitiveType)rtype).isNumeric()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.");
          }
        }
        else {
          /* ltype is boolean. */
          if( !rtype.isPrimitive()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.");
          }
          else if( ((PrimitiveType)rtype).isNumeric()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.");
          }
        }
      }
      else {
        /* ltype is a reference type. */
        if( rtype.isPrimitive()) {
          throw new SemanticException(
                    "Can only compare two expressions of similar type.");
        }
        else if( !(rtype instanceof ClassType || rtype instanceof NullType)) {
          throw new SemanticException(
                    "Can only compare two expressions of similar type.");
        }
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case LOGIC_OR:
    case LOGIC_AND:
      if( !(ltype.isSameType( c.getTypeSystem().getBoolean()) 
            && rtype.isSameType( c.getTypeSystem().getBoolean()))) {
        throw new SemanticException(
                  "Operands of logical operator must be boolean.");
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case PLUS:
    case PLUSASSIGN:
      /* (15.17). */
      Type string = c.getType( "java.lang.String");
      if( ltype.isSameType( string)) {
        setCheckedType( string);
        break;
      }
      else if( rtype.isSameType( string)) {
        setCheckedType( string);
        break;
      }
    case SUB:
    case SUBASSIGN:
      if( !ltype.isPrimitive()) {
        throw new SemanticException( 
                  "Additive operators must have numeric operands.");
      }
      else if( !((PrimitiveType)ltype).isNumeric()) {
        throw new SemanticException( 
                  "Additive operators must have numeric operands.");
      }
      else if( !rtype.isPrimitive()) {
        throw new SemanticException(
                  "Additive operators must have numeric operands.");
      }
      else if( !((PrimitiveType)rtype).isNumeric()) {
        throw new SemanticException(
                  "Additive operators must have numeric operands.");
      }
      else {
        setCheckedType( PrimitiveType.binaryPromotion( (PrimitiveType)ltype,
                                                (PrimitiveType)rtype));
      }
      break;
      
    case MULT:
    case MULTASSIGN:
    case DIV:
    case DIVASSIGN:
    case MOD:
    case MODASSIGN:
      if( !(ltype.isPrimitive() && rtype.isPrimitive())) {
        throw new SemanticException(
                  "Expected numeric operands to multiplicative operator.");
      }
      else if( !(((PrimitiveType)ltype).isNumeric()
                 && ((PrimitiveType)rtype).isNumeric())) {
        throw new SemanticException(
                  "Expected numeric operands to multiplicative operator.");
      }
      else {
        setCheckedType( PrimitiveType.binaryPromotion( (PrimitiveType)ltype,
                                                (PrimitiveType)rtype));
      }
      break;
      
    case BIT_OR:
    case ORASSIGN:
    case BIT_AND:
    case ANDASSIGN:
    case BIT_XOR:
    case XORASSIGN:
      /* Either both are either numeric or boolean (15.21). */
      if( !(ltype.isPrimitive() && rtype.isPrimitive())) {
        throw new SemanticException(
                 "Expected primitive operands to bitwise binary operator.");
      }
      else if( ((PrimitiveType)ltype).isNumeric()
               && ((PrimitiveType)rtype).isNumeric()) {
        setCheckedType( PrimitiveType.binaryPromotion( (PrimitiveType)ltype,
                                                (PrimitiveType)rtype));
      }
      else if( ((PrimitiveType)ltype).isBoolean() 
               && ((PrimitiveType)rtype).isBoolean()) {
        setCheckedType( c.getTypeSystem().getBoolean());
      }
      else {
        throw new SemanticException(
            "Bitwise operators require two boolean or two numeric operands.");
      }
      break;
      
    case LSHIFT:
    case LSHIFTASSIGN:
    case RSHIFT:
    case RSHIFTASSIGN:
    case RUSHIFT:
    case RUSHIFTASSIGN:
      if( !(ltype.isPrimitive() && rtype.isPrimitive())) {
        throw new SemanticException(
                  "Expected numeric operands to shift operator.");
      }
      else if( !(((PrimitiveType)ltype).isNumeric()
                 && ((PrimitiveType)rtype).isNumeric())) {
        throw new SemanticException(
                  "Expected numeric operands to shift operator.");
      }
      else {
        setCheckedType( PrimitiveType.unaryPromotion( (PrimitiveType)ltype));
      }
      break;
      
    default:
      throw new SemanticException(
                "Internal error: unknown binary operator.");
    }
    
    return this;
  }
  
   public void translate(LocalContext c, CodeWriter w) 
   {
     /* Extra checks are need here to see if we have a numeric binary
      * expression inside of a String concatenation. */
     try
     {
       if( getCheckedType().equals( c.getType( "java.lang.String")) 
           && left.getCheckedType().isPrimitive()) {
         w.write( "(");
         left.translate_block( c, w);
         w.write( ")");
       }
       else {
         translateExpression( left, c, w);
       }
       
       w.write( " ");
       w.write( getOperatorString( operator));
       w.allowBreak(2, " ");
       
       if( getCheckedType().equals( c.getType( "java.lang.String")) 
           && right.getCheckedType().isPrimitive()) {
         w.write( "(");
	 right.translate_block( c, w);
         w.write( ")");
       }
       else {
         translateExpression( right, c, w);
       }
     }
     catch( SemanticException e) 
     {
       throw new InternalCompilerError( 
                          "Caught SemanticException during translation: " 
                          + e.getMessage());
     }
   }

   public void dump( CodeWriter w)
   {
      w.write( "( BINARY EXPR < ");
      w.write( getOperatorString(operator));
      w.write( " > ");
      dumpNodeInfo( w);
      w.write( ")");
   }

  public int getPrecedence()
  {
    switch( operator)
    {
       case MULT:
    case DIV:
    case MOD:
      return PRECEDENCE_MULT;

    case PLUS:
    case SUB:
      return PRECEDENCE_ADD;

    case LSHIFT:
    case RSHIFT:
    case RUSHIFT:
      return PRECEDENCE_SHIFT;

    case GT:
    case LT:
    case LE:
    case GE:
      return PRECEDENCE_INEQUAL;

    case EQUAL:
    case NE:
      return PRECEDENCE_EQUAL;

    case BIT_AND:
      return PRECEDENCE_BAND;

    case BIT_XOR:
      return PRECEDENCE_BXOR;

    case BIT_OR:
      return PRECEDENCE_BOR;

    case LOGIC_AND:
      return PRECEDENCE_CAND;

    case LOGIC_OR:
      return PRECEDENCE_COR;

    case ASSIGN:
    case PLUSASSIGN:
    case SUBASSIGN:
    case MULTASSIGN:
    case DIVASSIGN:
    case ANDASSIGN:
    case ORASSIGN:
    case XORASSIGN:
    case MODASSIGN:
    case LSHIFTASSIGN: 
    case RSHIFTASSIGN: 
    case RUSHIFTASSIGN:
      return PRECEDENCE_ASSIGN;
     
    default:
      return PRECEDENCE_UNKNOWN;
    }
  }

  private static String getOperatorString( int operator)
  {
    switch( operator)
    {
    case ASSIGN:
      return "=";
    case GT:
      return ">";
    case LT:
      return "<";
    case EQUAL:
      return "==";
    case LE:
      return "<=";
    case GE:
      return ">=";
    case NE:
      return "!=";
    case LOGIC_OR:
      return "||";
    case LOGIC_AND:
      return "&&";
    case MULT:
      return "*";
    case DIV:
      return "/";
    case BIT_OR:
      return "|";
    case BIT_AND:
      return "&";
    case BIT_XOR:
      return "^";
    case MOD:
      return "%";
    case LSHIFT:
      return "<<";
    case RSHIFT:
      return ">>";
    case RUSHIFT:
      return ">>>";
    case PLUSASSIGN:
      return "+=";
    case SUBASSIGN:
      return "-=";
    case MULTASSIGN:
      return "*=";
    case DIVASSIGN:
      return "/=";
    case ANDASSIGN:
      return "&=";
    case ORASSIGN:
      return "|=";
    case XORASSIGN:
      return "^=";
    case MODASSIGN:
      return "%=";
    case LSHIFTASSIGN:
      return "<<=";
    case RSHIFTASSIGN:
      return ">>=";
    case RUSHIFTASSIGN:
      return ">>>=";
    case PLUS:
      return "+";
    case SUB:
      return "-";      
    default:
      return "???";
    }
  }
}
