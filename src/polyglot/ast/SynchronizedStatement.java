/*
 * SynchronizedStatement.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;

/**
 * SynchronizedStatement
 *
 * Overview: A mutable representation of a Java language synchronized block.
 *   Contains an expression being tested and a statement to be executed
 *   while the expression is true.
 */
public class SynchronizedStatement extends Statement {
  /**
   * Effects: Creates a new SynchronizedStatment with expression
   *    <expr>, and a statement <body>.
   */
  public SynchronizedStatement (Expression expr, BlockStatement body) {
    this.expr = expr;
    this.body = body;
  }

  /**
   * Effects: Returns the Expression that this SynchronizedStatement is
   * conditioned on.
   */
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects: Sets the expression that this Synchronized statement
   * synchronizes on to <newExpr>.  
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

  /**
   * Effects: Returns the statement associated with this
   *    SynchronizedStatement.
   */
  public BlockStatement getBody() {
    return body;
  }

  /**
   * Effects: Sets the statement of this SynchronizedStatement to be
   *    <newStatement>.
   */
  public void setStatement(BlockStatement newBody) {
    body = newBody;
  }

  Object visitChildren(NodeVisitor v)
  {
    Object vinfo = Annotate.getVisitorInfo( this);

    expr = (Expression) expr.visit( v);
    vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( expr), vinfo);

    body = (BlockStatement) body.visit( v);;
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( body), vinfo);
  }

   public Node typeCheck(LocalContext c) throws TypeCheckException
   {
     if ( !( expr.getCheckedType() instanceof ClassType))
       throw new TypeCheckException ("The type of the expression \"" + expr.getCheckedType() + 
                                     "\" is not valid to synchronize on.");
     addThrows ( expr.getThrows() );
     addThrows ( body.getThrows() );
     Annotate.setTerminatesOnAllPaths (this, Annotate.terminatesOnAllPaths ( body ) );

      return this;
   }

   public void  translate(LocalContext c, CodeWriter w)
   {
      w.write ("synchronized (") ;
      expr.translate(c, w);
      w.write(")");
      w.beginBlock();
      body.translate(c, w);
      w.endBlock();
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( SYNCHRONIZED ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
   }

  public Node copy() {
    SynchronizedStatement ss = new SynchronizedStatement(expr, body);
    ss.copyAnnotationsFrom(this);
    return ss;
  }

  public Node deepCopy() {
    SynchronizedStatement ss = 
      new SynchronizedStatement((Expression) expr.deepCopy(), 
				(BlockStatement) body.deepCopy());      
    ss.copyAnnotationsFrom(this);
    return ss;
  }

  private Expression expr;
  private BlockStatement body;
}

