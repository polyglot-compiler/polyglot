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

  // Misc

  public Node visitTypeNode(TypeNode tn) {
    tn.visitChildren(this);
    return tn;
  }

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

  public Node visitClassDeclarationStatement(ClassDeclarationStatement cs) {
    cs.visitChildren(this);
    return cs;
  }

  public Node visitSwitchStatement(SwitchStatement ss) {
    ss.visitChildren(this);
    return ss;
  }

  public Node visitTryStatement (TryStatement ts) {
    ts.visitChildren(this);
    return ts;
  }


  public Node visitCatchBlock(CatchBlock cb) {
    cb.visitChildren(this);
    return cb;
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

  public Node visitNewObjectExpression(NewObjectExpression oe) {
    oe.visitChildren(this);
    return oe;
  }

  public Node visitSpecialExpression(SpecialExpression se) {
    se.visitChildren(this);
    return se;
  }

  public Node visitInstanceofExpression(InstanceofExpression ie) {
    ie.visitChildren(this);
    return ie;
  }

  public Node visitArrayInitializerExpression(ArrayInitializerExpression aie) {
    aie.visitChildren(this);
    return aie;
  }

  public Node visitArrayIndexExpression(ArrayIndexExpression aie) {
    aie.visitChildren(this);
    return aie;
  }

  public Node visitAmbiguousNameExpression(AmbiguousNameExpression ane) {
    ane.visitChildren(this);
    return ane;
  }

  public Node visitMethodExpression(MethodExpression me) {
    me.visitChildren(this);
    return me;
  }

  public Node visitFieldExpression(FieldExpression fe) {
    fe.visitChildren(this);
    return fe;
  }

  // ClassMembers
  public Node visitClassNode (ClassNode cn) {
    cn.visitChildren(this);
    return cn;
  }
  
  public Node visitMethodNode(MethodNode mn) {
    mn.visitChildren(this);
    return mn;
  }

  public Node visitFieldNode(FieldNode fn) {
    fn.visitChildren(this);
    return fn;
  }
  
  public Node visitInitializerBlock(InitializerBlock ib) {
    ib.visitChildren(this);
    return ib;
  }


  public Node visitImportNode(ImportNode in) {
    in.visitChildren(this);
    return in;
  }

  public Node visitSourceFileNode(SourceFileNode sf) {
    sf.visitChildren(this);
    return sf;
  }
}
