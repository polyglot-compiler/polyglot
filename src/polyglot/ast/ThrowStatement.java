/*
 * ThrowStatement.java
 */

package jltools.ast;

/**
 * ThrowStatement
 * 
 * Overview: ThrowStatement is a mutable representation of a throw
 *    statement.  ThrowStatement contains a single Expression which
 *    evaluates to the object being thrown.
 */
public class ThrowStatement extends Statement {
  
  /**
   * Effects: Creates a new ThrowStatement which throws the value
   * of the Expression <expr>.
   */
  public ThrowStatement (Expression expr) {
    this.expr = expr;
  }

  /** 
   * Effects: Returns the expression whose value is thrown by
   *    this ThrowStatement.
   */
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects: Sets the expression being thrown by this ThrowStatement
   *    to <newExpr>.
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

  public Node accept (NodeVisitor v) {
    return v.visitThrowStatement(this);
  }
  
  /**
   * Requires: <v> will not transform the Expression into anything
   *    other than another Expression.
   * Effects: Visits the subexpression of this
   */
  public void visitChildren(NodeVisitor v) {
    expr = (Expression) expr.accept(v);
  }

  public void copy() {
    ThrowStatement ts = new ThrowStatement(expr);
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  public void deepCopy() {
    ThrowStatement ts = new ThrowStatement((Expression) expr.deepCopy());
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  private Expression expr;

}
  

  
