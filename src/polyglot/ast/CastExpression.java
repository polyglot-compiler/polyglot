/*
 * CastExpression.java
 */

package jltools.ast;

import jltools.util.*;
import jltools.types.*;

/**
 * CastExpression
 * 
 * Overview: A CastExpression is a mutable representation of a casting
 *   operation.  It consists of an Expression being cast and a Type
 *   being cast to.
 */ 

public class CastExpression extends Expression {
    /** 
     * Effects: Creates a new cast expression casting <expr> to type <type>.
     */
    public CastExpression (TypeNode type, Expression expr) {
	this.type = type;
	this.expr = expr;
    }
    /** 
     * Effects: Creates a new cast expression casting <expr> to type <type>.
     */
    public CastExpression (Type type, Expression expr) {
        this(new TypeNode(type), expr);
    }

    /**
     * Effects: Returns the type that this CastExpression is casting to
     */
    public Node getCastType () {
	return type;
    }

    /**
     * Effects: Sets the type that this CastExpression is casting to
     * <newType> 
     */
    public void setCastType(Type newType) {
	type = new TypeNode(newType);
    }

    /**
     * Effects: Sets the type that this CastExpression is casting to
     * <newType> 
     */
    public void setCastType(TypeNode newType) {
	type = newType;
    }


    /**
     * Effects: Returns the expression that is being cast.
     */
    public Expression getExpression () {
      return expr;
    }

    /**
     * Effects: Sets the expression that is being cast to <newExpression>. 
     */
    public void setExpression(Expression newExpression) {
	expr = newExpression;
    }

  public void translate ( LocalContext c, CodeWriter w)
  {
    w.write ("(" );
    type.translate(c, w);
    w.write ( ")" );

    translateExpression( expr, c, w);
  }
  
  public Node dump( CodeWriter w)
  {
    w.write( "( CAST ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public int getPrecedence()
  {
    return PRECEDENCE_CAST;
  }

  public Node typeCheck( LocalContext c) throws TypeCheckException
  {
    if ( ! expr.getCheckedType().isCastValid( type.getType() ) )
      throw new TypeCheckException("Cannot cast the expression of type \"" +
                                   expr.getCheckedType().getTypeString() + "\" to type \"" + 
                                   type.getType().getTypeString() + "\"");
    Annotate.addThrows ( this, Annotate.getThrows( expr ) );
    setCheckedType( type.getType() );

    return this;
  }


  /**
   * Requires: v will not transform the Expression into anything
   *    other than another Expression.
   * Effects:
   *    Visits the sub expression of this.
   */ 
  Object visitChildren(NodeVisitor v) 
  {
    Object vinfo = Annotate.getVisitorInfo( this);
    
    expr = (Expression) expr.visit(v);
    vinfo = v.mergeVisitorInfo( Annotate.getVisitorInfo( expr), vinfo);
    
    type = (TypeNode) type.visit(v);
    return v.mergeVisitorInfo( Annotate.getVisitorInfo( type), vinfo);
  }
  
    public Node copy() {
      CastExpression ce = new CastExpression(type, expr);
      ce.copyAnnotationsFrom(this);
      return ce;
    }

    public Node deepCopy() {
      CastExpression ce = 
	new CastExpression((TypeNode) type.deepCopy(), 
			   (Expression) expr.deepCopy());
      ce.copyAnnotationsFrom(this);
      return ce;     
    }

    protected Expression expr;
    protected TypeNode type;
}

