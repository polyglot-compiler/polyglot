/*
 * Node.java
 */

package jltools.ast;

import java.util.*;

import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.SymbolReader;


/**
 * Node
 *
 * Overview: A Node is an AST node.  All other nodes in the AST must
 * be subclasses of Node. All nodes are mutable.
 **/
public abstract class Node extends jltools.util.AnnotatedObject {

  
  /**
   * Object visitChildren(NodeVisitor vis)
   *
   * Used by the subclasses of NodeVisitor.  Applies accept(vis) to
   * every child of this node, replacing that child with the return value.
   **/
  abstract Object visitChildren(NodeVisitor vis);

  public Node visit(NodeVisitor vis)
  {
    Node n;

    Annotate.removeVisitorInfo( this);

    n = vis.visitBefore(this);

    if(n != null) {
      return n;
    }
    else {
      Object vinfo = visitChildren(vis);
      return vis.visitAfter( this, vinfo);
    }
  }

  public abstract Node readSymbols( SymbolReader sr) throws TypeCheckException;

  public Node adjustScope( LocalContext c)
  {
    return null; 
  }
  
  public Node resolveAmbiguities(LocalContext c) throws TypeCheckException
  {
    return this;
  }

  public Node removeAmbiguities( NodeVisitor vis, LocalContext c) throws TypeCheckException
  {
    return removeAmbiguities( c ) ;
  }

  public Node removeAmbiguities( LocalContext c) throws TypeCheckException
  { 
    return this; 
  }

  public abstract Node typeCheck( LocalContext c) throws TypeCheckException;
  
  public abstract void translate( LocalContext c, CodeWriter w);
  
  public abstract Node dump( CodeWriter w) throws TypeCheckException;

  /**
   * Dumps the attributes to the writer, if the attributes have been set
   */
  public void dumpNodeInfo( CodeWriter w)
  {
    Type type = Annotate.getCheckedType( this);
    if( type != null) {
      w.write( "T: " + type.getTypeString() + " ");
    }
    type = Annotate.getExpectedType( this);
    if( type != null) {
      w.write( "E: " + type.getTypeString() + " ");
    }
    Object o = Annotate.getVisitorInfo( this);
    if( o != null) {
      w.write( "ERROR ");
    }
  }

  /**
   * Node copy()
   *
   * Returns a new node with the same, contents, and annotations as
   *  this.  This is a shallow copy; if some object is stored under
   *  this node, an identical object will be stored under the copied
   *  node.
   **/
  public abstract Node copy();

  /**
   * Node deepCopy()
   *
   * Returns a new node with the same type, contents, and annotations
   * as this.  Any changes made to the new node, or any subnode of
   * that node, are guaranteed not to affect this.  In other words,
   * this method performs a deep copy.
   **/
  public abstract Node deepCopy();

  /**
   * Return a new array containing all the elements of lst, in the same order.
   *
   * Used to implement many copy functions.
   **/
  public static List copyList(List lst) {
    ArrayList newList = new ArrayList(lst.size());
    for (Iterator it = lst.iterator(); it.hasNext(); ) {
      newList.add( it.next() );
    }
    return newList;
  }

  /**
   * Return a new array containing all the elements of lst, in the same order,
   * after a deep copy operation.
   *
   * Used to implement many deepCopy functions.
   **/
  public static List deepCopyList(List lst) {
    ArrayList newList = new ArrayList(lst.size());
    for (Iterator it = lst.iterator(); it.hasNext(); ) {
      newList.add( ((Node) it.next()).deepCopy() );
    }
    return newList;
  }

  public void addThrows( SubtypeSet s ) 
  {
    Annotate.addThrows ( this, s) ;
  }

  public SubtypeSet getThrows ( )
  {
    return Annotate.getThrows ( this ) ;
  }

  public boolean completesNormally( )
  {
    return Annotate.completesNormally( this );
  }

  public void setCompletesNormally( boolean b )
  {
    Annotate.setCompletesNormally( this , b);
  }

  public boolean isReachable()
  {
    return Annotate.isReachable( this ) ;
  }

  public void setReachable(boolean b)
  {
    Annotate.setReachable(this, b);
  }


}

