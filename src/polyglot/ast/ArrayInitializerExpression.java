package jltools.ast;

import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

import jltools.types.*;
import jltools.util.*;


/**
 * An <code>ArrayInitializerExpression</code> is an immutable representation of
 * the an array initializer, such as { 3, 1, { 4, 1, 5 } }. Note that the 
 * elements of these array may be expressions of any type (e.g. 
 * <code>MethedExpression</code>).
 */
public class ArrayInitializerExpression extends Expression 
{
  protected final List children;

  /**
   * Creates a new, empty <code>ArrayInitializerExpression</code>.
   */
  public ArrayInitializerExpression() {
    children = new ArrayList();
  }

  /**
   * Creates a new <code>ArrayInitializerExpression</code> with 
   * <code>list</code> as its elements.
   *
   * @pre Each element of <code>list</code> is an expression.
   */
  public ArrayInitializerExpression( List list) {
    children = TypedList.copyAndCheck( list, Expression.class, true);
  }

  /** 
   * Lazily reconstuct this node.
   */
  public ArrayInitializerExpression reconstruct( List list) {
    if( list.size() != children.size()) {
      ArrayInitializerExpression n = new ArrayInitializerExpression( list);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < list.size(); i++) {
        if( list.get( i) != children.get( i)) {
          ArrayInitializerExpression n = new ArrayInitializerExpression( list);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }      
      return this;
    }
  }

  /**
   * Returns a Iterator which yields every child of this expression,
   * in order.
   */
  public Iterator children() {
    return children.iterator();
  }

  /**
   * Returns the <code>pos</code>'th child of this expression.
   */
  public Expression getChildAt( int pos) 
  {
    return (Expression)children.get( pos); 
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that the <code>visit</code> method of each child returns
   *  an object of type <code>Expression</code>.
   */
  Node visitChildren( NodeVisitor v) 
  {
    List list = new ArrayList( children.size());

    for( Iterator iter = children(); iter.hasNext(); ) {
      Expression expr = (Expression)((Expression)iter.next()).visit( v);
      if( expr != null) {
        list.add( expr);
      }
    }
    return reconstruct( list);
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type type = null;
    Expression child;

    for( Iterator iter = children(); iter.hasNext(); ) {
      child = (Expression)iter.next();

      type = (type == null ? child.getCheckedType() :
              c.getTypeSystem().leastCommonAncestor( type,
                                  child.getCheckedType()));
    }
    
    setCheckedType( type);
    
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "{ ");
    for( Iterator iter = children(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate(c, w);
      if( iter.hasNext()) {
        w.write(", ");
      }
    }
    w.write( " }");
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "( ARRAY INITIALIZER ");
    dumpNodeInfo( w);
    w.write( ")");
  }
  
  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
  
