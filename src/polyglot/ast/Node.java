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

