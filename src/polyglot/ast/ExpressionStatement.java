/*
 * ExpressionStatement.java
 */

package jltools.ast;

import jltools.util.Assert;
import jltools.util.Annotate;

/**
 * ExpressionStatement
 *
 * Overview: An Statement represents a mutable expression statement.
 **/
public class ExpressionStatement extends Statement {
  /**
   * Effects: Create a new ExpressionStatement.
   **/
  public ExpressionStatement(Expression exp) {
    expression = exp;
  }

  /** 
   * Returns the underlying expression of this ExpressionStatement.
   **/
  public Expression getExpression() {
    return expression;
  }

  /** 
   * Sets the underlying expression of this ExpressionStatement.
   **/
  public void setExpression(Expression exp) {
    Assert.assert(exp != null);
    expression = exp;
  }    

  public Node accept(NodeVisitor v) {
    return v.visitExpressionStatement(this);
  }

  /**
   * Requires: v will not transform an Expression into anything other than an
   *    Expression.
   **/
  public void visitChildren(NodeVisitor v) {
    expression = (Expression) expression.accept(v);
  }

  public Node copy() {
    ExpressionStatement es = new ExpressionStatement(expression);
    es.copyAnnotationsFrom(this);
    return es;
  }

  public Node deepCopy() {
    ExpressionStatement es = new ExpressionStatement(expression.deepCopy());
    es.copyAnnotationsFrom(this);
    return es;
  }

  private Expression expression;
}

