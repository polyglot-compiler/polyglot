/*
 * Node.java
 */

package jltools.ast;

import java.util.*;

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
   * void visitChildren(NodeVisitor vis)
   *
   * Used by the subclasses of NodeVisitor.  Applies accept(vis) to
   * every child of this node, replacing that child with the return value.
   **/
  abstract void visitChildren(NodeVisitor vis);

  public Node visit(NodeVisitor vis)
  {
    Node n;
    
    n = vis.visitBefore(this);
    if(n != null)
      return n;
    else {
      visitChildren(vis);
      return vis.visitAfter(this);
    }
  }
  
  public Node resolveAmbiguities(LocalContext c)
  {
    return this;
  }

   /**
    * Dumps the attributes to the writer, if the attributes have been set
    */
   public void dumpNodeInfo(LocalContext c, CodeWriter w)
   {
      //FIXME: Do this
   }

  public abstract Node readSymbols( SymbolReader sr);

  public abstract Node typeCheck(LocalContext c);
  
  public abstract void translate(LocalContext c, CodeWriter w);
  
  public abstract void dump(LocalContext c, CodeWriter w);

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

  public void setError(int errType, String errMsg)
  {
    ErrorInfo err = new ErrorInfo(errType, errMsg);
    Annotate.addError(this, err);
    
    jltools.frontend.Compiler.enqueueError(Annotate.getLineNumber(this), err);
  }
}

