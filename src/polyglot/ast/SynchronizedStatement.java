/*
 * SynchronizedStatement.java
 */

package jltools.ast;

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

  public Node accept(NodeVisitor v) {
    return v.visitSynchronizedStatement(this);
  }

  /** 
   * Requires: v will not transform an expression into anything other
   *    than another expression, and that v will not transform a
   *    BlockStatement into anything other than another BlockStatement.
   * Effects: visits each of the children of this node with <v>.  If <v>
   *    returns an expression in place of the sub-statement, it is
   *    wrapped in an ExpressionStatement.
   */
  public void visitChildren(NodeVisitor v) {
    expr = (Expression) expr.accept(v);
    body = (BlockStatement) body.accept(v);
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

