package jltools.ast;

import jltools.util.*;
import jltools.types.*;

/**
 * A <code>TernaryExpression</code> represents a Java ternary expression as
 * an immutable triple of expressions.
 */
public class TernaryExpression extends Expression 
{
  protected final Expression cond;
  protected final Expression second;
  protected final Expression third;
    
  /**
   * Creates a new <code>TernaryExpression</code>.
   */
  public TernaryExpression( Expression cond, Expression second,
                            Expression third) 
  {
    this.cond = cond;
    this.second = second;
    this.third = third;
  }
  
  /**
   * Lazily reconstruct this node.
   */
  public TernaryExpression reconstruct( Expression cond, Expression second, 
                                        Expression third)
  {
    if( this.cond == cond && this.second == second && this.third == third) {
      return this;
    }
    else {
      TernaryExpression n = new TernaryExpression( cond, second, third);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the condition subexpression.
   */ 
  public Expression getCondition()
  {
    return cond;
  }

  /**
   * Returns the result if the condition is true.
   */ 
  public Expression getTrueResult() 
  {
    return second;
  }
    
  /**
   * Returns the result if the condition is false.
   */ 
  public Expression getFalseResult() 
  {
    return third;
  }

  public Node visitChildren( NodeVisitor v)
  {
    return reconstruct( (Expression)cond.visit( v),
                        (Expression)second.visit( v),
                        (Expression)third.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    if( !cond.getCheckedType().equals( c.getTypeSystem()
                                                .getBoolean())) {
       throw new SemanticException( 
                              "Ternary condition must be of type boolean.");
    }
     
    setCheckedType( c.getTypeSystem().leastCommonAncestor( 
                                               second.getCheckedType(), 
                                               third.getCheckedType()));

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    translateExpression( cond, c, w);
    
    w.write( " ? ");
    
    translateExpression( second, c, w);
    
    w.write( " : ");
    
    translateExpression( third, c, w);
  }

  public void dump( CodeWriter w)
  {
    w.write( "( TERNARY ");
    dumpNodeInfo( w);
    w.write( ")");
  }

  public int getPrecedence()
  {
    return PRECEDENCE_TERN;
  }
}
