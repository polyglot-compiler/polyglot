/*
 * NullLiteral.java
 */

package jltools.ast;

import jltools.util.CodeWriter;
import jltools.types.LocalContext;

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

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write("null");
  }

  public void dump(LocalContext c, CodeWriter w)
  {
    w.write("( NULL )");
  }

  public Node typeCheck(LocalContext c)
  {
    setCheckedType( c.getTypeSystem().getNull());
    return this;
  }
  public void visitChildren(NodeVisitor v) 
  {
    // nothing to do 
  }
}
