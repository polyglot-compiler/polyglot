/*
 * NewArrayExpression.java
 */

package jltools.ast;

import jltools.types.Type;

// FIXME: This doesn't handle multidimensional or initialized arrays.


/**
 * NewArrayExpression
 *
 * Overview: A NewArrayExpression is a mutable representation of the
 *   creation of a new array such as "new Object[8]".  It consists of a 
 *   type of the array and an expression indicates the size of the array.
 */

public class NewArrayExpression extends Expression {
    /**
     * Effects: Creates a new NewArrayExpression of type <type> and
     *   a length expression of <lengthExpr>.
     */
    public NewArrayExpression(Type type, Expression lengthExpr) {
	this.type = type;
	this.expr = lengthExpr;
    }

    /**
     * Effects: Returns the type of the array being created. 
     */
    public Type getArrayType() {
	return type;
    }

    /** 
     * Effects: Sets the type of the array being create to <newType>.
     */
    public void setArrayType(Type newType) {
	type = newType;
    }

    /** 
     * Effects: Returns the expression indicating the length of the array.
     */
    public Expression getArrayLengthExpression() {
	return expr;
    }
    
    /** 
     * Effects: Changes the expression indicating the length of the
     *   array to be <newLengthExpr>.
     */
    public void setArrayLengthExpression(Expression newExpr) {
	expr = newExpr;
    }

    public Node accept(NodeVisitor v) {
	return v.visitNewArrayExpression(this);
    }

    /** 
     * Requires: v will not transform the length Expression into
     *   anything other than another Expression.
     * Effects: visits the subexpression of this.
     */
    public void visitChildren(NodeVisitor v) {
	expr = (Expression) expr.accept(v);
    }


    private Expression expr;
    private Type type;
}

    
