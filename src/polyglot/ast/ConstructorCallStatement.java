/*
 * ConstructorCallStatement.java
 */

package jltools.ast;

import jltools.util.TypedListIterator;
import jltools.util.TypedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * ConstructorCallStatement
 *
 * Overview: A ConstructorCallStatement is a mutable representation of
 *    a direct call to a constructor of a class in the form of
 *    super(...)  or this(...).  It consists of a type of the call
 *    (either super or this) and a list of expressions to be
 *    parameters of the call.  
 */

public class ConstructorCallStatement extends Statement {
    
  public static final int SUPER   = 0; 
  public static final int THIS    = 1;

  // the highest type used by this class
  public static final int MAX_TYPE = THIS; 

  /**
   * Requires: <type> be one of the valid types listed in the static
   *    final ints of this class, every memeber of <arguments> to be
   *    an Expression.
   *
   * Effects: Creates a new ConstructorCallStatement of type <type>
   *    which contains the elements of <arguments> as arguments to the
   *    call.
   */
  public ConstructorCallStatement(int type, List arguments) {
    setType(type);
    TypedList.check(arguments, Expression.class);
    argumentList = new ArrayList(arguments);
  }

  /**
   * Effects: Returns the type of this constructor call as defined in
   * the list of public static ints in this class.
   */
  public int getType() {
    return type;
  }

  /**
   * Requires: <newType> is a valid type as defined in the list of
   *    public static ints of this class.
   * Effects: sets the type of this to be <newType>.
   */
  public void setType(int newType) {
    if (newType < 0 || newType > MAX_TYPE) {
      throw new IllegalArgumentException("Value for type of " +
					 "ConstructorCallStatement " +
					 "not valid.");
    }
    type = newType;
  }

  /**
   * Effects: Returns the argument at position <pos>.  Throws
   *   IndexOutOfBoundsException if <pos> is not valid.
   */
  public Expression argumentAt(int pos) {
    return (Expression) argumentList.get(pos);
  }

  /**
   * Effects: Retursn a TypedListIterator which will yield each
   * expression which is an argument in this call in order.
   */
  public TypedListIterator arguments() {
    return new TypedListIterator(argumentList.listIterator(),
				 Expression.class,
				 false);
  }

  public Node accept(NodeVisitor v) {
    return v.visitConstructorCallStatement(this);
  }

  /**
   * Requires v will not transform an Expression into anything other
   *   then another Expression.
   *  Effects: visits all subexpressions of this with <v>.  If <v>
   *    returns null for an argument expression, that argument is
   *    removed.
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
  }

  public Node copy() {
    ConstructorCallStatement ca = new ConstructorCallStatement(type,
							       argumentList);
    ca.copyAnnotationsFrom(this);
    return ca;
  }

  public Node deepCopy() {
    List newArgumentList = new ArrayList(argumentList.size());
    for (ListIterator it = newArgumentList.listIterator(); it.hasNext(); ) {
      Expression e = (Expression) it.next();
      newArgumentList.add(e.deepCopy());
    }
    ConstructorCallStatement ca = 
      new ConstructorCallStatement(type, newArgumentList);
    ca.copyAnnotationsFrom(this);        
    return ca;
  }


  private int type;
  private List argumentList;
}
