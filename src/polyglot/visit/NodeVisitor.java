package polyglot.visit;

import polyglot.ast.Node;
import polyglot.util.*;
import java.util.*;

/**
 * The <code>NodeVisitor</code> represents an implementation of the "Visitor"
 * style of tree traversal. There is a convention among <b>polyglot</b> visitors
 * which states that traversals will <i>lazily reconstruct</i> the tree. That
 * is, the AST is functionally "modified" by creating new nodes on each
 * traversal, but only when necessary-- only when nodes (or their children) are
 * actually changed. Up to three seperate calls into the visitor may be invoked
 * for each AST node. <code>override</code> allows the visitor to redefine the
 * entire traversal for a particular subtree. <code>enter</code> notifies the
 * visitor that traversal of a particular subtree has begun.
 * <code>leave</code> informs the visitor that traversal is finishing a
 * particular subtree.
 *
 * @see polyglot.ast.Node#visit
 * @see polyglot.ast.Node
 */
public abstract class NodeVisitor
{
    /**
     * Given a tree rooted at <code>n</code>, the visitor has the option of
     * overriding all traversal of the children of <code>n</code>. If no
     * changes were made to <code>n</code> and the visitor wishes to prevent
     * further traversal of the tree, then it should return <code>n</code>. If
     * changes were made to the subtree, then the visitor should return a
     * <i>copy</i> of <code>n</code> with appropriate changes.  Finally, if the
     * visitor does not wish to override traversal of the subtree rooted at
     * <code>n</code>, then it should return <code>null</code>.
     *
     * @param n The root of the subtree to be traversed.
     * @return A node if normal traversal is to stop, <code>null</code> if it
     * is to continue.
     */
    public Node override(Node parent, Node n) {
        return override(n);
    }

    public Node override(Node n) {
	return null;
    }

    /**
     * Begin normal traversal of a subtree rooted at <code>n</code>. This gives
     * the visitor the option of changing internal state or returning a new
     * visitor which will be used to visit the children of <code>n</code>.
     *
     * @param n The root of the subtree to be traversed.
     * @return The <code>NodeVisitor</code> which should be used to visit the
     * children of <code>n</code>.
     */
    public NodeVisitor enter(Node parent, Node n) {
        return enter(n);
    }

    public NodeVisitor enter(Node n) {
        return this;
    }

    /**
     * This method is called after all of the children of <code>n</code>
     * have been visited. In this case, these children were visited by the
     * visitor <code>v</code>. This is the last chance for the visitor to
     * modify the tree rooted at <code>n</code>. This method will be called
     * exactly the same number of times as <code>entry</code> is called.
     * That is, for each node that is not overriden, <code>enter</code> and
     * <code>leave</code> are each called exactly once.
     * <p>
     * Note that if <code>old == n</code> then the vistior should make a copy
     * of <code>n</code> before modifying it. It should then return the
     * modified copy.
     *
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The <code>NodeVisitor</code> object used to visit the children.
     * @return The final result of the traversal of the tree rooted at
     * <code>n</code>.
     */
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        return leave(old, n, v);
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
        return n;
    }

    /**
     * The begin method is called before the entire tree is visited.
     * This method allows the visitor to perform any initialization
     * that cannot be done when the visitor is created.
     * If <code>null</code> is returned, the ast is not traversed.
     */
    public NodeVisitor begin() {
        return this;
    }

    /**
     * The finish method is called after the entire tree has been visited.
     * This method allows the visitor to perform any last minute cleanup,
     * including flushing buffers and I/O connections.
     */
    public void finish() { }
    public void finish(Node ast) { this.finish(); }

    public String toString() {
        return getClass().getName();
    }

    public Node visitEdge(Node parent, Node child) {
	Node n = override(parent, child);

	if (n == null) {
	    NodeVisitor v_ = this.enter(parent, child);

	    if (v_ == null) {
		throw new InternalCompilerError(
		    "NodeVisitor.enter() returned null.");
	    }

	    n = child.visitChildren(v_);

	    if (n == null) {
		throw new InternalCompilerError(
		    "Node_c.visitChildren() returned null.");
	    }

	    n = this.leave(parent, child, n, v_);

	    if (n == null) {
		throw new InternalCompilerError(
		    "NodeVisitor.leave() returned null.");
	    }
	}

	return n;
    }
}
