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
  public ThrowStatement( Node ext, Expression expr) 
  {
    this.ext = ext;
    this.expr = expr;
  }
  
    public ThrowStatement( Expression expr) {
	this(null, expr);
    }

  /**
   * Lazily reconstruct this node.
   */
  public ThrowStatement reconstruct( Node ext, Expression expr)
  {
    if( this.expr == expr && this.ext == ext) {
      return this;
    }
    else {
      ThrowStatement n = new ThrowStatement( ext, expr);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public ThrowStatement reconstruct( Expression expr) {
	return reconstruct(this.ext, expr);
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
    return reconstruct( Node.condVisit(this.ext, v), (Expression)expr.visit( v));
  }

  public Node typeCheck( LocalContext c)
  {
    return this;
  }

  public Node exceptionCheck( ExceptionChecker ec ) throws SemanticException
  {
     if (! expr.getCheckedType().isThrowable())
       throw new SemanticException("Can only throw objects that extend from " 
                                    + "\"java.lang.Throwable\"",
				    Annotate.getLineNumber(expr));
     else
       ec.throwsException ( (ClassType)expr.getCheckedType() );
     return this;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    w.write("throw ");
    expr.translate(c, w);
    w.write("; ");
  }

  public String toString() {
    return "throw " + expr + ";";
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "THROW ");
    dumpNodeInfo( w);
  }
}
  

  
