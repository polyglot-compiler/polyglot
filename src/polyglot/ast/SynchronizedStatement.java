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
   *    <expr>, and a statement <statement>.
   */
  public SynchronizedStatement (Expression expr, Statement statement) {
    this.expr = expr;
    this.statement = statement;
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
  public Statement getStatement() {
    return statement;
  }

  /**
   * Effects: Sets the statement of this SynchronizedStatement to be
   *    <newStatement>.
   */
  public void setStatement(Statement newStatement) {
    statement = newStatement;
  }

  public Node accept(NodeVisitor v) {
    return v.visitSynchronizedStatement(this);
  }

  /** 
   * Requires: v will not transform an expression into anything other
   *    than another expression, and that v will not transform a
   *    Statement into anything other than another Statement or
   *    Expression.
   * Effects: visits each of the children of this node with <v>.  If <v>
   *    returns an expression in place of the sub-statement, it is
   *    wrapped in an ExpressionStatement.
   */
  public void visitChildren(NodeVisitor v) {
    expr = (Expression) condExpr.accept(v);
    Node newNode = (Node) statement.accept(v);
    if (newNode instanceof Expression) {
      statement = new ExpressionStatement((Expression) newNode);
    }
    else {
      statement = (Statement) newNode;
    }
  }

  private Expression expr;
  private Statement statement;
}

