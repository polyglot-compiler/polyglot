package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * An immutable representation of a Java language <code>synchronized</code>
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is <code>true</code>.
 */
public class SynchronizedStatement extends Statement 
{
  protected final Expression expr;
  protected final BlockStatement body;
  
  /**
   * Creates a new <code>SynchronizedStatement</code>.
   */
  public SynchronizedStatement( Expression expr, BlockStatement body) 
  {
    this.expr = expr;
    this.body = body;
  }
  
  /**
   * Lazily reconstruct this node.
   */
  public SynchronizedStatement reconstruct( Expression expr, 
                                            BlockStatement body)
  {
    if( this.expr == expr && this.body == body) {
      return this;
    }
    else {
      SynchronizedStatement n = new SynchronizedStatement( expr, body);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the <code>Expression</code> that this 
   * <code>SynchronizedStatement</code> is synchronized on.
   */
  public Expression getExpression() 
  {
    return expr;
  }

  /**
   * Returns the block associated with this statement.
   */
  public BlockStatement getBody() 
  {
    return body;
  }

  /**
   * Visit the children of this node.
   */
  Node visitChildren( NodeVisitor v)
  {
    return reconstruct( (Expression)expr.visit( v),
                        (BlockStatement)body.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    if( !( expr.getCheckedType().descendsFrom( c.getTypeSystem().getObject()))
        && !(expr.getCheckedType().equals( c.getTypeSystem().getObject()))) {
       throw new SemanticException( "The type of the expression \"" + 
                                     expr.getCheckedType().getTypeString() + 
                                     "\" is not valid to synchronize on.");
    }
    expr.setExpectedType( c.getTypeSystem().getObject()) ;
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "synchronized (");
    expr.translate( c, w);
    w.write( ") ");
    if( !(body instanceof BlockStatement)) {
      w.beginBlock();
      body.translate( c, w);
      w.endBlock();
    }
    else {
      body.translate( c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "( SYNCHRONIZED ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

