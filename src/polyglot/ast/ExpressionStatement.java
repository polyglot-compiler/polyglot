package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * An <code>ExpressionStatement</code> is a wrapper for an expression (a
 * sequence of Java code that yeilds a value) in the context of a statement.
 */
public class ExpressionStatement extends Statement 
{
  protected final Expression expr;

  /**
   * Effects: Create a new ExpressionStatement.
   **/
  public ExpressionStatement( Node ext, Expression expr)
  {
    this.ext = ext;
    this.expr = expr;
  }

    public ExpressionStatement( Expression expr) {
	this(null, expr);
    }

  /**
   * Lazily reconstuct this node. 
   */
  public ExpressionStatement reconstruct( Node ext, Expression expr) 
  {
    if( this.expr == expr && this.ext == ext) {
      return this;
    }
    else {
      ExpressionStatement n = new ExpressionStatement( ext, expr);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public ExpressionStatement reconstruct( Expression expr) {
	return reconstruct(this.ext, expr);
    }

  /** 
   * Returns the underlying expression of this 
   * <code>ExpressionStatement</code>.
   */
  public Expression getExpression() 
  {
    return expr;
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>expr.visit</code> returns an object of type
   *  <code>Expression</code>.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),(Expression)expr.visit( v));
  }

  public Node typeCheck( LocalContext c)
  {
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    expr.translate( c, w);
    w.write( ";");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( EXPR STMT ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

