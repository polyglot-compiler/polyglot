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
    protected Context.Mark top;
    protected int depth;
    protected BitSet errors;

    public SemanticVisitor(Job job) {
        super(job);
    }

    public boolean begin() {
	context = job.context();
        top = context.mark();
        depth = 0;
        errors = new BitSet();
        return true;
    }

    public Context context() {
	return context;
    }

    protected Node overrideCall(Node n) throws SemanticException {
	return null;
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v)
        throws SemanticException {

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

    public void finish() {
        context.assertMark(top);
    }

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
	return n instanceof Stmt
	    || n instanceof ClassMember
	    || n instanceof ClassDecl
	    || n instanceof SourceFile;
    }

    public Node override(Node n) {
	Context.Mark mark = context.mark();

        Types.report(5, "enter override(" + n + "): " + depth + "->" + (depth+1));

        int oldDepth = depth++;
        errors.clear(depth);

        try {
            Node m = overrideCall(n);

	    // Ensure that we are at the same scope level as before we
	    // visited this node.
	    context.assertMark(mark);

            Types.report(5, "leave override(" + n + "): " + depth + "->" + oldDepth);
            if (m != null) {
                Types.report(5, "leave override(" + n + "): traversal stopped");
            }

            depth = oldDepth;
            return m;
        }
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

	    context.popToMark(mark);

            Types.report(5, "override(" + n + "): error at " + depth);

            errors.set(depth);

            Types.report(5, "leave override(" + n + "): " + depth + "->" + oldDepth);
            Types.report(5, "leave override(" + n + "): traversal stopped");

            depth = oldDepth;

            return n;
	}
    }

    public NodeVisitor enter(Node n) {
        Types.report(5, "enter(" + n + "): " + depth + "->" + (depth+1));
        depth++;
        errors.clear(depth);
	enterScope(n);
        return this;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
        try {
            Node m;

            if (errors.get(depth)) {
                Types.report(5, "leave(" + n + "): error below at " + (depth+1));

                // There was an error below us.
                if (! catchErrors(n)) {
                    // Propagate error up one level
                    errors.set(depth-1);
                    Types.report(5, "leave(" + n + "): error propagated to depth " + depth);
                }
                else {
                    Types.report(5, "leave(" + n + "): error not propagated");
                }

                errors.clear(depth);

                m = n;
            }
            else {
                Types.report(5, "leave(" + n + "): calling leaveCall");
                m = leaveCall(old, n, v);
            }

            // don't visit the node
            Types.report(5, "leave(" + m + "): " + depth + "->" + (depth-1));
            leaveScope(m);
            depth--;
            return m;
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

            Types.report(5, "leave(" + n + "): error at " + depth);

            // There was an error below us.
            if (! catchErrors(n)) {
                // Propagate error up one level
                errors.set(depth-1);
            }

            // don't visit the node
            Types.report(5, "leave(" + n + "): " + depth + "->" + (depth-1));
            leaveScope(n);
            depth--;
            return n;
        }
    }
}
