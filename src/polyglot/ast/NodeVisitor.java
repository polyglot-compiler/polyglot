/*
 * NodeVisitor.java
 */

package jltools.ast;

/**
 * NodeVisitor
 *
 * Overview: A NodeVisitor is a traversal class for the AST, which implements
 *   the 'Visitor' design pattern. The contract is that every AST Node will
 *   have an accept method, which will dispatch to NodeVisitor's visitFoo
 *   method.
 *
 * Notes: 
 *    If you want to add nodes to the AST outside of jtools.ast, don't add
 *    them to NodeVisitor.  Instead, make a new visitor interface which
 *    extends NodeVisitor, and have your code look a little like:
 *         Node accept(NodeVistor v) {
 *             if (v instanceof SpecialVisitor)
 *                return v.visitSpecial(this);
 *             else {
 *                visitChildren(v);
 *                return this;
 *                // or maybe: return v.visitSupertypeOfThis(this);
 *             }
 *         }
 **/
public interface NodeVisitor {

  // Misc
  public Node visitTypeNode(TypeNode tn);

  // Statements
  public Node visitBlockStatement(BlockStatement bs);  
  public Node visitExpressionStatement(ExpressionStatement es);
  public Node visitVariableDeclarationStatement(VariableDeclarationStatement vs);
  public Node visitReturnStatement(ReturnStatement re);
  public Node visitThrowStatement(ThrowStatement ts);
  public Node visitIfStatement(IfStatement is);
  public Node visitWhileStatement(WhileStatement ws);
  public Node visitDoStatement(DoStatement ds);
  public Node visitForStatement(ForStatement fs);
  public Node visitSynchronizedStatement(SynchronizedStatement ss);
  public Node visitLabelledStatement(LabelledStatement ss);
  public Node visitBranchStatement(BranchStatement bs);
  public Node visitConstructorCallStatement(ConstructorCallStatement cs);
  public Node visitClassDeclarationStatement(ClassDeclarationStatement cs);
  public Node visitSwitchStatement(SwitchStatement ss);
  public Node visitTryStatement(TryStatement ts);

  public Node visitCatchBlock(CatchBlock cb);

  // Expressions
  public Node visitNullLiteral(NullLiteral nl);
  public Node visitStringLiteral(StringLiteral sl);
  public Node visitIntLiteral(IntLiteral il);
  public Node visitFloatLiteral(FloatLiteral fl); 
  public Node visitCastExpression(CastExpression ce);
  public Node visitUnaryExpression(UnaryExpression ue);
  public Node visitBinaryExpression(BinaryExpression be);
  public Node visitTernaryExpression(TernaryExpression te);  
  public Node visitLocalVariableExpression(LocalVariableExpression ve);
  public Node visitNewArrayExpression(NewArrayExpression ae);
  public Node visitNewObjectExpression(NewObjectExpression oe);
  public Node visitSpecialExpression(SpecialExpression se);
  public Node visitInstanceofExpression(InstanceofExpression ie);
  public Node visitArrayInitializerExpression(ArrayInitializerExpression aie);
  public Node visitArrayIndexExpression(ArrayIndexExpression aie);
  public Node visitAmbiguousNameExpression(AmbiguousNameExpression ane);
  public Node visitMethodExpression(MethodExpression me);
  public Node visitFieldExpression(FieldExpression fe);

  // ClassMemebers
  public Node visitClassNode(ClassNode cn);
  public Node visitMethodNode(MethodNode mn);
  public Node visitFieldNode(FieldNode fn);
  public Node visitInitializerBlock(InitializerBlock ib);


  public Node visitImportNode(ImportNode in);
  public Node visitSourceFileNode(SourceFileNode sf);

	 
}
