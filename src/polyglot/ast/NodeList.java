/*
 * This file is part of the Polyglot extensible compiler framework. Copyright
 * (c) 2000-2007 Polyglot project group, Cornell University Copyright (c)
 * 2006-2007 IBM Corporation
 */

package polyglot.ast;

import java.util.List;

/**
 * A <code>NodeList</code> represents a list of AST nodes.
 * <code>NodeList</code>s are not intended to appear as part of the AST. When
 * a node is visited, it may replace itself with multiple nodes by returning a
 * <code>NodeList</code> to the visitor. The rewritten node's parent would
 * then be responsible for properly splicing those nodes into the AST.
 */
public interface NodeList extends Node {
  /**
   * Get the <code>NodeFactory</code> to use when converting the list to a
   * proper AST node.
   */
  NodeFactory nodeFactory();
  
  /** Get the nodes contained in the list. */
  List nodes();
  
  /** Set the nodes contained in the list. */
  NodeList nodes(List nodes);
  
  /** Convert the list into a <code>Block</code>. */
  Block toBlock();
}
