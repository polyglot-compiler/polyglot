package jltools.ast;

import jltools.util.*;
import jltools.visit.*;
import jltools.types.*;

/**
 * A <code>CastExpression</code> is an immutable representation of a casting
 * operation.  It consists of an <code>Expression</code> being cast and a
 * <code>Type</code> being cast to.
 */ 
public class CastExpression extends Expression
{
  protected final TypeNode tn;
  protected final Expression expr;

  /** 
   * Creates a new cast expression casting <code>expr</code>> to type
   * <code>type</code>.
   */
  public CastExpression( Node ext, TypeNode tn, Expression expr) 
  {
    this.ext = ext;
    this.tn = tn;
    this.expr = expr;
  }
  
    public CastExpression( TypeNode tn, Expression expr) {
	this(null, tn, expr);
    }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   *
   * @param tn The new type of the cast.
   * @param expr The expression that is being cast.
   * @return An <code>CastExpression<code> with the given type and expression.
   */
  public CastExpression reconstruct( Node ext, TypeNode tn, Expression expr)
  {
    if( this.tn == tn && this.expr == expr && this.ext == ext) {
      return this;
    }
    else {
      CastExpression n = new CastExpression( ext, tn, expr);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public CastExpression reconstruct( TypeNode tn, Expression expr) {
	return reconstruct(this.ext, tn, expr);
    }

  /**
   * Returns the type that this <code>CastExpression</code> is casting to.
   */
  public Type getCastType() 
  {
    return tn.getType();
  }

  /**
   * Returns the expression that is being cast.
   */
  public Expression getExpression()
  {
    return expr;
  }

  /**
   * return the type node
   */
  public TypeNode getTypeNode() {
	  return tn; 
  }
  
  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>tn.visit</code> returns an object of type
   *  <code>TypeNode</code> and that <code>expr.visit</code> returns an
   *  object of type <code>Expression</code>.
   */ 
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v), (TypeNode)tn.visit( v),
                        (Expression)expr.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    if ( !expr.getCheckedType().isCastValid( tn.getType()))
      throw new SemanticException( "Cannot cast the expression of type \"" 
                                   + expr.getCheckedType().getTypeString() 
                                   + "\" to type \"" 
                                   + tn.getType().getTypeString() 
                                   + "\".",
				   Annotate.getPosition(this) );

    setCheckedType( tn.getType());
    return this;
  }
  

  public Node exceptionCheck(ExceptionChecker ec)
    throws SemanticException
  {
    if (expr.getCheckedType().isReferenceType()) {
      TypeSystem ts = ec.getTypeSystem();
      ec.throwsException((ClassType) ts.getClassCastException());
    }

    return this;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    w.begin(0);
    w.write("(");
    tn.translate(c, w);
    w.write(")");
    w.allowBreak(2, "");

    translateExpression(expr, c, w);
    w.end();
  }

  public String toString() {
    return "(" + tn + ")" + expr;
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "CAST ");
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_CAST;
  }
}

