/*
 * NewArrayExpression.java
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
 * NewArrayExpression
 *
 * Overview: A NewArrayExpression is a mutable representation of the
 *   creation of a new array such as "new File[8][]".  It consists of
 *   an element type, in the above example File, a list of dimension
 *   expressions (expressions which evaluate to a length for a
 *   dimension in this case { 8 }), and a number representing the
 *   number optional dimensions (in the example 1).
 */

public class NewArrayExpression extends Expression {
  /**
   * Requires: additionalDim >= 0, and every element of lengthExpressions is 
   *    an Expression.  optInitializer may be null.
   * Effects: Creates a NewArrayExpression with element type <elemType>,
   *    <lengthExpressions> as supplied dimensions,
   *    <additionalDim> additional dimensions of unspecified length,
   *    and (optionally) <optInitializer> as an initializer expression.
   */ 
  public NewArrayExpression(TypeNode elemType, 
			    List lengthExpressions, 
			    int additionalDim,
			    ArrayInitializerExpression optInitializer) {
    if (additionalDim < 0) {
      throw new IllegalArgumentException("additionalDim must be positive");
    }
    TypedList.check(lengthExpressions, Expression.class);
    type = elemType;    
    additionalDimensions = additionalDim;
    dimensionExpressions = new ArrayList(lengthExpressions);
    initializer = optInitializer;
  }

  /**
   * Requires: additionalDim >= 0, and every element of lengthExpressions is 
   *    an Expression.  optInitializer may be null.
   * Effects: Creates a NewArrayExpression with element type <elemType>,
   *    <lengthExpressions> as supplied dimensions,
   *    <additionalDim> additional dimensions of unspecified length,
   *    and (optionally) <optInitializer> as an initializer expression.
   */ 
  public NewArrayExpression(Type elemType, 
			    List lengthExpressions, 
			    int additionalDim,
			    ArrayInitializerExpression optInitializer) {
    this(new TypeNode(elemType), lengthExpressions, additionalDim, 
		      optInitializer);
  }
  
  /**
   * Effects: Returns the type of the array being created. 
   */
  public TypeNode getArrayType() {
    return type;
  }
  
  /** 
   * Effects: Sets the type of the array being create to <newType>.
   */
  public void setArrayType(TypeNode newType) {
    type = newType;
  }

  /** 
   * Effects: Sets the type of the array being create to <newType>.
   */
  public void setArrayType(Type newType) {
    type = new TypeNode(newType);
  }

  /**
   * Effects: Returns the number of additional dimensions which do not
   * have a defined length.
   */
  public int getAdditionalDimensions() {
    return additionalDimensions;
  }

  /**
   * Effects: Sets the number of additional dimensions of unspecified
   * length.
   */ 
  public void setAdditionalDimensions(int newAdditionalDim) {
    additionalDimensions = newAdditionalDim;
  } 
  
  /**
   * Effects: Returns the total dimensionality of the array.  For
   * example File[8][][] is three dimensional.  
   */
  
  public int getDimensions() {
    return dimensionExpressions.size() + additionalDimensions;
  }
  
  /**
   * Effects: adds a new dimension length expression to the end of the
   * list of specified dimensions.  (This occurs before any of the
   * dimensions of unspecified length.
   */
  public void addDimensionExpression(Expression e) {
    dimensionExpressions.add(e);
  }

  /**
   * Effects: adds a new dimension expression, <e>, at posistion
   * <pos>.  Throws IndexOutOfBoundsException if <pos> is not a valid
   * position.
   */
  public void addDimensionExpression(Expression e, int pos) {
    dimensionExpressions.add(pos, e);
  }

  /**
   * Effects: removes the dimension expression at position <pos>.
   * Throws an IndexOutOfBoundsException if <pos> is not a valid
   * position.
   */
  public void removeDimension(int pos) {
    dimensionExpressions.remove(pos);
  }

  /**
   * Effects: Returns the expression specifing the length of <pos>
   * dimension.
   */
  public Expression dimensionExpressionAt(int pos) {
    return (Expression) dimensionExpressions.get(pos);
  }  

  /**
   * Returns a TypedListIterator which will yield each expression
   * which specifies a dimension of this.
   */
  public TypedListIterator lengths() {
    return new TypedListIterator(dimensionExpressions.listIterator(),
				 Expression.class,
				 false);
  }

  /**
   * Returns the initializer for this expression, or null if none exists.
   **/
  public ArrayInitializerExpression getInitializer() {
    return initializer;
  }

  /**
   * Sets the initializer for this expression to equal <expr>
   **/
  public void setInitializer(ArrayInitializerExpression expr) {
    initializer = expr;
  }

  public Node accept(NodeVisitor v) {
    return v.visitNewArrayExpression(this);
  }

    /** 
     * Requires: v will not transform an Expression into anything
     *   other than another Expression.  If an Expression is
     *   transforemed into null, that dimension will be removed.
     * Effects: visits the subexpression of this.  
     */
    public void visitChildren(NodeVisitor v) {
      type = (TypeNode) type.accept(v);
      for (ListIterator i=dimensionExpressions.listIterator(); i.hasNext(); ) {
	Expression e = (Expression) i.next();
	e = (Expression) e.accept(v);
	if (e==null) {
	  i.remove();
	}
	else {
	  i.set(v);
	}
      }
    }

  public Node copy() {
    NewArrayExpression na = new NewArrayExpression(type,
						   new ArrayList(),
						   additionalDimensions,
						   initializer);
    na.copyAnnotationsFrom(this);
    for (Iterator i = dimensionExpressions.iterator(); i.hasNext() ; ) {
      na.addDimensionExpression((Expression) i.next());
    }
    return na;
  }

  public Node deepCopy() {
    ArrayInitializerExpression init = initializer == null ? null
      : (ArrayInitializerExpression) initializer.deepCopy();
    NewArrayExpression na = new NewArrayExpression((TypeNode) type.deepCopy(),
						   new ArrayList(),
						   additionalDimensions,
						   init);
    na.copyAnnotationsFrom(this);
    for (Iterator i = dimensionExpressions.iterator(); i.hasNext() ; ) {
      Expression e = (Expression) i.next();
      na.addDimensionExpression((Expression) e.deepCopy());
    }
    return na;
  }

  private TypeNode type;
  // RI: contains only elements of type Expression 
  private ArrayList dimensionExpressions;
  private int additionalDimensions;
  private ArrayInitializerExpression initializer;
}

    
