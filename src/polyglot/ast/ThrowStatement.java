/*
 * ThrowStatement.java
 */

package jltools.ast;
import jltools.util.CodeWriter;
import jltools.types.Context;

/**
 * ThrowStatement
 * 
 * Overview: ThrowStatement is a mutable representation of a throw
 *    statement.  ThrowStatement contains a single Expression which
 *    evaluates to the object being thrown.
 */
public class ThrowStatement extends Statement {
  
  /**
   * Effects: Creates a new ThrowStatement which throws the value
   * of the Expression <expr>.
   */
  public ThrowStatement (Expression expr) {
    this.expr = expr;
  }

  /** 
   * Effects: Returns the expression whose value is thrown by
   *    this ThrowStatement.
   */
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects: Sets the expression being thrown by this ThrowStatement
   *    to <newExpr>.
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

   /**
    *
    */
   void visitChildren(NodeVisitor vis)
   {
      expr = (Expression)expr.visit(vis);
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void  translate(Context c, CodeWriter w)
   {
      w.write("throw ");
      expr.translate(c, w);
      w.write(";");
   }

   public void dump(Context c, CodeWriter w)
   {
      w.write("( ");
      expr.dump(c, w);
      w.write(")");
   }
  
  public Node copy() {
    ThrowStatement ts = new ThrowStatement(expr);
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  public Node deepCopy() {
    ThrowStatement ts = new ThrowStatement((Expression) expr.deepCopy());
    ts.copyAnnotationsFrom(this);
    return ts;
  }

  private Expression expr;

}
  

  
