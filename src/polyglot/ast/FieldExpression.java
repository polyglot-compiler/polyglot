/*
 * FieldExpression.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Overview: A Field is a mutable representation of a Java field
 * access.  It consists of field name and may also have either a Type
 * or an Expression containing the field being accessed.
 */
public class FieldExpression extends Expression {
  /**
   * Requires: Either <type> or <expr> or both are null, all
   * elements of <arguments> are of type Expression.
   *
   * Effects: Creates a new FieldExpression accessing a field named
   * <name> from optionally either <type> or <expr>.  
   */
  public FieldExpression(Type type, Expression expr, String name) {
    if (type != null & expr != null) {
      throw new IllegalArgumentException("A Field must have either"+
					 " a null type or expression. ");
    }
    this.type = type;
    this.expr = expr;
    this.name = name;
  }

  /**
   * Effects: Returns the name of the field being accessed in this.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of the field being accessed by this to <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Effects: Returns the Type that the field is being accessed from.
   * If the field is from an object, returns null.  
   */
  public Type getTargetType() {
    return type;
  }

  /**
   * Effects: Sets the field to be accessed from <newType>.  If the
   * field had an expression from which it was accessed, the
   * expression is set to null.
   */
  public void setTargetType(Type newType) {
    type = newType;
    if (type != null) expr = null;
  }

  /**
   * Effects: Returns the expression upon which this field is being
   * accessed or null if it is not being accessed from an expression.
   */
  public Expression getTargetExpression() {
    return expr;
  }

  /**
   * Effects: Sets this FieldExpression to be accessed from <newExpr>.
   * If this FieldExpression previosuly referenced a Type, then the
   * type is set to null.  
   */
  public void setTargetExpression(Expression newExpr) {
    expr = newExpr;
    if (expr != null) type = null;
  }

  public Node accept(NodeVisitor v) {
    return v.visitFieldExpression(this);
  }

  public void visitChildren(NodeVisitor v) {
    if (expr != null) {
      expr = (Expression) expr.accept(v);
    }
  }

  public Node copy() {
    FieldExpression fe = new FieldExpression(type,
					     expr,
					     name);
    fe.copyAnnotationsFrom(this);
    return fe;
  }
	
  public Node deepCopy() {
    FieldExpression fe = new FieldExpression(type,
					     (Expression) expr.deepCopy(),
					     name);
    fe.copyAnnotationsFrom(this);
    return fe;
  }

  private Type type;
  private Expression expr;
  private String name;
}
    
  
