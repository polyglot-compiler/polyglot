/*
 * ReturnStatement.java
 */

package jltools.ast;

/**
 * ReturnStatement
 * 
 * Overview: A ReturnStatment is a mutable representation of a return
 *   statement in Java.
 */
public class ReturnStatement extends Statement {
  /**
   * Effects: Creates a new ReturnStatement which returns <expr>.
   */
  public ReturnStatement (Expression expr) {
    this.expr = expr;
  }

  /**
   * Effects: Returns the expression which would be returned by
   * this ReturnStatement.
   */ 
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects: Sets the Expression to be returned by this to be <newExpr>.
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

  public Node accept(NodeVisitor v) {
    return v.visitReturnStatement(this);
  }

  /** 
   * Requires: v will not transform the expression into anything other than
   *   another expression.
   */
  public void visitChildren(NodeVisitor v) {
    expr = (Expression) expr.accept(v);
  }

  public Node copy() {
    ReturnStatement rs = new ReturnStatement(expr);
    rs.copyAnnotationsFrom(this);
    return rs;
  }

  public Node deepCopy() {
    ReturnStatement rs = new ReturnStatement((Expression) expr.deepCopy());
    rs.copyAnnotationsFrom(this);
    return rs;
  }

  private Expression expr;
}
