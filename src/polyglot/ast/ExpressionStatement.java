/*
 * ExpressionStatement.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.LocalContext;

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

  /**
   * Requires: v will not transform an Expression into anything other than an
   *    Expression.
   **/
  public void visitChildren(NodeVisitor v) {
    expression = (Expression) expression.visit(v);
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    expression.translate(c, w);
    w.write(";");
  }

  public void dump(LocalContext c, CodeWriter w)
  {
    w.write("( EXPRESSION_STATEMENT" );
    dumpNodeInfo(c, w);
    w.write(" (");
    expression.dump(c, w);
    w.write(" ) )");
  }

  public Node typeCheck(LocalContext c)
  {
    // Fixme: implement
    return this;
  }

  public Node copy() {
    ExpressionStatement es = new ExpressionStatement(expression);
    es.copyAnnotationsFrom(this);
    return es;
  }

  public Node deepCopy() {
    ExpressionStatement es = 
      new ExpressionStatement((Expression) expression.deepCopy());
    es.copyAnnotationsFrom(this);
    return es;
  }

  private Expression expression;
}

