/*
 * WhileStatement.java
 */

package jltools.ast;

/**
 * WhileStatement
 *
 * Overview: A mutable representation of a Java language while
 *   statment.  Contains a statement to be executed and an expression
 *   to be tested indicating whether to reexecute the statement.
 */ 
public class WhileStatement extends Statement {
  /**
   * Effects: Creates a new WhileStatement with a statement <statement>,
   *    and a conditional expression <condExpr>.
   */
  public WhileStatement(Expression condExpr, Statement statement) {
    this.condExpr = condExpr;
    this.statement = statement;
  }

  /**
   * Effects: Returns the Expression that this WhileStatement is
   * conditioned on.
   */
  public Expression getConditionalExpression() {
    return condExpr;
  }

  /**
   * Effects: Sets the conditional expression of this to <newExpr>.
   */
  public void setConditionalExpression(Expression newExpr) {
    condExpr = newExpr;
  }

  /**
   * Effects: Returns the statement associated with this
   *    WhileStatement.
   */
  public Statement getBody() {
    return statement;
  }

  /**
   * Effects: Sets the statement of this WhileStatement to be
   *    <newStatement>.
   */
  public void setBody(Statement newStatement) {
    statement = newStatement;
  }

   /**
    *
    */
   void visitChildren(NodeVisitor vis)
   {
      condExpr = condExpr.visit(vis);
      statement = statement.visit(vis);
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public Node translate(Context c)
   {
      w.write("while ");
      w.write("(");
      condExpr.translate(c, w);
      w.write(")");
      w.beginBlock();
      statement.translate(c, w);
      w.endBlock();

      return this;
   }

   public Node dump(Context c, CodeWriter w)
   {
      w.write("( WHILE ");
      dumpNodeInfo(c, w); 
      condExpr.dump(c, w);
      w.beginBlock();
      statement.dump(c, w);
      w.endBlock();

      return this;
   }
   
  
   public Node copy() {
      WhileStatement ds = new WhileStatement(condExpr, statement);
      ds.copyAnnotationsFrom(this);
      return ds;
   }

   public Node deepCopy() {
    WhileStatement ds = new WhileStatement((Expression) condExpr.deepCopy(),
					   (Statement) statement.deepCopy());
    ds.copyAnnotationsFrom(this);
    return ds;
  }

  private Expression condExpr;
  private Statement statement;
}

