/*
 * NullLiteral.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.Context;

/**
 * NullLiteral
 *
 * Overview: An Literal represents the Java literal 'null'.
 **/
public class NullLiteral extends Literal {
  /**
   * Creates a new NullLiteral object.
   **/
  public NullLiteral() {}

  public Node copy() {
    NullLiteral nl = new NullLiteral();
    nl.copyAnnotationsFrom(this);
    return nl;
  }

  public Node deepCopy() {
    return copy();
  }

  public void translate(Context c, CodeWriter w)
  {
    w.write("null");
  }

  public void dump(Context c, CodeWriter w)
  {
    w.write("( NULL )");
  }

  public Node typeCheck(Context c)
  {
    // Fixme: implement
    return this;
  }
  public void visitChildren(NodeVisitor v) 
  {
    // nothing to do 
  }
}
