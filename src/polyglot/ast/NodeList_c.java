/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.List;

import polyglot.util.ListUtil;
import polyglot.util.Position;

/**
 * A <code>NodeList</code> represents a list of AST nodes.
 * <code>NodeList</code>s are not intended to appear as part of the AST. When
 * a node is visited, it may replace itself with multiple nodes by returning a
 * <code>NodeList</code> to the visitor. The rewritten node's parent would
 * then be responsible for properly splicing those nodes into the AST.
 */
public class NodeList_c<N extends Node> extends Node_c implements NodeList<N> {
  protected NodeFactory nf;
  protected List<N> nodes;

  public NodeList_c(Position pos, NodeFactory nf, List<N> nodes) {
    super(pos);
    assert (nodes != null);
    this.nf = nf;
    this.nodes = ListUtil.copy(nodes, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodes()
   */
  @Override
  public List<N> nodes() {
    return nodes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodes(java.util.List)
   */
  @Override
  public NodeList<N> nodes(List<N> nodes) {
    NodeList_c<N> result = copy();
    result.nodes = ListUtil.copy(nodes, true);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public NodeList_c<N> copy() {
    return (NodeList_c<N>) super.copy();
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#nodeFactory()
   */
  @Override
  public NodeFactory nodeFactory() {
    return nf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see polyglot.ast.NodeList#toBlock()
   */
  @Override
  public Block toBlock() {
    @SuppressWarnings("unchecked")
    List<Stmt> stmts = (List<Stmt>) nodes;
    return nf.Block(position, stmts);
  }

}
