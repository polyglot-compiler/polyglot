package jltools.ast;

import jltools.types.*;
import jltools.util.*;

import java.util.*;


/**
 * A <code>NewArrayExpression</code> is an immutable representation of the
 * creation of a new array such as <code>new File[8][]</code>.  It consists of
 * an element type, in the above example <code>File</code>, a list of dimension
 * expressions (expressions which evaluate to a length for a
 * dimension in this case <code>8</code>), and a number representing the
 * number optional dimensions (in the example 1).
 */
public class NewArrayExpression extends Expression 
{
  protected final TypeNode base;
  /** A list of <code>Expression</code>s. */
  protected final List dimExprs;
  protected final int addDims;
  protected final ArrayInitializerExpression init;

  /**
   * Creates a new <code>NewArrayExpression</code>.
   *
   * @param init The initializer expression. This argument may be 
   *  <code>null</code> if there is not such expression.
   * @pre <code>addDims</code> >= 0 and each element of <code>dimExprs</code>
   *  is of type <code>Expression</code>.
   */ 
  public NewArrayExpression( Node ext, TypeNode base, List dimExprs, int addDims, 
                             ArrayInitializerExpression init)
  {
    if (addDims < 0) {
      throw new IllegalArgumentException( "The number of additional dimensions"
                                         + " must be positive.");
    }
    this.ext = ext;
    this.base = base;
    this.dimExprs = TypedList.copyAndCheck( dimExprs, Expression.class, true);
    this.addDims = addDims;
    this.init = init;
  }

  public NewArrayExpression( TypeNode base, List dimExprs, int addDims, 
                             ArrayInitializerExpression init) {
      this(null, base, dimExprs, addDims, init);
  }


  /**
   * Lazily reconstruct this node. 
   */
  public NewArrayExpression reconstruct( Node ext, TypeNode base, List dimExprs,
                                         int addDims,
                                         ArrayInitializerExpression init)
  {
    if( this.base != base || this.ext != ext || this.dimExprs.size() != dimExprs.size()
        || this.addDims != addDims || this.init != init) {
      NewArrayExpression n = new NewArrayExpression( ext, base, dimExprs, addDims,
                                                     init);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < dimExprs.size(); i++) {
        if( this.dimExprs.get( i) != dimExprs.get( i)) {
          NewArrayExpression n = new NewArrayExpression( ext, base, dimExprs,
                                                         addDims, init);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  public NewArrayExpression reconstruct( TypeNode base, List dimExprs,
                                         int addDims,
                                         ArrayInitializerExpression init) {
      return reconstruct(this.ext, base, dimExprs, addDims, init);
  }

  
  /**
   * Returns the type of the array being created. That is, the type without
   * any array dimensions. For the expression <code>new File[8][]</code>
   * the method returns the type <code>File</code>.
   */
  public Type getBaseType() 
  {
    return base.getType();
  }
  
  public TypeNode getBaseTypeNode()
  {
	return base;
  }
  
  /**
   * Returns the number of additional dimensions which do not have a defined
   * length.
   */
  public int getAdditionalDimensions() 
  {
    return addDims;
  }

  /**
   * Returns the initalizer for this expression, or <code>null</code> if none
   * exists.
   */
  public ArrayInitializerExpression getInitializer()
  {
    return init;
  }
  
  /**
   * Returns the total dimensionality of the array.  For example 
   * <code>File[8][][]</code> is three dimensional.  
   */
  public int getDimensions() 
  {
    return dimExprs.size() + addDims;
  }

  /**
   * Returns the expression specifing the length of the <code>pos</code>'th
   * dimension.
   */
  public Expression dimensionExpressionAt( int pos) 
  {
    return (Expression)dimExprs.get( pos);
  }  

  /**
   * Returns a iterator which will yield each expression
   * which specifies the length of a dimension of this node.
   */
  public Iterator dimensionExpressions() 
  {
    return dimExprs.iterator();
  }

  /** 
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    TypeNode newBase = (TypeNode)base.visit( v);

    List newDimExprs = new ArrayList( dimExprs.size());

    for( Iterator iter = dimensionExpressions(); iter.hasNext(); ) {
      Expression expr = (Expression)((Expression)iter.next()).visit( v);
      if( expr != null) {
        newDimExprs.add( expr);
      }
    }
    
    ArrayInitializerExpression newInit = null;

    if( init != null) {
      newInit = (ArrayInitializerExpression)init.visit(v);
    }
    
    return reconstruct( Node.condVisit(this.ext, v),newBase, newDimExprs, addDims, newInit);
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type type = new ArrayType( c.getTypeSystem(), base.getType(),
                                   getDimensions());

    // Check that the initializer has the correct type.
    if (init != null &&
	! c.getTypeSystem().isAssignableSubtype(init.getCheckedType(), type)) {
      throw new SemanticException("An array initializer must be the same " +
				  "type as the array declaration",
				  Annotate.getLineNumber(init));
    }

    setCheckedType( type );

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "new ");
    base.translate( c, w);
    for( Iterator iter = dimensionExpressions(); iter.hasNext(); ) {
      w.write( "[");
      w.begin(0);
      ((Expression)iter.next()).translate( c, w);
      w.end();
      w.write( "]");
    }

    for( int i = 0; i < addDims; i++) { 
      w.write( "[]");
    }

    if( init != null) {
      init.translate( c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "NEW");
    w.write( " < " + addDims + " > ");
    dumpNodeInfo( w);
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}

    
