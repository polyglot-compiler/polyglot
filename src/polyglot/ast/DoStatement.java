package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/**
 * A immutable representation of a Java language <code>do</code> statement. 
 * Contains a statement to be executed and an expression to be tested 
 * indicating whether to reexecute the statement.
 */ 
public class DoStatement extends Statement 
{
  /** The statement which is iteratively executed. */
  protected Statement stmt;
  /** The conditional expression of this statement. */
  protected Expression cond;

  /**
   * Creates a new <code>DoStatement</code> with a statement <code>statement>,
   *    and a conditional expression <code>cond</code>.
   */
  public DoStatement( Statement stmt, Expression cond) 
  {
    this.stmt = stmt;
    this.cond = cond;
  }

  /** 
   * Lazily reconstuct this node.
   */
  public DoStatement reconstruct( Statement stmt, Expression cond)
  {
    if( this.stmt == stmt && this.cond == cond) {
      return this;
    }
    else {
      DoStatement n = new DoStatement( stmt, cond);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the <code>Expression</code> that this <code>DoStatement</code>
   * is conditioned on.
   */
  public Expression getCondition() 
  {
    return cond;
  }

  /** 
   * Returns the statement associated with this <code>DoStatement</code>.
   */
  public Statement getBody() 
  {
    return stmt;
  }

  /** 
   * Visit the children of this node.
   *
   * @pre Requires that <code>stmt.visit</code> returns an object of type
   *  <code>Statement</code> and that <code>cond.visit</code> returns an
   *  object of type <code>Expression</code>.
   * @post Returns <code>this</code> if no changes are made, otherwise a copy
   *  is made and returned.
   */  
  Node visitChildren(NodeVisitor v) 
  {
    return reconstruct( (Statement)stmt.visit( v),
                        (Expression)cond.visit( v));
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write( "do ");
    stmt.translate_substmt( c, w);
    w.write( "while(");
    cond.translate_block( c, w);
    w.write( "); ");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( DO ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
