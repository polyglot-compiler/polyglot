package jltools.visit;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import java.util.*;

/** Visitor which checks if exceptions are caught or declared properly. */
public class ExceptionChecker extends NodeVisitor
{
    protected TypeSystem ts;
    protected ErrorQueue eq;

    /**
     * This is a stack of sets of exceptions thrown for each lexical nesting
     * level.
     */
    protected Stack stack;

    public ExceptionChecker(TypeSystem ts, ErrorQueue eq) {
        this.ts = ts;
        this.eq = eq;
        this.stack = new Stack();
    }

    public boolean begin() {
        pushScope();
        return true;
    }

    public void finish() {
        popScope();
        if (! stack.isEmpty()) {
            throw new InternalCompilerError("Throws stack not empty.");
        }
    }

    public TypeSystem typeSystem() {
	return ts;
    }

    public Node override(Node n) {
        pushScope();

	try {
	    Node m = n.ext().exceptionCheckOverride(this);
            SubtypeSet t = popScope();
	    throwsSet().addAll(t);
	    return m;
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);

            popScope();

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
    public Node enter(Node n) {
        pushScope();
        return n;
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
	// Merge results from the children and free the checker used for the
	// children.
        SubtypeSet t = popScope();
        throwsSet().addAll(t);

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
	throwsSet().add(t) ;
    }
    
    /**
     * Method to allow the throws clause and method body to inspect and
     * modify the throwsSet.
     */
    public SubtypeSet throwsSet() {
        return (SubtypeSet) stack.peek();
    }

    /**
     * Push a new, empty, throws set.
     */
    public void pushScope() {
        stack.push(new SubtypeSet());
    }

    /**
     * Push the current throws set.
     */
    public SubtypeSet popScope() {
        return (SubtypeSet) stack.pop();
    }
}
