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
public class ContextVisitor extends ErrorHandlingVisitor
{
    protected ContextVisitor outer;
    protected Context context;

    public ContextVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        this.outer = null;
        this.context = null;
    }

    public NodeVisitor begin() {
        context = job.context();

        if (context == null) {
            context = ts.createContext();
        }

        outer = null;

        return super.begin();
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

    protected Context updateScope(Node n) {
        return n.updateScope(context);
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

        return v.superEnter(parent, n);
    }

    public NodeVisitor superEnter(Node parent, Node n) {
        return super.enter(parent, n);
    }
}
