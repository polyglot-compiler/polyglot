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

package polyglot.visit;

import polyglot.ast.JLangToJLDel;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * The {@code NodeVisitor} represents an implementation of the "Visitor"
 * style of tree traversal. There is a convention among <b>polyglot</b> visitors
 * which states that traversals will <i>lazily reconstruct</i> the tree. That
 * is, the AST is functionally "modified" by creating new nodes on each
 * traversal, but only when necessary-- only when nodes (or their children) are
 * actually changed. Up to three separate calls into the visitor may be invoked
 * for each AST node. {@code override} allows the visitor to redefine the
 * entire traversal for a particular subtree. {@code enter} notifies the
 * visitor that traversal of a particular subtree has begun.
 * {@code leave} informs the visitor that traversal is finishing a
 * particular subtree.
 *
 * @see polyglot.ast.Node#visit
 * @see polyglot.ast.Node
 */
public abstract class NodeVisitor implements Copy<NodeVisitor> {
    /** The language this NodeVisitor operates on. */
    private final Lang lang;

    @Deprecated
    protected NodeVisitor() {
        this(JLangToJLDel.instance);
    }

    protected NodeVisitor(Lang lang) {
        this.lang = lang;
    }

    public Lang lang() {
        return lang;
    }

    /**
     * Given a tree rooted at {@code n}, the visitor has the option of
     * overriding all traversal of the children of {@code n}. If no
     * changes were made to {@code n} and the visitor wishes to prevent
     * further traversal of the tree, then it should return {@code n}. If
     * changes were made to the subtree, then the visitor should return a
     * <i>copy</i> of {@code n} with appropriate changes.  Finally, if the
     * visitor does not wish to override traversal of the subtree rooted at
     * {@code n}, then it should return {@code null}.
     * <p>
     * The default implementation of this method is to call
     * {@link #override(Node) override(n)}, as most subclasses do not need to know
     * the parent of the node {@code n}.
     *
     * @param parent The parent of {@code n},
     *    {@code null} if {@code n} has no parent.
     * @param n The root of the subtree to be traversed.
     * @return A node if normal traversal is to stop, {@code null} if it
     * is to continue.
     */
    public Node override(Node parent, Node n) {
        return override(n);
    }

    /**
     * Given a tree rooted at {@code n}, the visitor has the option of
     * overriding all traversal of the children of {@code n}. If no
     * changes were made to {@code n} and the visitor wishes to prevent
     * further traversal of the tree, then it should return {@code n}. If
     * changes were made to the subtree, then the visitor should return a
     * <i>copy</i> of {@code n} with appropriate changes.  Finally, if the
     * visitor does not wish to override traversal of the subtree rooted at
     * {@code n}, then it should return {@code null}.
     * <p>
     * This method is typically called by the method
     * {@link #override(Node, Node) override(parent, n)}. If a subclass overrides the
     * method {@link #override(Node, Node) override(parent, n)} then this method
     * may not be called.
     *
     * @param n The root of the subtree to be traversed.
     * @return A node if normal traversal is to stop, {@code null} if it
     * is to continue.
     */
    public Node override(Node n) {
        return null;
    }

    /**
     * Begin normal traversal of a subtree rooted at {@code n}. This gives
     * the visitor the option of changing internal state or returning a new
     * visitor which will be used to visit the children of {@code n}.
     * <p>
     * The default implementation of this method is to call
     * {@link #enter(Node) enter(n)}, as most subclasses do not need to know
     * the parent of the node {@code n}.
     *
     * @param parent The parent of {@code n}, {@code null} if {@code n} has no parent.
     * @param n The root of the subtree to be traversed.
     * @return The {@code NodeVisitor} which should be used to visit the
     * children of {@code n}.
     */
    public NodeVisitor enter(Node parent, Node n) {
        return enter(n);
    }

    /**
     * Begin normal traversal of a subtree rooted at {@code n}. This gives
     * the visitor the option of changing internal state or returning a new
     * visitor which will be used to visit the children of {@code n}.
     * <p>
     * This method is typically called by the method
     * {@link #enter(Node, Node) enter(parent, n)}. If a subclass overrides the
     * method {@link #enter(Node, Node) enter(parent, n)} then this method
     * may not be called.
     *
     * @param n The root of the subtree to be traversed.
     * @return The {@code NodeVisitor} which should be used to visit the
     * children of {@code n}.
     */
    public NodeVisitor enter(Node n) {
        return this;
    }

    /**
     * This method is called after all of the children of {@code n}
     * have been visited. In this case, these children were visited by the
     * visitor {@code v}. This is the last chance for the visitor to
     * modify the tree rooted at {@code n}. This method will be called
     * exactly the same number of times as {@code entry} is called.
     * That is, for each node that is not overridden, {@code enter} and
     * {@code leave} are each called exactly once.
     * <p>
     * Note that if {@code old == n} then the visitor should make a copy
     * of {@code n} before modifying it. It should then return the
     * modified copy.
     * <p>
     * The default implementation of this method is to call
     * {@link #leave(Node, Node, NodeVisitor) leave(old, n, v)},
     * as most subclasses do not need to know the parent of the
     * node {@code n}.
     *
     * @param parent The parent of {@code old},
     *    {@code null} if {@code old} has no parent.
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The {@code NodeVisitor} object used to visit the children.
     * @return The final result of the traversal of the tree rooted at
     * {@code n}.
     */
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        return leave(old, n, v);
    }

    /**
     * This method is called after all of the children of {@code n}
     * have been visited. In this case, these children were visited by the
     * visitor {@code v}. This is the last chance for the visitor to
     * modify the tree rooted at {@code n}. This method will be called
     * exactly the same number of times as {@code entry} is called.
     * That is, for each node that is not overridden, {@code enter} and
     * {@code leave} are each called exactly once.
     * <p>
     * Note that if {@code old == n} then the visitor should make a copy
     * of {@code n} before modifying it. It should then return the
     * modified copy.
     * <p>
     * This method is typically called by the method
     * {@link #leave(Node, Node, Node, NodeVisitor) leave(parent, old, n v)}.
     * If a subclass overrides the method
     * {@link #leave(Node, Node, Node, NodeVisitor) leave(parent, old, n v)}
     * then this method may not be called.
     *
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The {@code NodeVisitor} object used to visit the children.
     * @return The final result of the traversal of the tree rooted at
     * {@code n}.
     */
    public Node leave(Node old, Node n, NodeVisitor v) {
        return n;
    }

    /**
     * The begin method is called before the entire tree is visited.
     * This method allows the visitor to perform any initialization
     * that cannot be done when the visitor is created.
     * If {@code null} is returned, the ast is not traversed.
     *
     * @return the {@code NodeVisitor} to traverse the ast with. If
     *     {@code null} is returned, the ast is not traversed.
     */
    public NodeVisitor begin() {
        return this;
    }

    /**
     * The finish method is called after the entire tree has been visited.
     * This method allows the visitor to perform any last minute cleanup,
     * including flushing buffers and I/O connections.
     */
    public void finish() {
    }

    public void finish(Node ast) {
        this.finish();
    }

    @Override
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }

    /**
     * Visit the edge between the parent node {@code parent}, and child
     * node {@code child}. This method recursively visits  the subtree rooted
     * at {@code child}.
     *
     * @param parent the parent node of {@code child}, {@code null} if
     *         {@code child} was visited by calling
     *         {@link polyglot.ast.Node#visit(NodeVisitor) Node.visit(NodeVisitor)} instead
     *         of {@link polyglot.ast.Node#visitChild(Node, NodeVisitor)
     *         polyglot.ast.Node.visitChild(Node, NodeVisitor)}.
     * @param child the child node of {@code parent} to be visited.
     * @return the (possibly new) version of {@code child} after the
     *       subtree rooted at {@code child} has been recursively visited.
     */
    public <N extends Node> N visitEdge(Node parent, N child) {
        try {
            @SuppressWarnings("unchecked")
            N n = (N) override(parent, child);

            if (n == null) {
                return visitEdgeNoOverride(parent, child);
            }

            return n;
        }
        catch (InternalCompilerError e) {
            if (e.position() == null && child != null)
                e.setPosition(child.position());
            throw e;
        }
    }

    /**
     * Visit the edge between the parent node {@code parent}, and child
     * node {@code child}, without invoking {@code override} for
     * the child.  This method recursively visits the subtree rooted at
     * {@code child}.
     *
     * @param parent
     * @param child
     * @return the (possibly new) version of {@code child} after the
     *       subtree rooted at {@code child} has been recursively visited.
     */
    public <N extends Node> N visitEdgeNoOverride(Node parent, N child) {
        if (child == null) {
            return null;
        }

        NodeVisitor v_ = enter(parent, child);

        if (v_ == null) {
            throw new InternalCompilerError("NodeVisitor.enter() returned null.");
        }

        @SuppressWarnings("unchecked")
        N n = (N) lang().visitChildren(child, v_);

        if (n == null) {
            throw new InternalCompilerError("Node.visitChildren() returned null.");
        }

        try {
            @SuppressWarnings("unchecked")
            N result = (N) this.leave(parent, child, n, v_);
            return result;
        }
        catch (InternalCompilerError e) {
            if (e.position() == null) e.setPosition(n.position());
            throw e;
        }
    }

    @Override
    public NodeVisitor copy() {
        try {
            return (NodeVisitor) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }
}
