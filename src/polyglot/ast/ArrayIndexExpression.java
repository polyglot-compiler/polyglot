/*
 * ArrayIndexExpression.java
 */

package jltools.ast;

/**
 * Overview: An ArrayIndexExpression is a mutable representation of an
 * access of an array member.  For instance foo[i] accesses the i'th
 * member of foo.  An ArrayIndexExpression consists of a base
 * Expression which evaulates to an array, and an index expression
 * which evaluates to an int indicating the index of the array to be
 * accessed.
 */
public class ArrayIndexExpression extends Expression {
  /**
   * Overview: Creates a NewArrayIndexExpression, accessing an
   * element of Expression <base> at index <index>.
   */
  public ArrayIndexExpression(Expression base, Expression index) {
    this.base = base;
    this.index = index;
  }

  /**
   * Overview: Returns the base of this ArrayIndexExpression.
   */
  public Expression getBase() {
    return base;
  }

  /**
   * Overview: Sets the base expression of this to <newBase>.
   */
  public void setBase(Expression newBase) {
    base = newBase;
  }

  /**
   * Overview: Returns the index expression of this.
   */
  public Expression getIndex() {
    return index;
  }

  /**
   * Overview: Sets the index expression of this to <newIndex>.
   */
  public void setIndex(Expression newIndex) {
    index = newIndex;
  }

  public Node accept(NodeVisitor v) {
    return v.visitArrayIndexExpression(this);
  }

  /** 
   * Requires: v does not transform an Expression into anything other
   * than another Expression.
   *
   * Effects: visits the children of this with v.
   */  
  public void visitChildren(NodeVisitor v) {
    base = (Expression) base.accept(v);
    index = (Expression) index.accept(v);
  }

  public Node copy() {
    ArrayIndexExpression aie = new ArrayIndexExpression(base, index);
    aie.copyAnnotationsFrom(this);
    return aie;
  }

  public Node deepCopy() {
    ArrayIndexExpression aie =
      new ArrayIndexExpression((Expression) base.deepCopy(),
			       (Expression) index.deepCopy());
    aie.copyAnnotationsFrom(this);
    return aie;

  }

  private Expression base;
  private Expression index;
}



