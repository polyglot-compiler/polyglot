/*
 * NewObjectExpression.java
 */ 

package jltools.ast;

import jltools.types.Type;
import jltools.util.TypedListIterator;
import jltools.util.TypedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * NewObjectExpression
 *
 * Overview: A NewObjectExpression is a mutable representation of the
 * use of the new operator to create a new instance of a class.  In
 * addition to the type of the class being created, a
 * NewObjectExpression has a list of arguments to be passed to the
 * constructor of the object and an optional ClassNode used to support
 * anonymous classes.  A new object expression may also be proceeded
 * by an primary expression which specifies the context in which the
 * object is being created.
 */

public class NewObjectExpression extends Expression {
    
  /**
   * Requires: arguments contains only elements of type <Expression>.
   *
   * Effects: Creates a new NewObjectExpression representing the
   * creation of an object of type <type> calling the constructor with
   * arguments in <arguments> and optionally using <classNode> to
   * extend the class.  If an anonymous class is not being created
   * classNode should be null.
   */
  public NewObjectExpression(Expression primary, TypeNode type,
			     List arguments, ClassNode classNode) {
    this.primary = primary;
    this.type = type;
    TypedList.check(arguments, Expression.class);
    argumentList = new ArrayList(arguments);
    this.classNode = classNode;
  }

  public NewObjectExpression(Expression primary, Type type,
			     List arguments, ClassNode classNode) {
    this(primary, new TypeNode(type), arguments, classNode);
  }

  /**
   * Effects: Returns the primary expression of this node or null if
   * there is none.
   */
  public Expression getPrimary() {
    return primary;
  }

  /**
   * Effects: Sets the primary expression which specifies the context
   * of the creation of the new object to <newPrimary>.
   */
  public void setPrimary(Expression newPrimary) {
    primary = newPrimary;
  }

  /**
   * Effects: Returns the type of the object being created by this
   * NewObjectExpression.  */
  public TypeNode getType() {
    return getType();
  }
    
  /**
   * Effects: Sets the type of the object being created by this to
   * be <newType>.
   */
  public void setType(TypeNode newType) {
    type = newType;
  }

  public void setType(Type newType) {
    type = new TypeNode(newType);
  }


  /**
   * Effects: Returns the argument at position <pos>.  Throws
   * IndexOutOfBoundsException if <pos> is not valid.
   */
  public Expression argumentAt(int pos) {
    return (Expression) argumentList.get(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which will yield each
   * argument exprssion of this in order.
   */
  public TypedListIterator arguments() {
    return new TypedListIterator(argumentList.listIterator(),
				 Expression.class,
				 false);
  }

  /**
   * Effects: Returns the ClassNode containing the ClassMembers used
   * to extend the class being created.  If no such extentions exist,
   * returns null.
   */
  public ClassNode getClassNode() {
    return classNode;
  }

  /**
   * Effects: Sets the ClassNode containing the ClassMembers used to
   * extend the class being instatiated to <newClassNode>.  If
   * <newClassNode> is null, then the class is instantiated with out
   * being extended.
   */
  public void setClasNode(ClassNode newClassNode) {
    classNode = newClassNode;
  }

  public Node accept(NodeVisitor v) {
    return v.visitNewObjectExpression(this);
  }

  /**
   * Requires: v will not transform the an Expression from the
   * argument list into anything other than another expression and
   * will not transform a ClassMemeber of the class body into
   * anything other than another ClassMemeber.
   *
   * Effects: visits each child of this with <v>.  If <v> returns null
   * for a memeber of the class body, then that member is removed. 
   */
  public void visitChildren(NodeVisitor v) {
    if (primary != null) {
      primary = (Expression) primary.accept(v);
    }
    type = (TypeNode) type.accept(v);
    for(ListIterator i=argumentList.listIterator(); i.hasNext(); ) {
      Expression e = (Expression) i.next();
      e = (Expression) e.accept(v);
      if (e == null) {
	i.remove();
      }
      else {
	i.set(v);
      }
    }
    if (classNode != null) {
      classNode = (ClassNode) classNode.accept(v);
    }
  }

  public Node copy() {
    NewObjectExpression no = new NewObjectExpression(primary,
						     type,
						     argumentList,
						     classNode);
    no.copyAnnotationsFrom(this);
    return no;
  }

  public Node deepCopy() {
    List newArgumentList = Node.deepCopyList(argumentList);
    Expression newPrimary =
      (Expression) (primary==null?null:primary.deepCopy());
    NewObjectExpression no =
      new NewObjectExpression(newPrimary,
			      (TypeNode) type.deepCopy(), 
			      newArgumentList, 
			      (ClassNode) classNode.deepCopy());
    no.copyAnnotationsFrom(this);
    return no;
  }

  private Expression primary;
  private TypeNode type;
  private List argumentList;
  private ClassNode classNode;
}
  
