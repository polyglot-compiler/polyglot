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

  public Node visitVariableDeclarationStatement(VariableDeclarationStatement v)
  {
    v.visitChildren(this);
    return v;
  }

  public Node visitReturnStatement(ReturnStatement rs) {
    rs.visitChildren(this);
    return rs;
  }

  public Node visitThrowStatement(ThrowStatement ts) {
    ts.visitChildren(this);
    return ts;
  }

  public Node visitIfStatement(IfStatement is) {
    is.visitChildren(this);
    return is;
  }

  public Node visitWhileStatement(WhileStatement ws) {
    ws.visitChildren(this);
    return ws;
  }

  public Node visitDoStatement(DoStatement ds) {
    ds.visitChildren(this);
    return ds;
  }

  public Node visitForStatement(ForStatement fs) {
    fs.visitChildren(this);
    return fs;
  }

  public Node visitSynchronizedStatement(SynchronizedStatement ss) {
    ss.visitChildren(this);
    return ss;
  }

  public Node visitLabelledStatement(LabelledStatement ls) {
    ls.visitChildren(this);
    return ls;
  }

  public Node visitBranchStatement(BranchStatement bs) {
    bs.visitChildren(this);
    return bs;
  }

  public Node visitConstructorCallStatement(ConstructorCallStatement cs) {
    cs.visitChildren(this);
    return cs;
  }

  public Node visitTryStatement (TryStatement ts) {
    ts.visitChildren(this);
    return ts;
  }

  // Expressions
  public Node visitNullLiteral(NullLiteral nl) {
    return nl;
  }

  public Node visitStringLiteral(StringLiteral sl) {
    return sl;
  }

  public Node visitIntLiteral(IntLiteral il) {
    return il;
  }

  public Node visitFloatLiteral(FloatLiteral fl) {
    return fl;
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

  public Node visitInstanceofExpression(InstanceofExpression ie) {
    ie.visitChildren(this);
    return ie;
  }

  public Node visitArrayInitializerExpression(ArrayInitializerExpression aie) {
    aie.visitChildren(this);
    return aie;
  }

}
