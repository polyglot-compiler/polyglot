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
 * constructor of the object and an optional array of class memebers
 * to support anonymous classes.
 */

public class NewObjectExpression extends Expression {
    
  /**
   * Requires: <classMemebers> contains only elements of type
   * ClassMember, arguments contains only elements of type
   * <Expression>.
   *
   * Effects: Creates a new NewObjectExpression representing the
   * creation of an object of type <type> calling the constructor
   * with arguments in <arguments> and optionally using the elements
   * of <classMembers> to construct a body of the new class
   * instance.  If there are no specified class body,
   * <classMembers> can be either empty or null.
   */
  public NewObjectExpression(Type type, List arguments, List classMemebers) {
    this.type = type;
    TypedList.check(arguments, Expression.class);
    argumentList = new ArrayList(arguments);
    TypedList.check(classMembers, ClassMember.class);
    this.classMembers = new ArrayList(classMembers);
  }

  /**
   * Effects: Returns the type of the object being created by this
   * NewObjectExpression.
   */
  public Type getType() {
    return getType();
  }
    
  /**
   * Effects: Sets the type of the object being created by this to
   * be <newType>.
   */
  public void setType(Type newType) {
    type = newType;
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
   * Effects: Returns the classMemeber at positions <pos>.  Thorws an
   * IndexOutOfBoundsException if <pos> is not valid.
   */
  public ClassMember classMemberAt(int pos) {
    return (ClassMember) classMembers.get(pos);
  }

  /**
   * Effects: Returns a TypedListIterator which will yield each
   * ClassMember in the optional ClassBody of this NewObjectExpression
   * in order.
   */
  public TypedListIterator classMemebers() {
    return new TypedListIterator(classMembers.listIterator(),
				 ClassMember.class,
				 false);
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
    for(ListIterator i=classMembers.listIterator(); i.hasNext(); ) {
      ClassMember m = (ClassMember) i.next();
      m = (ClassMember) m.accept(v);
      if (m == null) {
	i.remove();
      }
      else {
	i.set(v);
      }
    }
  }

  public Node copy() {
    NewObjectExpression no = new NewObjectExpression(type,
						     argumentList,
						     classMembers);
    no.copyAnnotationsFrom(this);
    return no;
  }

  public Node deepCopy() {
    List newArgumentList = new ArrayList(argumentList.size());
    for (ListIterator it = newArgumentList.listIterator(); it.hasNext();) {
      Expression e = (Expression) it.next();
      newArgumentList.add(e.deepCopy());
    }
    List newClassMemebers = new ArrayList(classMembers.size());
    for (ListIterator it = newArgumentList.listIterator(); it.hasNext(); ) {
      ClassMember m = (ClassMember) it.next();
      newClassMemebers.add(m.deepCopy());
    }
    NewObjectExpression no =
      new NewObjectExpression (type, newArgumentList, newClassMemebers);
    no.copyAnnotationsFrom(this);
    return no;
  }

  private Type type;
  private List argumentList;
  private List classMembers;
}
  
