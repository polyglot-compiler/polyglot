/*
 * IfStatement.java
 */

package jltools.ast;
import jltools.types.LocalContext;
import jltools.util.CodeWriter;

/**
 * IfStatement
 *
 * Overview: A mutable representation of a Java language if statement.
 *    Contains an expression whose value is tested, a then statement, and
 *    optionally an else statement.
 */
public class IfStatement extends Statement {
  /**
   * Effects: Creates a new IfStatement with conditional expression
   *    <condExpr>, a then statement <thenStatement> and else statement
   *    <elseStatement>.  If there is no else statement <elseStatement>
   *    should be null.
   */
  public IfStatement (Expression condExpr, Statement thenStatement,
		      Statement elseStatement) {
    this.condExpr = condExpr;
    this.thenStatement = thenStatement;
    this.elseStatement = elseStatement;
  }

  public IfStatement (Expression condExpr, Statement thenStatement) {
    this(condExpr, thenStatement, null);
  }

  /**
   * Effects: Returns the Expression that this IfStatement is
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
   * Effects: Returns the then statement associated with this
   *    IfStatement.
   */
  public Statement getThenStatement() {
    return thenStatement;
  }

  /**
   * Effects: Sets the then statement of this IfExpression to be
   *    <newStatement>.
   */
  public void setThenStatement(Statement newStatement) {
    thenStatement = newStatement;
  }

  /**
   * Effects: Returns the else statement associated with this
   *    IfStatement.
   */
  public Statement getElseStatement() {
    return elseStatement;
  }

  /**
   * Effects: Sets the else statement of this IfExpression to be
   *    <newStatement>.
   */
  public void setElseStatement(Statement newStatement) {
    elseStatement = newStatement;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    boolean bThenBlockStatement, bElseBlockStatement;
    bThenBlockStatement = thenStatement instanceof BlockStatement;
    bElseBlockStatement = elseStatement instanceof BlockStatement;
    
    w.write ( "if ( " ) ;
    condExpr.translate ( c, w);
    w.write ( " ) " );
    if (! bThenBlockStatement)
    {
      w.beginBlock();
      thenStatement.translate(c, w);
      w.endBlock();
    }
    else
      thenStatement.translate(c, w);
    if ( elseStatement != null)
    {
      w.write ( " else " );
      if (! bElseBlockStatement)
      {
        w.beginBlock();
        elseStatement.translate(c, w);
        w.endBlock();
      }
      else
        elseStatement.translate(c,w );
    }

  }

  public void dump(LocalContext c, CodeWriter w)
  {
    w.write ( "( IF " ) ;
    dumpNodeInfo(c, w);
    w.write (" ( " );
    condExpr.dump ( c, w);
    w.write ( " ) " );
    w.beginBlock();
    w.write ( " ( ");
    thenStatement.translate(c, w);
    w.write ( " ) ");
    w.endBlock();
    if ( elseStatement != null)
    {
      w.write ( " ( " );
      w.beginBlock();
      elseStatement.translate(c, w);
      w.write ( " ) ");
      w.endBlock();
    }
    w.write ( ") ");
  }

  public Node typeCheck(LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  /**
   * Requires: v will not transform an expression into anything other
   *    than another expression, and that v will not transform a
   *    Statement into anything other than another Statement or
   *    Expression.
   * Effects: visits each of the children of this node with <v>.  If <v>
   *    returns an expression in place of one of the sub-statements, it is
   *    wrapped in an ExpressionStatement.
   */
  public void visitChildren(NodeVisitor v) {
    condExpr = (Expression) condExpr.visit(v);
    Node newNode = (Node) thenStatement.visit(v);
    if (newNode instanceof Expression) {
      thenStatement = new ExpressionStatement((Expression) newNode);
    }
    else {
      thenStatement = (Statement) newNode;
    }

    newNode = (Node) elseStatement.visit(v);
    if (newNode instanceof Expression) {
      elseStatement = new ExpressionStatement((Expression) newNode);
    }
    else {
      elseStatement = (Statement) newNode;
    }
  }

  public Node copy() {
    IfStatement is = new IfStatement(condExpr, thenStatement, elseStatement);
    is.copyAnnotationsFrom(this);
    return is;
  }

  public Node deepCopy() {
    IfStatement is = new IfStatement((Expression) condExpr.deepCopy(),
				     (Statement) thenStatement.deepCopy(),
				     (Statement) elseStatement.deepCopy());
    is.copyAnnotationsFrom(this);
    return is;
  }

  private Expression condExpr;
  private Statement thenStatement;
  private Statement elseStatement;
}
