/*
 * NewArrayExpression.java
 */

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


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
   *    <optInitializer> initializer (may be null) used when 
   *               = new byte[] {2, 32, 32}
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
   *    an Expression.  
   * Effects: Creates a NewArrayExpression with element type <elemType>,
   *    <lengthExpressions> as supplied dimensions,
   *    <additionalDim> additional dimensions of unspecified length,
   *    <optInitializer> initializer (may be null) used when 
   *               = new byte[] {2, 32, 32}
   */ 
  public NewArrayExpression(Type elemType, 
			    List lengthExpressions, 
			    int additionalDim, 
                            ArrayInitializerExpression optInitializer) {
    this(new TypeNode(elemType), lengthExpressions, additionalDim, optInitializer);
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
   * Returns the initalizer for this expression, or null if none exists
   */
  public ArrayInitializerExpression getInitializer()
  {
    return initializer;
  }

  /**
   * Sets the initializer for this expression equal to <init>
   */
  public void setInitializer( ArrayInitializerExpression init)
  {
    initializer = init;
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

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write ("new ");
    type.translate(c, w);
    for (ListIterator i = dimensionExpressions.listIterator(); i.hasNext(); )
    {
      w.write("[");
      ( (Expression)i.next()).translate( c, w);
      w.write("]");
    }
    for (int i = 0; i < additionalDimensions; i++)
    { 
      w.write("[]");
    }
    if (initializer != null)
    {
      initializer.translate(c, w);
    }
   }

  public Node dump( CodeWriter w)
  {
    w.write( "( NEW");
    w.write( " < " + additionalDimensions + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }
  
  public Node typeCheck (LocalContext c)
  {
    setCheckedType( new ArrayType( c.getTypeSystem(), type.getType(),
                                   dimensionExpressions.size() + 
                                   additionalDimensions));
    return this;
  }

  /** 
   * Requires: v will not transform an Expression into anything
   *   other than another Expression.  If an Expression is
   *   transforemed into null, that dimension will be removed.
   * Effects: visits the subexpression of this.  
   */
  Object visitChildren(NodeVisitor v) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);
    
    type = (TypeNode) type.visit(v);
    vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( type), vinfo);
    
    for (ListIterator i=dimensionExpressions.listIterator(); i.hasNext(); ) {
      Expression e = (Expression) i.next();
      e = (Expression) e.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( e), vinfo);
      if (e==null) {
        i.remove();
      }
      else {
        i.set(e);
      }
    }
    if( initializer != null) {
      initializer = (ArrayInitializerExpression) initializer.visit(v);
      vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( initializer), 
                                  vinfo);
    }
    
    return vinfo;
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
    ArrayInitializerExpression init = initializer == null ? null : 
      ( ArrayInitializerExpression) initializer.deepCopy();
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

    
