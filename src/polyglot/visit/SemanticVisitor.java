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
    protected int depth;
    protected int errorDepth;

    public SemanticVisitor(Job job) {
        super(job);
	context = typeSystem().createContext(importTable());

        // start at depth 1 do that depth > errorDepth
        depth = 1;
        errorDepth = 0;
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

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
	return n instanceof Stmt
	    || n instanceof ClassMember
	    || n instanceof ClassDecl
	    || n instanceof SourceFile;
    }

    public Node override(Node n) {
	Context.Mark mark = context.mark();

        int oldDepth = depth++;

        try {
            Node m = overrideCall(n);

	    // Ensure that we are at the same scope level as before we
	    // visited this node.
	    context.assertMark(mark);

            if (errorDepth >= depth) {
                if (! catchErrors(n)) {
                    errorDepth = oldDepth;
                }
                else {
                    errorDepth = 0;
                }

                m = n;
            }

            depth = oldDepth;

            if (m == null) {
                return m;
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

            context.popToMark(mark);

            if (! catchErrors(n)) {
                errorDepth = oldDepth;
            }
            else {
                errorDepth = 0;
            }

            depth = oldDepth;

            return n;
	}
    }

    public NodeVisitor enter(Node n) {
        depth++;
	enterScope(n);
        return this;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
        Context.Mark mark = context.mark();

        int oldDepth = depth - 1;

	try {
            Node m;

            if (errorDepth >= depth) {
                if (! catchErrors(n)) {
                    errorDepth = oldDepth;
                }
                else {
                    errorDepth = 0;
                }

                m = n;
            }
            else {
                m = leaveCall(old, n, v);
            }

	    leaveScope(m);

            depth = oldDepth;

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

            context.popToMark(mark);

            if (! catchErrors(n)) {
                errorDepth = oldDepth;
            }
            else {
                errorDepth = 0;
            }

            depth = oldDepth;

            return n;
	}
    }
}
