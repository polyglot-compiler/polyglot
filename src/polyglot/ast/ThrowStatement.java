package jltools.ast;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * A <code>ThrowStatement</code> is an immutable representation of a
 * <code>throw</code> statement. Such a statement  contains a single
 * <code>Expression</code> which evaluates to the object being thrown.
 */
public class ThrowStatement extends Statement 
{
  protected final Expression expr;
  
  /**
   * Creates a new <code>ThrowStatement</code>.
   */
  public ThrowStatement( Expression expr) 
  {
    this.expr = expr;
  }
  
  /**
   * Lazily reconstruct this node.
   */
  public ThrowStatement reconstruct( Expression expr)
  {
    if( this.expr == expr) {
      return this;
    }
    else {
      ThrowStatement n = new ThrowStatement( expr);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /** 
   * Returns the expression whose value is thrown by this statement.
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
    return reconstruct( (Expression)expr.visit( v));
  }

  public Node typeCheck( LocalContext c)
  {
    return this;
  }

  public Node exceptionCheck( ExceptionChecker ec ) throws SemanticException
  {
     if (! expr.getCheckedType().isThrowable())
       throw new SemanticException("Can only throw objects that extend from " 
                                    + "\"java.lang.Throwable\"");
     else
       ec.throwsException ( (ClassType)expr.getCheckedType() );
     return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write("throw ");
    expr.translate(c, w);
    w.write("; ");
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "( THROW ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
  

  
