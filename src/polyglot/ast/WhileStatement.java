package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * An immutable representation of a Java language <code>while</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */ 
public class WhileStatement extends Statement 
{
  protected final Expression cond;
  protected final Statement body;

  /**
   * Creates a new <code>WhileStatement</code>.
   */
  public WhileStatement( Expression cond, Statement body) 
  {
    this.cond = cond;
    this.body = body;
  }

  /**
   * Lazily reconstruc this node.
   */
  public WhileStatement reconstruct( Expression cond, Statement body)
  {
    if( this.cond == cond && this.body == body) {
      return this;
    }
    else {
      WhileStatement n = new WhileStatement( cond, body);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the expression that this statement is conditioned on.
   */
  public Expression getCondition() 
  {
    return cond;
  }

  /**
   * Returns the body associated with this statement.
   */
  public Statement getBody() 
  {
    return body;
  }

  /** 
   * Visit the children of this node.
   *
   * @pre Requires that <code>cond.visit</code> returns an object of type
   *  <code>Expression</code> and that, if <code>body</code> is non-
   *  <code>null</code>, <code>body.visit</code> returns an object
   *  of type <code>Statement</code>.
   */
  Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( (Expression)cond.visit( v),
                        (body == null ? null : (Statement)body.visit( v)));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type ctype = cond.getCheckedType();
    
    if( !ctype.equals( c.getTypeSystem().getBoolean())) {
      throw new SemanticException( "Condition expression of while statement "
                                    + "must have boolean type.");
    }
    
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "while( " );
    cond.translate( c, w);
    w.write( ")");

    if( body == null){
      w.write( "; ");
      return;
    }

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
    w.write( "( WHILE ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
