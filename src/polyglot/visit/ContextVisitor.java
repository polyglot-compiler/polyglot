package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import java.util.*;

/**
 * A visitor which maintains a context throughout the visitor's pass.  This is 
 * the base class of the disambiguation and type checking visitors.
 */
public class ContextVisitor extends ErrorHandlingVisitor
{
    protected ContextVisitor outer;

    /** The current context of this visitor. */
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

    /** Returns the context for this visitor.
     *
     *  @return Returns the context that is currently in use by this visitor.
     *  @see polyglot.types.Context
     */
    public Context context() {
        return context;
    }

    /** Returns a new ContextVisitor that is a copy of the current visitor,
     *  except with an updated context.
     *
     *  @param c The new context that is to be used.
     *  @return Returns a copy of this visitor with the new context 
     *  <code>c</code>.
     */
    public ContextVisitor context(Context c) {
        ContextVisitor v = (ContextVisitor) this.copy();
        v.context = c;
        return v;
    }

    /** Returns a new context based on the current context and the
     *  Node that is being entered.
     *
     *  @return The new context after entering Node <code>n</code>.
     */
    protected Context enterScope(Node n) {
	return n.enterScope(context);
    }

    /** Returns a new context based on the current context and the
     *  Node that is updating the scope of the context.
     *
     *  @return The new context after <code>n</code> updates the context.
     */

     //FIXME does this make sense?
    protected Context updateScope(Node n) {
        return n.updateScope(context);
    }

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
	return n instanceof Stmt
	    || n instanceof ClassMember
	    || n instanceof ClassDecl
	    || n instanceof SourceFile;
    }

    public NodeVisitor enter(Node parent, Node n) {
        if (Types.should_report(5))
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

    public Node leave(Node old, Node n, NodeVisitor v) {
        Node m = super.leave(old, n, v);

        // FIXME: hack to allow locals added to the context by enterScope to
        // propagate outward.  We need this until we have true "let"-style
        // local decls.  This, of course, makes this visitor imperative.
        this.context = this.updateScope(m);

        return m;
    }
}
