/*
 * NullLiteral.java
 */

package jltools.ast;

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

  public Node accept(NodeVisitor v) {
    return v.visitNullLiteral(this);
  }

  public void visitChildren(NodeVisitor v) {}
}
