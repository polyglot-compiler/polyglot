/*
 * ThrowStatement.java
 */

package jltools.ast;
import jltools.util.*;
import jltools.types.*;

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
  Object visitChildren(NodeVisitor v)
  {
    expr = (Expression)expr.visit( v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( this),
                               Annotate.getVisitorInfo( expr));
  }

   public Node typeCheck(LocalContext c) throws TypeCheckException
   {
     if (! expr.getCheckedType().isThrowable())
       throw new TypeCheckException("Can only throw objects that extend from \"java.lang.Throwable\"");
     Annotate.addThrows ( this, expr.getCheckedType()  );
     Annotate.addThrows ( this, Annotate.getThrows( expr ) );
     Annotate.setTerminatesOnAllPaths (this, true);
     return this;
   }

   public void  translate(LocalContext c, CodeWriter w)
   {
      w.write("throw ");
      expr.translate(c, w);
      w.write(";");
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( THROW");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
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
  

  
