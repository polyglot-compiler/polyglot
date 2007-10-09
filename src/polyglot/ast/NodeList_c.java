/*
 * This file is part of the Polyglot extensible compiler framework. Copyright
 * (c) 2000-2007 Polyglot project group, Cornell University Copyright (c)
 * 2006-2007 IBM Corporation
 */

package polyglot.ast;

import java.util.List;

import polyglot.util.Position;
import polyglot.util.TypedList;

/**
 * A <code>NodeList</code> represents a list of AST nodes.
 * <code>NodeList</code>s are not intended to appear as part of the AST. When
 * a node is visited, it may replace itself with multiple nodes by returning a
 * <code>NodeList</code> to the visitor. The rewritten node's parent would
 * then be responsible for properly splicing those nodes into the AST.
 */
public class NodeList_c extends Node_c implements NodeList {
  protected NodeFactory nf;
  protected List nodes;

  public NodeList_c(Position pos, NodeFactory nf, List nodes) {
    super(pos);
    assert (nodes != null);
    this.nf = nf;
    this.nodes = TypedList.copyAndCheck(nodes, Node.class, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodes()
   */
  public List nodes() {
    return nodes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodes(java.util.List)
   */
  public NodeList nodes(List nodes) {
    NodeList_c result = (NodeList_c) copy();
    result.nodes = TypedList.copyAndCheck(nodes, Node.class, true);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodeFactory()
   */
  public NodeFactory nodeFactory() {
    return nf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#toBlock()
   */
  public Block toBlock() {
    return nf.Block(position, nodes);
  }

}
