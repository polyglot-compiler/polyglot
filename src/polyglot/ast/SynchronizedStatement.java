/*
 * SynchronizedStatement.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

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

   void visitChildren(NodeVisitor vis)
   {
    expr = (Expression) expr.visit(vis);
    body = (BlockStatement) body.visit(vis);
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void  translate(Context c, CodeWriter w)
   {
      w.write ("synchronized (") ;
      expr.translate(c, w);
      w.write(")");
      w.beginBlock();
      body.translate(c, w);
      w.endBlock();
   }

   public void  dump(Context c, CodeWriter w)
   {
      w.write ("SYNCHRONIZED ");
      dumpNodeInfo(c, w);
      expr.dump(c, w);
      w.beginBlock();
      body.dump(c, w);
      w.endBlock();
      w.write(")");
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

