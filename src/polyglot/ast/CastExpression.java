/*
 * CastExpression.java
 */

package jltools.ast;

import jltools.types.Type;
import jltools.util.CodeWriter;
import jltools.types.LocalContext;

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
    public CastExpression (Node type, Expression expr) {
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
    public void setCastType(Node newType) {
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
    w.write (" (( " );
    type.translate(c, w);
    w.write ( " ) " );
    expr.translate(c, w);
    w.write ( " )");
  }
  
  public void dump (LocalContext c, CodeWriter w)
  {
    w.write (" ( CAST to " );
    type.dump(c, w);
    expr.dump(c, w);
    w.write (" )");
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME: implement;
    return this;
  }


    /**
     * Requires: v will not transform the Expression into anything
     *    other than another Expression.
     * Effects:
     *    Visits the sub expression of this.
     */ 
    public void visitChildren(NodeVisitor v) {
	expr = (Expression) expr.visit(v);
	type = (TypeNode) type.visit(v);
    }
  
    public Node copy() {
      CastExpression ce = new CastExpression(type, expr);
      ce.copyAnnotationsFrom(this);
      return ce;
    }

    public Node deepCopy() {
      CastExpression ce = 
	new CastExpression((Node) type.deepCopy(), 
			   (Expression) expr.deepCopy());
      ce.copyAnnotationsFrom(this);
      return ce;     
    }

    private Expression expr;
    private Node type;
}

