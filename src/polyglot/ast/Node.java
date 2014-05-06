/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.io.Serializable;

import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

/**
 * A {@code Node} represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface Node extends NodeOps, Copy<Node>, Serializable {
    /**
     * Set the delegate of the node.
     */
    @Deprecated
    Node del(JLDel del);

    /**
     * Get the node's delegate.
     */
    @Deprecated
    NodeOps del();

    /**
     * Set the extension of the node.
     */
//    @Deprecated
    Node ext(Ext ext);

    /**
     * Get the node's extension.
     */
    Ext ext();

    /**
     * Set the node's nth extension, n &gt;= 1.
     */
//    @Deprecated
    Node ext(int n, Ext ext);

    /**
     * Get the node's nth extension, n &gt;= 1.
     */
    Ext ext(int n);

    /**
     * Get the position of the node in the source file.  Returns null if
     * the position is not set.
     */
    Position position();

    /** Create a copy of the node with a new position. */
    Node position(Position position);

    /** Return true if there an error in this node or its children. */
    boolean error();

    Node error(boolean flag);

    /**
     * @return true if the all type information for the node (but not necessarily
     * for the node's children) is unambiguous.
     */
    boolean isDisambiguated();

    /**
     * @return true if all the type information for the node (but not necessarily
     * for the node's children) has been computed.
     */
    boolean isTypeChecked();

    /**
     * Visit the node.  This method is equivalent to
     * {@code visitEdge(null, v)}.
     *
     * @param v The visitor which will traverse/rewrite the AST.
     * @return A new AST if a change was made, or {@code this}.
     */
    Node visit(NodeVisitor v);

    /**
     * Visit the node, passing in the node's parent.  This method is called by
     * a {@code NodeVisitor} to traverse the AST starting at this node.
     * This method should call the {@code override}, {@code enter},
     * and {@code leave} methods of the visitor.  The method may return a
     * new version of the node.
     *
     * @param parent The parent of {@code this} in the AST.
     * @param v The visitor which will traverse/rewrite the AST.
     * @return A new AST if a change was made, or {@code this}.
     * 
     * @deprecated Call {@link Node#visitChild(Node, NodeVisitor)} instead.
     */
    @Deprecated
    Node visitEdge(Node parent, NodeVisitor v);

    /**
     * Dump the AST node for debugging purposes.
     */
    void dump(CodeWriter w);
}
