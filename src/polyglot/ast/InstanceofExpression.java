/*
 * InstanceofExpression.java
 */

package jltools.ast;

import jltools.types.Type;

/**
 * InstanceofExpression
 *
 * Overview: An InstanceofExpression is a mutable representation of
 *   the use of the instanceof operator in Java such as "<expression>
 *   instanceof <type>".
 */

public class InstanceofExpression extends Expression {
  /**
   * Effects: Creates a new InstanceofExpreession which is testing if
   *    <expr> is an instance of <type>.
   */
  public InstanceofExpression (Expression expr, TypeNode type) {
    this.expr = expr;
    this.type = type;
  }
  /**
   * Effects: Creates a new InstanceofExpreession which is testing if
   *    <expr> is an instance of <type>.
   */
  public InstanceofExpression (Expression expr, Type type) {
    this.expr = expr;
    this.type = new TypeNode(type);
  }

  /**
   * Effects: Retursn the expression whose type is being checked
   */
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects:  Sets the expression being tested to <newExpr>.
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

  /**
   * Effects: Returns the type to which the type of the expression
   *    is being compared. 
   */
  public TypeNode getType() {
    return type;
  }

  /**
   * Effects: Changes the type of being checked in this expression
   *    to <newType>.
   */
  public void setType(TypeNode newType) {
    type = newType;
  }

  /**
   * Effects: Changes the type of being checked in this expression
   *    to <newType>.
   */
  public void setType(Type newType) {
    type = new TypeNode(newType);
  }

  public Node accept(NodeVisitor v) {
    return v.visitInstanceofExpression(this);
  }

  /**
   * Requires: v will not transform the Expression into anything other
   *    than another Expression.
   * Effects:
   *     Visits the subexpression of this.
   */
  public void visitChildren(NodeVisitor v) {
    type = (TypeNode) type.accept(v);
    expr = (Expression) expr.accept(v);
  }

  public Node copy() {
    InstanceofExpression ie = new InstanceofExpression(expr, type);
    ie.copyAnnotationsFrom(this);
    return ie;
  }

  public Node deepCopy() {
    InstanceofExpression ie = 
      new InstanceofExpression((Expression) expr.deepCopy(), 
			       (TypeNode) type.deepCopy());
    ie.copyAnnotationsFrom(this);
    return ie;
  }

  private Expression expr;
  private TypeNode type;
}
  
