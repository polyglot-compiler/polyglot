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

  // Statements
  public Node visitBlockStatement(BlockStatement bs);  
  public Node visitExpressionStatement(ExpressionStatement es);
  public Node visitVariableDeclarationStatement(VariableDeclarationStatement vs);
  public Node visitReturnStatement(ReturnStatement re);
  public Node visitThrowStatement(ThrowStatement ts);
  public Node visitIfStatement(IfStatement is);
  //public Node visitWhileStatement(WhileStatement ws);
  public Node visitDoStatement(DoStatement ds);
  public Node visitSynchronizedStatement(SynchronizedStatement ss);

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
 
  public Node visitInstanceofExpression(InstanceofExpression ie);
    
	 
}
