package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * An <code>ArrayIndexExpression</code> is an immutable representation of an
 * access of an array member.  For instance <code>foo[i]</code> accesses the 
 * <code>i</code>'th member of <code>foo</code>.  An 
 * <code>ArrayIndexExpression</code> consists of a base expression which 
 * evaulates to an array, and an index expression which evaluates to an integer
 * indicating the index of the array to be accessed.
 */
public class ArrayIndexExpression extends Expression 
{
  protected final Expression base;
  protected final Expression index;

  /**
   * Creates a <code>NewArrayIndexExpression</code>, accessing an element of
   * <code>Expression</code> <code>base</code> at index <code>index</code>.
   */
  public ArrayIndexExpression( Expression base, Expression index) 
  {
    this.base = base;
    this.index = index;
  }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   *
   * @param base The new base expression.
   * @param index The new index expression.
   * @return An <code>ArrayIndexExpression<code> with the given base and index.
   */
  public ArrayIndexExpression reconstruct( Expression base, Expression index) 
  {
    if( this.base == base && this.index == index) {
      return this;
    }
    else {
      ArrayIndexExpression n = new ArrayIndexExpression( base, index);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the base of this <code>ArrayIndexExpression</code>.
   */
  public Expression getBase() 
  {
    return base;
  }

  /**
   * Returns the index expression of this.
   */
  public Expression getIndex() 
  {
    return index;
  }

  /** 
   * Visit the children of this node.
   *
   * @pre Requires that <code>base.visit</code> and <code>index.visit</code> 
   *  both return objects of type <code>Expression</code>.
   * @post Returns <code>this</code> if no changes are made, otherwise a copy
   *  is made and returned.
   */  
  Node visitChildren(NodeVisitor v) 
  {
    return reconstruct( (Expression)base.visit( v),
                        (Expression)index.visit( v));
  }

  public Node typeCheck(LocalContext c) throws SemanticException
  {
    Type btype = base.getCheckedType();
    if ( !(btype instanceof ArrayType)) {
      throw new SemanticException(  
                    "Subscript can only follow an array type.");
    }
    else {
      setCheckedType( ((ArrayType)btype).getBaseType());
    }

    Type itype = index.getCheckedType();
    if ( !itype.isImplicitCastValid( c.getTypeSystem().getInt())) {
      throw new SemanticException(
                    "Array subscript must be an integer.");
    } 

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    translateExpression( base, c, w);

    w.write ("[");
    w.begin(0);
    index.translate(c, w);
    w.end();
    w.write ("]");
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "( ARRAY INDEX EXPR ") ;
    dumpNodeInfo( w);
    w.write( ")");
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}



