package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/**
 * An <code>InstanceofExpression</code> is an immutable representation of
 * the use of the <code>instanceof</code> operator.
 */
public class InstanceofExpression extends Expression 
{
  protected final Expression expr;
  protected final TypeNode tn;

  /**
   * Creates a new <code>InstanceofExpreession</code>.
   */
  public InstanceofExpression( Node ext, Expression expr, TypeNode tn)
  {
    this.ext = ext;
    this.expr = expr;
    this.tn = tn;
  }

    public InstanceofExpression( Expression expr, TypeNode tn){
	this(null, expr, tn);
    }

  /**
   * Lazily reconstruct this node.
   */
  public InstanceofExpression reconstruct( Node ext, Expression expr, TypeNode tn)
  {
    if( this.expr == expr && this.tn == tn && this.ext == ext) {
      return this;
    }
    else {
      InstanceofExpression n = new InstanceofExpression( ext, expr, tn);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public InstanceofExpression reconstruct( Expression expr, TypeNode tn) {
	return reconstruct(this.ext, expr, tn);
    }

  /**
   * Returns the expression whose type is being checked.
   */
  public Expression getExpression() 
  {
    return expr;
  }

  /**
   * Returns the type to which the type of the expression is being compared. 
   */
  public Type getType() 
  {
    return tn.getType();
  }
  
  public TypeNode getTypeNode()
  {
	  return tn;
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>expr.visit</code> returns an object of type
   *  <code>Expression</code> and <code>tn.visit</code> returns an object of
   *  type <code>TypeNode</code>.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v),(Expression)expr.visit( v),
                        (TypeNode)tn.visit( v));
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type rtype = tn.getType();

    if( !rtype.isReferenceType()) {
      throw new SemanticException( 
                 "Right operand of \"instanceof\" must be a reference type.",
				  Annotate.getLineNumber(tn));
    }

    Type ltype = expr.getCheckedType();
    if( !ltype.isCastValid( rtype)) {
      throw new SemanticException(
                 "Left operand of \"instanceof\" must be castable to "
                 + "the right operand.", Annotate.getLineNumber(expr));
    }

    setCheckedType( c.getTypeSystem().getBoolean());
    return this;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    translateExpression( expr, c, w);

    w.write( " instanceof ");

    tn.translate( c, w);
  }

  public String toString() {
    return expr + " instanceof " + tn;
  }

  public void dump( CodeWriter w)
  {
    w.write( "INSTANCEOF ");
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_INSTANCE;
  }
}
  
