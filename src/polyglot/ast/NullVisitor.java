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
  public Node visitNullLiteral(NullLiteral nl) {
    return nl;
  }

  public Node visitStringLiteral(StringLiteral sl) {
    return sl;
  }

  public Node visitCastExpression(CastExpression ce) {
    ce.visitChildren(this);
    return ce;
  }

  public Node visitUnaryExpression(UnaryExpression ue) {
    ue.visitChildren(this);
    return ue;
  }
  
  public Node visitBinaryExpression(BinaryExpression be) {
    be.visitChildren(this);
    return be;
  }
    
  public Node visitTernaryExpression(TernaryExpression te) {
    te.visitChildren(this);
    return te;
  }

  public Node visitLocalVariableExpression(LocalVariableExpression ve) {
    return ve;
  }

  public Node visitNewArrayExpression(NewArrayExpression ae) {
    ae.visitChildren(this);
    return ae;
  }

  
}


