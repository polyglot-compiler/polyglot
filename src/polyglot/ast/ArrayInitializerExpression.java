/*
 * ArrayInitializerExpression.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import jltools.util.CodeWriter;
import jltools.types.Context;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * ArrayInitializerExpression
 *
 * Overview: An ArrayInitializerExpression is a mutable representation of
 *   the an ArrayInitializer, such as { 3, 1, { 4, 1, 5 } }
 */
public class ArrayInitializerExpression extends Expression {
  /**
   * Checks: every element of <list> is an Expression.
   *
   * Creates a new ArrayInitializerExpression with <list> as its elements.
   **/
  public ArrayInitializerExpression(List list) {
    TypedList.check(list, Expression.class);
    children = new ArrayList(list);
  }

  /**
   * Creates a new, empty ArrayInitializerExpression.
   **/
  public ArrayInitializerExpression() {
    children = new ArrayList();
  }

  /**
   * Returns a TypedListIterator which yields every child of this expression,
   * in order, and only allows Expressions to be inserted.
   **/
  public ListIterator children() {
    return new TypedListIterator(children.listIterator(),
				 Expression.class,
				 false);
  }

  /**
   * Adds a new element to the end of this expression.
   **/
  public void addExpression(Expression e) { children.add(e); }

  /**
   * Adds a new element to this expression at position <pos>.
   **/
  public void addExpression(int pos, Expression e) { children.add(pos, e); }

  /**
   * Returns the <pos>'th element of this expression.
   **/
  public Expression getExpression(int pos) 
    { return (Expression) children.get(pos); }

  /**
   * Replaces the <pos>'th element of this expression with <e>.
   **/
  public void setExpression(int pos, Expression e) { children.set(pos,e); }

  /**
   * Removes the <pos'th elemen of this expression.
   **/
  public void removeExpression(int pos) { children.remove(pos); }

  public void translate ( Context c, CodeWriter w)
  {
    w.write ( " { " );
    for (ListIterator iter = children(); iter.hasNext(); ) {
      ((Expression) iter.next()).translate(c, w);
      if ( iter.hasNext())
      {
        w.write (" , ");
      }
    }
    w.write ( " } " );
  }
  
  public void dump (Context c, CodeWriter w)
  {
    w.write (" ( ARRAY INITIALIZER " );
    dumpNodeInfo(c, w);
    for (ListIterator iter = children(); iter.hasNext(); ) {
      w.write ( " ( " );
      ((Expression) iter.next()).dump(c, w);
      w.write (" ) ");
    }
    w.write ( " ) " );
    
  }

  public Node typeCheck(Context c)
  {
    // FIXME: implement;
    return this;
  }


  public void visitChildren(NodeVisitor v) {
    for (ListIterator iter = children(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      Expression newExpr = (Expression) expr.visit(v);
      if (expr != newExpr)
	iter.set(newExpr);
    }
  }

  public Node copy() {
    Node n = new ArrayInitializerExpression(children);
    n.copyAnnotationsFrom(this);
    return n;
  }

  public Node deepCopy() {
    ArrayInitializerExpression aie = new ArrayInitializerExpression();
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      Expression expr = (Expression) iter.next();
      aie.addExpression( (Expression) expr.deepCopy() );
    }
    aie.copyAnnotationsFrom(this);
    return aie;
  }
  
  private List children;
}
  
