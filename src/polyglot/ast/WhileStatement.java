/*
 * WhileStatement.java
 */

package jltools.ast;

/**
 * WhileStatement
 *
 * Overview: A mutable representation of a Java language while
 *   statment.  Contains a statement to be executed and an expression
 *   to be tested indicating whether to reexecute the statement.
 */ 
public class WhileStatement extends Statement {
  /**
   * Effects: Creates a new WhileStatement with a statement <statement>,
   *    and a conditional expression <condExpr>.
   */
  public WhileStatement(Expression condExpr, Statement statement) {
    this.condExpr = condExpr;
    this.statement = statement;
  }

  /**
   * Effects: Returns the Expression that this WhileStatement is
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
   * Effects: Returns the statement associated with this
   *    WhileStatement.
   */
  public Statement getBody() {
    return statement;
  }

  /**
   * Effects: Sets the statement of this WhileStatement to be
   *    <newStatement>.
   */
  public void setBody(Statement newStatement) {
    statement = newStatement;
  }

  public Node accept(NodeVisitor v) {
    return v.visitWhileStatement(this);
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
    Node newNode = (Node) statement.accept(v);
    if (newNode instanceof Expression) {
      statement = new ExpressionStatement((Expression) newNode);
    }
    else {
      statement = (Statement) newNode;
    }
    condExpr = (Expression) condExpr.accept(v);
  }

  public Node copy() {
    WhileStatement ds = new WhileStatement(condExpr, statement);
    ds.copyAnnotationsFrom(this);
    return ds;
  }

  public Node deepCopy() {
    WhileStatement ds = new WhileStatement((Expression) condExpr.deepCopy(),
					   (Statement) statement.deepCopy());
    ds.copyAnnotationsFrom(this);
    return ds;
  }

  private Expression condExpr;
  private Statement statement;
}

