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
  public SynchronizedStatement( Node ext, Expression expr, BlockStatement body) 
  {
    this.ext = ext;
    this.expr = expr;
    this.body = body;
  }
  
    public SynchronizedStatement( Expression expr, BlockStatement body) {
	this(null, expr, body);
    }

  /**
   * Lazily reconstruct this node.
   */
  public SynchronizedStatement reconstruct( Node ext, Expression expr, 
                                            BlockStatement body)
  {
    if( this.expr == expr && this.body == body && this.ext == ext) {
      return this;
    }
    else {
      SynchronizedStatement n = new SynchronizedStatement( ext, expr, body);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public SynchronizedStatement reconstruct( Expression expr, 
                                            BlockStatement body) {
      return reconstruct(this.ext, expr, body);
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
  public Node visitChildren( NodeVisitor v)
  {
    return reconstruct( Node.condVisit(this.ext, v), (Expression)expr.visit( v),
                        (BlockStatement)body.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    if( !( expr.getCheckedType().descendsFrom( c.getTypeSystem().getObject()))
        && !(expr.getCheckedType().equals( c.getTypeSystem().getObject()))) {
       throw new SemanticException( "The type of the expression \"" + 
                                     expr.getCheckedType().getTypeString() + 
                                     "\" is not valid to synchronize on.",
				    Annotate.getPosition(expr));
    }
    expr.setExpectedType( c.getTypeSystem().getObject()) ;
    return this;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    w.write( "synchronized (");
    expr.translate_block( c, w);
    w.write( ") ");
    body.translate_substmt( c, w);
  }

  public String toString() {
    return "synchronized (" + expr + ") " + body;
  }

  public void dump( CodeWriter w)
  {
    w.write( "SYNCHRONIZED ");
    dumpNodeInfo( w);
  }
}

