package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * A ReturnStatment is an immutable representation of a <code>return</code>
 * statement in Java.
 */
public class ReturnStatement extends Statement 
{
  protected final Expression expr;

  /**
   * Creates a new <code>ReturnStatement</code> which returns 
   * <code>expr</code>.
   *
   * @param expr The expression to be returned. May optionally be 
   *  <code>null</code> if no expression is returned.
   */
  public ReturnStatement( Node ext, Expression expr) 
  {
    this.ext = ext;
    this.expr = expr;
  }
  
    public ReturnStatement( Expression expr) {
	this(null, expr);
    }

  /**
   * Lazily reconstruct this node.
   */
  public ReturnStatement reconstruct( Node ext, Expression expr)
  {
    if( this.expr == expr && this.ext == ext) {
      return this;
    }
    else {
      ReturnStatement n = new ReturnStatement( ext, expr);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public ReturnStatement reconstruct( Expression expr) {
	return reconstruct(this.ext, expr);
    }

  /**
   * Returns the expression which would be returned by this statement.
   */ 
  public Expression getExpression() 
  {
    return expr;
  }

  /** 
   * Visit the children of this node.
   *
   * @pre Requires that <code>expr.visit</code> returns an object of type
   *  <code>Expression</code>.
   */
  public Node visitChildren( NodeVisitor v) 
  {
      return reconstruct( Node.condVisit(this.ext, v), (Expression)Node.condVisit(expr, v));
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    MethodTypeInstance mti = c.getCurrentMethod();
    if (mti instanceof MethodTypeInstanceInitializer)
      throw new SemanticException( "Return statements are not valid inside an " +
                                   "initializer block.",
				  Annotate.getPosition(this));
                                  
    if( expr == null) {
      if( !mti.isConstructor() &&
          !mti.getReturnType().equals( c.getTypeSystem().getVoid())) {
        throw new SemanticException( 
                          "Method \"" + mti.getName() + "\" must return "
                          + "an expression of type \"" 
                          + mti.getReturnType().getTypeString() + "\".",
			  Annotate.getPosition(this));

      }
    }
    else {
      if( mti.getReturnType().equals( c.getTypeSystem().getNull())) {
        throw new SemanticException(
                       "A return statement which returns a value can only"
                       + " occur in a method which does not have type void.",
			Annotate.getPosition(this));
      }
      else if( ! expr.getCheckedType().isImplicitCastValid ( mti.getReturnType()) &&
               ! (( expr instanceof NumericalLiteral) && 
                    c.getTypeSystem().numericConversionValid( 
                                         mti.getReturnType(), 
                                         ((NumericalLiteral)expr).getValue()))) {
        throw new SemanticException( 
                          "Method \"" + mti.getName() + "\" must return "
                          + "an expression of type \"" 
                          + mti.getReturnType().getTypeString() + "\".",
			  Annotate.getPosition(this));
      } 
    }
    
    return this;
  }
  
  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    w.write( "return") ;
    if( expr != null) {
      w.write( " ");
      expr.translate( c, w);
    }
    w.write( ";");
  }

  public String toString() {
    return "return" + (expr != null ? (" " + expr) : "") + ";";
  }

  public void dump( CodeWriter w)
  {
    w.write( "RETURN ");
    dumpNodeInfo( w);
  }
}
