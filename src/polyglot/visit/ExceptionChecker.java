package jltools.visit;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import java.util.*;

/** Visitor which checks if exceptions are caught or declared properly. */
public class ExceptionChecker extends NodeVisitor
{
    protected SubtypeSet thrown;
    protected TypeSystem ts;
    protected ErrorQueue eq;
    protected LinkedList freelist;

    public ExceptionChecker(TypeSystem ts, ErrorQueue eq) {
        this(ts, eq, new LinkedList());
    }

    protected ExceptionChecker(TypeSystem ts, ErrorQueue eq,
	                       LinkedList freelist) {
	this.ts = ts;
	this.eq = eq; 
	this.freelist = freelist;
	this.thrown = new SubtypeSet();
    }

    public TypeSystem typeSystem() {
	return ts;
    }

    public Node override(Node n) {
	ExceptionChecker ec = alloc();

	try {
	    Node m = n.ext().exceptionCheckOverride(ec);
	    this.throwsSet().addAll(ec.throwsSet());
	    release(ec);
	    return m;
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);

	    release(ec);

	    return n;
	}
    }
    
    /**
     * This method is called when we are to perform a "normal" traversal of 
     * a subtree rooted at <code>n</code>.   At every node, we will push a 
     * stack frame.  Each child node will add the exceptions that it throws
     * to this stack frame. For most nodes ( excdeption for the try / catch)
     * will just aggregate the stack frames.
     *
     * @param n The root of the subtree to be traversed.
     * @return The <code>NodeVisitor</code> which should be used to visit the 
     *  children of <code>n</code>.
     *
     */
    public NodeVisitor enter(Node n) {
	return alloc(); 
    }

    /**
     * Here, we pop the stack frame that we pushed in enter and agregate the 
     * exceptions.
     *
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The <code>NodeVisitor</code> object used to visit the children.
     * @return The final result of the traversal of the tree rooted at 
     *  <code>n</code>.
     */
    public Node leave(Node old, Node n, NodeVisitor v) {
	ExceptionChecker ec = (ExceptionChecker) v;

	// Merge results from the children and free the checker used for the
	// children.
	thrown.addAll(ec.throwsSet());
	release(ec);

	// gather exceptions from this node.
	try {
	    return n.ext().exceptionCheck(this);
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);

	    return n;
	}
    }

    /**
     * The ast nodes will use this callback to notify us that they throw an 
     * exception of type t. This should only be called by MethodExpr node, 
     * and throw node, since they are the only node which can generate
     * exceptions.  
     *
     * @param t The type of exception that the node throws.
     */
    public void throwsException(Type t) {
	thrown.add(t) ;
    }
    
    /**
     * Method to allow the throws clause and method body to inspect and
     * modify the throwsSet.
     */
    public SubtypeSet throwsSet() {
	return thrown;
    }

    public ExceptionChecker alloc() {
	if (freelist.isEmpty()) {
	    return new ExceptionChecker(ts, eq, freelist); 
	}
	else {
	    return (ExceptionChecker) freelist.removeLast();
	}
    }
    
    public void release(ExceptionChecker ec) {
	// reuse the ExceptionCheckers. saves an allocation.  
	ec.throwsSet().clear();
	freelist.addLast(ec);
    }
}
