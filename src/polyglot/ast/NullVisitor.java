/*
 * NullVisitor.java
 */

package jltools.ast;

/**
 * NullVisitor
 *
 * Overview: 
 *    This visitor performs a no-op.  It is intended to be subclassed by
 *    actual visitors.
 **/
public class NullVisitor implements NodeVisitor {

  // Statements

  public Node visitBlockStatement(BlockStatement bs) {
    bs.visitChildren(this);
    return bs;
  }
  public Node visitExpressionStatement(ExpressionStatement es) {
    es.visitChildren(this);
    return es;
  }

  // Expressions
}


