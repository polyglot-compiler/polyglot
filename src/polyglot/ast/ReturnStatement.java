/*
 * ReturnStatement.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;
/**
 * ReturnStatement
 * 
 * Overview: A ReturnStatment is a mutable representation of a return
 *   statement in Java.
 */
public class ReturnStatement extends Statement {
  /**
   * Effects: Creates a new ReturnStatement which returns <expr>.
   */
  public ReturnStatement (Expression expr) {
    this.expr = expr;
  }

  /**
   * Effects: Returns the expression which would be returned by
   * this ReturnStatement.
   */ 
  public Expression getExpression() {
    return expr;
  }

  /**
   * Effects: Sets the Expression to be returned by this to be <newExpr>.
   */
  public void setExpression(Expression newExpr) {
    expr = newExpr;
  }

  /** 
   * Requires: v will not transform the expression into anything other than
   *   another expression.
   */
  public void visitChildren(NodeVisitor v) {
    expr = (Expression) expr.visit(v);
  }
  
  public void translate(LocalContext c, CodeWriter w)
  {
    w.write("return ") ;
    expr.translate(c, w);
    w.write(";");
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( RETURN ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node typeCheck(LocalContext c) throws TypeCheckException
  {
    MethodTypeInstance mti = c.getCurrentMethod() ;
    if ( ! expr.getCheckedType().descendsFrom( mti.getReturnType() ) &&
         ! expr.getCheckedType().equals( mti.getReturnType() ))
      throw new TypeCheckException ( "The mehtod body says the return type is \"" + 
                                     mti.getReturnType().getTypeString() + "\", however, the return statement " +
                                     "returns an instnace of type \"" + 
                                     expr.getCheckedType().getTypeString() + "\"");

      

    Annotate.setTerminatesOnAllPaths (this, true);
    addThrows ( expr.getThrows() );
    return this;
  }

  public Node copy() {
    ReturnStatement rs = new ReturnStatement(expr);
    rs.copyAnnotationsFrom(this);
    return rs;
  }

  public Node deepCopy() {
    ReturnStatement rs = new ReturnStatement((Expression) expr.deepCopy());
    rs.copyAnnotationsFrom(this);
    return rs;
  }

  private Expression expr;
}
