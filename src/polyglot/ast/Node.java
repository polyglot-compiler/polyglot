/*
 * Node.java
 */

package jltools.ast;

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
   * Node accept(NodeVisitor vis)
   *
   * Used by the subclasses of NodeVisitor in order to implement the
   *   Visitor design pattern.  May change this node and its children,
   *   and returns a replacement for this node.
   **/
  public abstract Node accept(NodeVisitor vis);
  
  /**
   * void visitChildren(NodeVisitor vis)
   *
   * Used by the subclasses of NodeVisitor.  Applies accept(vis) to
   * every child of this node, replacing that child with the return value.
   **/
  public abstract void visitChildren(NodeVisitor vis);

}

