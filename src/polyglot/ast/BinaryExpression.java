package jltools.ast;

import jltools.util.*;
import jltools.visit.*;
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
  public BinaryExpression( Node ext, Expression left, int operator, Expression right) {
    if( left == null || right == null) {
	System.out.println("left = "+left);
	System.out.println("op = "+getOperatorString(operator));
	System.out.println("right = " + right);
      throw new NullPointerException ("BinaryExpression cannot " +
                                      "take null Expressions.");
    }
    this.ext = ext;
    this.left = left;
    this.right = right;
    
    if (operator < 0 || operator > MAX_OPERATOR) {
      throw new IllegalArgumentException("Value for operator to " +
                                         "BinaryExpression not valid.");
    }

    this.operator = operator;
  }

  public BinaryExpression( Expression left, int operator, Expression right) {  
      this(null, left, operator, right);
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
  public BinaryExpression reconstruct( Node ext, Expression left, int operator,
                                       Expression right)
  {
    if( this.left == left && this.operator == operator && this.right == right && this.ext == ext)
    {
      return this;
    }
    else {
      BinaryExpression n = new BinaryExpression( ext, left, operator, right);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public BinaryExpression reconstruct( Expression left, int operator,
                                       Expression right) {
      return reconstruct(this.ext, left, operator, right);
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
  public Node visitChildren( NodeVisitor v)
  {
    return reconstruct( Node.condVisit(this.ext, v), (Expression)left.visit( v),
                        operator,
                        (Expression)right.visit( v));
  }

  /**
   * Fold all constants.
   *
   * @return The node with all constants folded in.
   */
  public Node foldConstants(ExtensionFactory ef)
  {
    Expression newNode = this;

    if ( (left instanceof NumericalLiteral) &&
         (right instanceof NumericalLiteral))
    {
      long lLeft = ((NumericalLiteral)left).getValue();
      long lRight = ((NumericalLiteral)right).getValue();

      switch( operator)
      {
      case GT:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft > lRight );
        break;
      case LT:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft < lRight);
        break;
      case EQUAL:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft == lRight);
        break;
      case LE:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft <= lRight);
        break;
      case GE:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft >= lRight);
        break;
      case NE:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), lLeft != lRight);
        break;
      case MULT:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft * lRight);
        break;
      case DIV:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft / lRight);
        break;
        // FIXME: MAY NOT BE CORRECT: read jls
      case BIT_OR:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft | lRight);
        break;      
      case BIT_AND:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft & lRight);
        break;
      case BIT_XOR:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft ^ lRight);      
        break;
      case MOD:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft % lRight);
        break;
      case LSHIFT:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft << lRight);
        break;
      case RSHIFT:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft >> lRight);
        break;
      case RUSHIFT:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft >>> lRight);
        break;
      case PLUS:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft + lRight);
        break;
      case SUB:
        newNode = new IntLiteral( ef.getNewLiteralExtension(), lLeft - lRight);
      }
    }      
    else if ( (left instanceof BooleanLiteral) &&
         (right instanceof BooleanLiteral))
    {
      boolean bLeft = ((BooleanLiteral)left).getBooleanValue(), 
        bRight = ((BooleanLiteral)right).getBooleanValue();

      switch( operator)
      {
      case LOGIC_OR:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), bLeft || bRight);
        break;
      case LOGIC_AND:
        newNode = new BooleanLiteral( ef.getNewLiteralExtension(), bLeft && bRight);
      }
    }
    else if ( ( left instanceof StringLiteral) &&
              ( right instanceof StringLiteral)) {
      newNode = new StringLiteral( ef.getNewLiteralExtension(), ((StringLiteral)left).getString()
                                + ((StringLiteral)right).getString());

    }

    // copy the line number:
    if ( newNode != this)
      Annotate.setLineNumber( newNode, Annotate.getLineNumber ( left ));
    return newNode;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type ltype = left.getCheckedType(), rtype = right.getCheckedType();
	
    switch(operator)
    {
    case ASSIGN:
      /* See (5.2). */
      if( !rtype.isAssignableSubtype(ltype) &&
	  !rtype.isSameType(ltype) &&
          ! ( right instanceof NumericalLiteral && 
              c.getTypeSystem().numericConversionValid(ltype, 
                                   ((NumericalLiteral)right).getValue()))   ) {
        throw new SemanticException( "Unable to assign \"" + 
                                      rtype.getTypeString() + "\" to \""
                                      + ltype.getTypeString() + "\".",
				     Annotate.getLineNumber(this) );
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
                  "Operands of numeric comparison operators must be numeric.",
				     Annotate.getLineNumber(this) );
      }
      if( !(ltype.toPrimitiveType()).isNumeric() ||
          !(rtype.toPrimitiveType()).isNumeric()) {
        throw new SemanticException(
                 "Operands of numeric comparison operators must be numeric.",
				     Annotate.getLineNumber(this) );
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case EQUAL:
    case NE:
      /* See (15.19). */
      if( ltype.isPrimitive()) {
        if( (ltype.toPrimitiveType()).isNumeric()) {
          if( !rtype.isPrimitive()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
          }
          else if( !(rtype.toPrimitiveType()).isNumeric()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
          }
        }
        else {
          /* ltype is boolean. */
          if( !rtype.isPrimitive()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
          }
          else if( (rtype.toPrimitiveType()).isNumeric()) {
            throw new SemanticException(
                      "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
          }
        }
      }
      else {
        /* ltype is a reference type. */
        if( rtype.isPrimitive()) {
          throw new SemanticException(
                    "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
        }
        else if( !(rtype.isReferenceType() || rtype instanceof NullType)) {
          throw new SemanticException(
                    "Can only compare two expressions of similar type.",
				     Annotate.getLineNumber(this) );
        }
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case LOGIC_OR:
    case LOGIC_AND:
      if( !(ltype.isSameType( c.getTypeSystem().getBoolean()) 
            && rtype.isSameType( c.getTypeSystem().getBoolean()))) {
        throw new SemanticException(
                  "Operands of logical operator must be boolean.",
				     Annotate.getLineNumber(this) );
      }
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
      
    case PLUS:
    case PLUSASSIGN:
      /* (15.17). */
      Type string = c.getTypeSystem().getString();
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
                  "Additive operators must have numeric operands.",
				     Annotate.getLineNumber(this) );
      }
      else if( !(ltype.toPrimitiveType()).isNumeric()) {
        throw new SemanticException( 
                  "Additive operators must have numeric operands.",
				     Annotate.getLineNumber(this) );
      }
      else if( !rtype.isPrimitive()) {
        throw new SemanticException(
                  "Additive operators must have numeric operands.",
				     Annotate.getLineNumber(this) );
      }
      else if( !(rtype.toPrimitiveType()).isNumeric()) {
        throw new SemanticException(
                  "Additive operators must have numeric operands.",
				     Annotate.getLineNumber(this) );
      }
      else {
        setCheckedType( PrimitiveType.binaryPromotion( ltype.toPrimitiveType(),
                                                rtype.toPrimitiveType()));
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
                  "Expected numeric operands to multiplicative operator.",
				     Annotate.getLineNumber(this) );
      }
      else if( !((ltype.toPrimitiveType()).isNumeric()
                 && (rtype.toPrimitiveType()).isNumeric())) {
        throw new SemanticException(
                  "Expected numeric operands to multiplicative operator.",
				     Annotate.getLineNumber(this) );
      }
      else {
        setCheckedType( PrimitiveType.binaryPromotion( ltype.toPrimitiveType(),
                                                rtype.toPrimitiveType()));
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
                 "Expected primitive operands to bitwise binary operator.",
				     Annotate.getLineNumber(this) );
      }
      else if( (ltype.toPrimitiveType()).isNumeric()
               && (rtype.toPrimitiveType()).isNumeric()) {
        setCheckedType( PrimitiveType.binaryPromotion( ltype.toPrimitiveType(),
                                                rtype.toPrimitiveType()));
      }
      else if( (ltype.toPrimitiveType()).isBoolean() 
               && (rtype.toPrimitiveType()).isBoolean()) {
        setCheckedType( c.getTypeSystem().getBoolean());
      }
      else {
        throw new SemanticException(
            "Bitwise operators require two boolean or two numeric operands.",
				     Annotate.getLineNumber(this) );
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
                  "Expected numeric operands to shift operator.",
				     Annotate.getLineNumber(this) );
      }
      else if( !((ltype.toPrimitiveType()).isNumeric()
                 && (rtype.toPrimitiveType()).isNumeric())) {
        throw new SemanticException(
                  "Expected numeric operands to shift operator.",
				     Annotate.getLineNumber(this) );
      }
      else {
        setCheckedType( PrimitiveType.unaryPromotion( ltype.toPrimitiveType()));
      }
      break;
      
    default:
      throw new SemanticException(
                "Internal error: unknown binary operator.",
				     Annotate.getLineNumber(this) );
    }
    
    return this;
  }
  
   public void translate_no_override(LocalContext c, CodeWriter w) 
   {
     /* Extra checks are needed here to see if we have a numeric binary
      * expression inside of a String concatenation. If so they must
      * be parenthesized. */
        boolean str = getCheckedType().equals(c.getTypeSystem().getString());
	if (str && left.getCheckedType().isPrimitive()) {
	    w.write("(");
	    left.translate_block( c, w);
	    w.write(")");
	} else {
	    translateExpression( left, c, w);
	}
	w.write(" ");
	w.write( getOperatorString( operator));
	w.allowBreak(str ? 0 : 2, " ");
	if (str && right.getCheckedType().isPrimitive()) {
	    w.write("(");
	    right.translate_block(c, w);
	    w.write(")");
	} else {
	    translateExpression(right, c, w);
	}
   }

   public void dump( CodeWriter w)
   {
      w.write( "BINARY EXPR < ");
      w.write( getOperatorString(operator));
      w.write( " > ");
      dumpNodeInfo( w);
      w.write( ")");
   }
   
   public String toString() {
	   String str = left.toString();
	   str += getOperatorString(operator);
	   str += right.toString();
	   return str;
   }

    public boolean isAssignment() {
	switch( operator) {
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
	    return true;
	default: return false;
	}
    }

    public Node exceptionCheck(ExceptionChecker ec)
      throws SemanticException
    {
      TypeSystem ts = ec.getTypeSystem();

      if (throwsArithmeticException()) {
	ec.throwsException((ClassType) ts.getArithmeticException());
      }

      if (throwsArrayStoreException()) {
	ec.throwsException((ClassType) ts.getArrayStoreException());
      }

      return this;
    }

    public boolean throwsArrayStoreException() {
      return operator == ASSIGN && left.getCheckedType().isReferenceType() &&
	  left instanceof ArrayIndexExpression;
    }

    public boolean throwsArithmeticException() {
	// conservatively assume that any division or mod may throw ArithmeticException
	// this is NOT true-- floats and doubles don't throw any exceptions ever...
	if (operator == DIV || operator == MOD ||
	    operator == DIVASSIGN || operator == MODASSIGN) return true;
	else return false;
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
