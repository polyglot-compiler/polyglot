/*
 * ArrayIndexExpression.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

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

  public void translate ( Context c, CodeWriter w)
  {
    w.write("(");
    base.translate(c, w);
    w.write(")");
    w.write ("[");
    index.translate(c, w);
    w.write ("]");
  }
  
  public void dump (Context c, CodeWriter w)
  {
    w.write ( " ( ARRAY INDEX EXPR ") ;
    dumpNodeInfo(c, w);
    base.dump(c, w);
    index.dump(c, w);
    w.write (" ) " );
  }

  public Node typeCheck(Context c)
  {
    // FIXME: implement;
    return this;
  }


  /** 
   * Requires: v does not transform an Expression into anything other
   * than another Expression.
   *
   * Effects: visits the children of this with v.
   */  
  public void visitChildren(NodeVisitor v) {
    base = (Expression) base.visit(v);
    index = (Expression) index.visit(v);
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



