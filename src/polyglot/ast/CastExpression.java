/*
 * CastExpression.java
 */

package jltools.ast;

import jltools.types.Type;

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
    public TypeNode getCastType () {
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

    public Node accept(NodeVisitor v) {
	return v.visitCastExpression(this);
    }

    /**
     * Requires: v will not transform the Expression into anything
     *    other than another Expression.
     * Effects:
     *    Visits the sub expression of this.
     */ 
    public void visitChildren(NodeVisitor v) {
	expr = (Expression) expr.accept(v);
	type = (TypeNode) type.accept(v);
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

    private Expression expr;
    private TypeNode type;
}

