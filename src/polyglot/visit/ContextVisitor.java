package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import java.util.*;

/**
 * A visitor which maintains a context.  This is the base class of the
 * disambiguation and type checking visitors.
 */
public class ContextVisitor extends HaltingVisitor
{
    protected boolean error;
    protected Job job;
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected ContextVisitor outer;
    protected Context context;

    public ContextVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.outer = null;
        this.context = null;
        this.error = false;
    }

    public Job job() {
        return job;
    }

    public ErrorQueue errorQueue() {
        return job.compiler().errorQueue();
    }

    public NodeFactory nodeFactory() {
        return nf;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public NodeVisitor begin() {
        context = job.context();

        if (context == null) {
            context = ts.createContext();
        }

        outer = null;

        return this;
    }

    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        Types.report(1, "enter: " + parent + " -> " + n);
        return enterCall(n);
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
        return this;
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v)
        throws SemanticException {

	return leaveCall(n);
    }

    protected Node leaveCall(Node n) throws SemanticException {
	return n;
    }

    public Context context() {
        return context;
    }

    public ContextVisitor context(Context c) {
        ContextVisitor v = (ContextVisitor) this.copy();
        v.context = c;
        return v;
    }

    protected Context enterScope(Node n) {
	return n.enterScope(context);
    }

    public void finish() { }

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
	return n instanceof Stmt
	    || n instanceof ClassMember
	    || n instanceof ClassDecl
	    || n instanceof SourceFile;
    }

    public NodeVisitor enter(Node parent, Node n) {
        Types.report(5, "enter(" + n + ")");

        ContextVisitor v = this;

        Context c = this.enterScope(n);

        if (c != this.context) {
            v = (ContextVisitor) this.copy();
            v.context = c;
            v.outer = this;
            v.error = false;
        }

        try {
            v = (ContextVisitor) v.enterCall(parent, n);
        }
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

            v.error = true;
        }

        return v;
    }

    /*
    public NodeVisitor enter(Node parent, Node n) {
        Types.report(5, "enter(" + n + ")");

        ContextVisitor v = this;
        boolean error = false;

        try {
            v = (ContextVisitor) this.enterCall(parent, n);
        }
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

            error = true;
        }

        Context c = v.enterScope(n);

        if (true || c != v.context || error != v.error) {
            ContextVisitor v2 = (ContextVisitor) v.copy();
            v2.context = c;
            v2.outer = v;
            v2.error = error;
            return v2;
        }
        else {
            return v;
        }
    }
    */

    public Node leave(Node old, Node n, NodeVisitor v) {
        try {
            if (v instanceof ContextVisitor && ((ContextVisitor) v).error) {
                Types.report(5, "leave(" + n + "): error below");

                // There was an error below us.
                if (! catchErrors(n)) {
                    // Propagate error up one level
                    this.error = true;
                    Types.report(5, "leave(" + n + "): error propagated");
                }
                else {
                    Types.report(5, "leave(" + n + "): error not propagated");
                }

                return n;
            }

            Types.report(5, "leave(" + n + "): calling leaveCall");
            return leaveCall(old, n, v);
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

            // There was an error below us.
            if (! catchErrors(n)) {
                // Propagate error up one level
                this.error = true;
            }

            // don't visit the node
            return n;
        }
    }
}
