/*
 * IfStatement.java
 */

package jltools.ast;

/**
 * IfStatement
 *
 * Overview: A mutable representation of a Java language if statement.
 *    Contains an expression whose value is tested, a then statement, and
 *    optionally an else statement.
 */
public class IfStatement extends Statement {
  /**
   * Effects: Creates a new IfStatement with conditional expression
   *    <condExpr>, a then statement <thenStatement> and else statement
   *    <elseStatement>.  If there is no else statement <elseStatement>
   *    should be null.
   */
  public IfStatement (Expression condExpr, Statement thenStatement,
		      Statement elseStatement) {
    this.condExpr = condExpr;
    this.thenStatement = thenStatement;
    this.elseStatement = elseStatement;
  }

  /**
   * Effects: Returns the Expression that this IfStatement is
   * conditioned on.
   */
  public Expression getConditionalExpression() {
    return condExpr;
  }

  /**
   * Effects: Sets the conditional expression of this to <newExpr>.
   */
  public void setConditionalExpression(Expression newExpr) {
    condExpr = newExpr;
  }

  /**
   * Effects: Returns the then statement associated with this
   *    IfStatement.
   */
  public Statement getThenStatement() {
    return thenStatement;
  }

  /**
   * Effects: Sets the then statement of this IfExpression to be
   *    <newStatement>.
   */
  public void setThenStatement(Statement newStatement) {
    thenStatement = newStatement;
  }

  /**
   * Effects: Returns the else statement associated with this
   *    IfStatement.
   */
  public Statement getElseStatement() {
    return elseStatement;
  }

  /**
   * Effects: Sets the else statement of this IfExpression to be
   *    <newStatement>.
   */
  public void setElseStatement(Statement newStatement) {
    elseStatement = newStatement;
  }

  public Node accept(NodeVisitor v) {
    return v.visitIfStatement(this);
  }

  /** 
   * Requires: v will not transform an expression into anything other
   *    than another expression, and that v will not transform a
   *    Statement into anything other than another Statement or
   *    Expression.
   * Effects: visits each of the children of this node with <v>.  If <v>
   *    returns an expression in place of one of the sub-statements, it is
   *    wrapped in an ExpressionStatement.
   */
  public void visitChildren(NodeVisitor v) {
    condExpr = (Expression) condExpr.accept(v);
    Node newNode = (Node) thenStatement.accept(v);
    if (newNode instanceof Expression) {
      thenStatement = new ExpressionStatement((Expression) newNode);
    }
    else {
      thenStatement = (Statement) newNode;
    }

    newNode = (Node) elseStatement.accept(v);
    if (newNode instanceof Expression) {
      elseStatement = new ExpressionStatement((Expression) newNode);
    }
    else {
      elseStatement = (Statement) newNode;
    }
  }

  private Expression condExpr;
  private Statement thenStatement;
  private Statement elseStatement;
}
