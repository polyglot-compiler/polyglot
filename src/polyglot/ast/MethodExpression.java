/*
 * MethodExpression.java
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
 * Overview: A MethodExpression is a mutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which's value the method is being
 * called.
 */
public class MethodExpression extends Expression {
  /**
   * Requires: Either <type> or <expr> or both are null, all
   * elements of <arguments> are of type Expression.
   *
   * Effects: Creates a new MethodExpression calling a method named
   * <name> with arguments in <arguments> being called optionally on
   * either <type> or <expr>.
   */
  public MethodExpression(Type type, Expression expr, String name,
			  List arguments) {
    if (type != null & expr != null) {
      throw new IllegalArgumentException("A MethodExpression must have either"+
					 " a null type or expression. ");
    }
    this.type = type;
    this.expr = expr;
    this.name = name;
    TypedList.check(arguments, Expression.class);
    this.arguments = new ArrayList(arguments);
  }

  /**
   * Effects: Returns the name of the method being called in this.
   */
  public String getName() {
    return name;
  }

  /**
   * Effects: Sets the name of the method being called by this to <newName>.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Effects: Adds <newArg> to the arguments of the method being
   * called by this.
   */
  public void addArgument(Expression newArg) {
    arguments.add(newArg);
  }

  /**
   * Effects: Returns the argument at position <pos> of the method
   * being called.  Throws IndexOutOfBoundsException if <pos> is not
   * valid.
   */
  public Expression getArgumentAt(int pos) {
    return (Expression) arguments.get(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which will produce the
   * arugments of this MethodExpression in order.
   */
  public TypedListIterator arguments() {
    return new TypedListIterator(arguments.listIterator() ,
				 Expression.class,
				 false);
  }

  /**
   * Effects: Removes the argument at positions <pos>.  Throws
   * IndexOutOfBoundsException if <pos> is not valid.  */
  public void removeArgument(int pos) {
    arguments.remove(pos);
  }

  /**
   * Effects: Returns the Type that the method is being called on.  If
   * the method is being called on an object or a local call, returns
   * null.
   */
  public Type getTargetType() {
    return type;
  }

  /**
   * Effects: Sets this method to be called on <newType>.  If
   * <newType> is not null, any expression which this method was
   * previously set to be called on will be set to null.
   */
  public void setTargetType(Type newType) {
    type = newType;
    if (type != null) expr = null;
  }

  /**
   * Effects: Returns the expression upon which this method is being
   * called, or null if it is not being called on an expression.
   */
  public Expression getTargetExpression() {
    return expr;
  }

  /**
   * Effects: Sets this MethodExpression to be called on <newExpr>.
   * If this MethodExpression was previously called on a Type, then
   * the type is set to null.
   */
  public void setTargetExpression(Expression newExpr) {
    expr = newExpr;
    if (expr != null) type = null;
  }

  public Node accept(NodeVisitor v) {
    return v.visitMethodExpression(this);
  }

  public void visitChildren(NodeVisitor v) {
    if (expr != null) {
      expr = (Expression) expr.accept(v);
    }

    for(ListIterator it=arguments.listIterator(); it.hasNext(); ) {
      it.set(((Expression) it.next()).accept(v));
    }

  }

  public Node copy() {
    MethodExpression me = new MethodExpression(type,
					       expr,
					       name,
					       arguments);
    me.copyAnnotationsFrom(this);
    return me;
  }
	
  public Node deepCopy() {
    List newArguments = new ArrayList(arguments.size());
    for (ListIterator i=arguments.listIterator(); i.hasNext(); ) {
      newArguments.add(i.next());
    }
    MethodExpression me = new MethodExpression(type,
					       (Expression) expr.deepCopy(),
					       name,
					       newArguments);
    me.copyAnnotationsFrom(this);
    return me;
  }

  private Type type;
  private Expression expr;
  private String name;
  private List arguments;
}
    
  
