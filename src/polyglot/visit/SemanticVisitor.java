package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;
import java.util.*;

/**
 * A visitor which maintains a context.  This is the base class of the
 * disambiguation and type checking visitors.
 */
public abstract class SemanticVisitor extends BaseVisitor
{
    protected Context context;

    public SemanticVisitor(Job job) {
        super(job);
	context = typeSystem().createContext(importTable());
    }

    public Context context() {
	return context;
    }

    protected Node overrideCall(Node n) throws SemanticException {
	return null;
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
	return leaveCall(n);
    }

    protected Node leaveCall(Node n) throws SemanticException {
	return n;
    }

    protected void enterScope(Node n) {
	n.enterScope(context);
    }

    protected void leaveScope(Node n) {
	n.leaveScope(context);
    }

    /**
     * Class used to implement error propagation.  We maintain a linked list
     * of nodes where exceptions are caught error messages output, before
     * continuing with the visitor pass.  If a node does not catch an exception
     * thrown by one of its children (or by itself), its siblings will not be
     * visited.
     */
    protected static class Catcher {
	Node node;
	Catcher next;

	Catcher(Node node, Catcher next) {
	    this.node = node;
	    this.next = next;
	}
    }

    /** Exception used to abort a visitor pass. */
    protected static class Abort extends RuntimeException { }

    protected Catcher catcher = null;

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
	/*
	return n instanceof Stmt
	    || n instanceof ClassMember
	    || n instanceof ClassDecl
	    || n instanceof SourceFile;
	    */
	return true;
    }

    /**
     * Create a new exception catcher for the node.  If a catcher is already
     * created for this node or we should not catch exceptions at this node,
     * return false, otherwise return true.  Returning true will indicate to
     * the <code>override()<code> method that exceptions should be caught when
     * visiting the node.
     */
    protected boolean newCatcher(Node n) {
	if (catcher != null && catcher.node == n) {
	    return false;
	}

	if (catchErrors(n) || catcher == null) {
	    catcher = new Catcher(n, catcher);
	    return true;
	}

	return false;
    }

    public Node override(Node n) {
	Context.Mark mark = context.mark();

	if (newCatcher(n)) {
	    try {
		// Visit the node again, but newCatcher will fail next time
		// so we won't recurse forever.
		n = n.visit(this);
		return n;
	    }
	    catch (Abort a) {
		return n;
	    }
	    finally {
		context.popToMark(mark);
		catcher = catcher.next;
	    }
	}

        try {
	    Node m = overrideCall(n);

	    // Ensure that we are at the same scope level as before we
	    // visited this node.
	    context.assertMark(mark);

	    if (m == null) {
	        return null;
	    }

	    return m.ext().reconstructTypes(nodeFactory(),
		                                 typeSystem(), context);
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

	    throw new Abort();
	}
    }

    public NodeVisitor enter(Node n) {
	enterScope(n);
	return this;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	try {
	    Node m = leaveCall(old, n, v);

	    m = m.ext().reconstructTypes(nodeFactory(),
					      typeSystem(), context);

	    leaveScope(m);

	    return m;
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

	    leaveScope(n);

	    throw new Abort();
	    // return n;
	}
    }
}
