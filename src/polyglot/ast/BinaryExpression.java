/*
 * BinaryExpression.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;

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

    public static final int PLUS           = 29; // + operator
    public static final int SUB            = 30; // - operator

    // Largest operator used.
    public static final int MAX_OPERATOR   = SUB;

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

  /**
   *
   */
  Object visitChildren(NodeVisitor v)
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    left = (Expression)left.visit( v);
    vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( left), vinfo);

    right = (Expression)right.visit( v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( right), vinfo);
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
      return 0;
    }
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    Type ltype = left.getCheckedType(), rtype = right.getCheckedType();
    
    switch(operator)
    {
    case ASSIGN:
      /* See (5.2). */
      if( !rtype.isAssignableSubtype(ltype)) {
        throw new TypeCheckException( "Unable to assign \"" + 
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
        throw new TypeCheckException( 
                  "Operands of numeric comparison operators must be numeric.");
      }
      if( !((PrimitiveType)ltype).isNumeric() ||
          !((PrimitiveType)rtype).isNumeric()) {
        throw new TypeCheckException(
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
            throw new TypeCheckException(
                      "Can only compare two expressions of similar type.");
          }
          else if( !((PrimitiveType)rtype).isNumeric()) {
            throw new TypeCheckException(
                      "Can only compare two expressions of similar type.");
          }
        }
        else {
          /* ltype is boolean. */
          if( !rtype.isPrimitive()) {
            throw new TypeCheckException(
                      "Can only compare two expressions of similar type.");
          }
          else if( ((PrimitiveType)rtype).isNumeric()) {
            throw new TypeCheckException(
                      "Can only compare two expressions of similar type.");
          }
        }
      }
      else {
        /* ltype is a reference type. */
        if( rtype.isPrimitive()) {
          throw new TypeCheckException(
                    "Can only compare two expressions of similar type.");
        }
        else if( !(ltype.isCastValid( rtype) || rtype.isCastValid( ltype))) {
          throw new TypeCheckException(
                    "Can only compare two expressions of similar type.");
        }
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case LOGIC_OR:
    case LOGIC_AND:
      if( !(ltype.isSameType( c.getTypeSystem().getBoolean()) 
            && rtype.isSameType( c.getTypeSystem().getBoolean()))) {
        throw new TypeCheckException(
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
        throw new TypeCheckException( 
                  "Additive operators must have numeric operands.");
        //      setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
      }
      else if( !((PrimitiveType)ltype).isNumeric()) {
        throw new TypeCheckException( 
                  "Additive operators must have numeric operands.");
        //      setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
      }
      else if( !rtype.isPrimitive()) {
        throw new TypeCheckException(
                  "Additive operators must have numeric operands.");
        //        setCheckedType( ltype);
      }
      else if( !((PrimitiveType)rtype).isNumeric()) {
        throw new TypeCheckException(
                  "Additive operators must have numeric operands.");
        //        setCheckedType( ltype);
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
        throw new TypeCheckException(
                  "Expected numeric operands to multiplicative operator.");
        //        setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
      }
      else if( !(((PrimitiveType)ltype).isNumeric()
                 && ((PrimitiveType)rtype).isNumeric())) {
        throw new TypeCheckException(
                  "Expected numeric operands to multiplicative operator.");
        //        setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
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
        throw new TypeCheckException(
                 "Expected primitive operands to bitwise binary operator.");
        //        setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
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
        throw new TypeCheckException(
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
        throw new TypeCheckException(
                  "Expected numeric operands to shift operator.");
        //        setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
      }
      else if( !(((PrimitiveType)ltype).isNumeric()
                 && ((PrimitiveType)rtype).isNumeric())) {
        throw new TypeCheckException(
                  "Expected numeric operands to shift operator.");
        //        setCheckedType( c.getTypeSystem().getVoid()); /* FIXME */
      }
      else {
        setCheckedType( PrimitiveType.unaryPromotion( (PrimitiveType)ltype));
      }
      break;
      
    default:
      /* FIXME */
      throw new TypeCheckException(
                "Internal error: unknown binary operator.");
    }
    
    addThrows (left.getThrows() );
    addThrows (right.getThrows() );
    return this;
  }
  
   public void translate(LocalContext c, CodeWriter w) 
   {
     translateExpression( left, c, w);

     w.write( " ");
     w.write( getOperatorString( operator));
     w.write( " ");

     translateExpression( right, c, w);
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( BINARY EXPR < ");
      w.write( getOperatorString(operator));
      w.write( " > ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
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
