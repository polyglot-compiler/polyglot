package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public class For_c extends Stmt_c implements For
{
    protected List inits;
    protected Expr cond;
    protected List iters;
    protected Stmt body;

    public For_c(Ext ext, Position pos, List inits, Expr cond, List iters, Stmt body) {
	super(ext, pos);
	this.inits = TypedList.copyAndCheck(inits, ForInit.class, true);
	this.cond = cond;
	this.iters = TypedList.copyAndCheck(iters, ForUpdate.class, true);
	this.body = body;
    }

    /** List of initialization statements */
    public List inits() {
	return Collections.unmodifiableList(this.inits);
    }

    /** Set the inits of the statement. */
    public For inits(List inits) {
	For_c n = (For_c) copy();
	n.inits = TypedList.copyAndCheck(inits, ForInit.class, true);
	return n;
    }

    /** Loop condition */
    public Expr cond() {
	return this.cond;
    }

    /** Set the conditional of the statement. */
    public For cond(Expr cond) {
	For_c n = (For_c) copy();
	n.cond = cond;
	return n;
    }

    /** List of iterator expressions. */
    public List iters() {
	return Collections.unmodifiableList(this.iters);
    }

    /** Set the iterator expressions of the statement. */
    public For iters(List iters) {
	For_c n = (For_c) copy();
	n.iters = TypedList.copyAndCheck(iters, ForUpdate.class, true);
	return n;
    }

    /** Loop body */
    public Stmt body() {
	return this.body;
    }

    /** Set the body of the statement. */
    public For body(Stmt body) {
	For_c n = (For_c) copy();
	n.body = body;
	return n;
    }

    /** Reconstruct the statement. */
    protected For_c reconstruct(List inits, Expr cond, List iters, Stmt body) {
	if (! CollectionUtil.equals(inits, this.inits) || cond != this.cond || ! CollectionUtil.equals(iters, this.iters) || body != this.body) {
	    For_c n = (For_c) copy();
	    n.inits = TypedList.copyAndCheck(inits, ForInit.class, true);
	    n.cond = cond;
	    n.iters = TypedList.copyAndCheck(iters, ForUpdate.class, true);
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
	List inits = new ArrayList(this.inits.size());
	for (Iterator i = this.inits.iterator(); i.hasNext(); ) {
	    ForInit n = (ForInit) i.next();
	    n = (ForInit) n.visit(v);
	    inits.add(n);
	}

	Expr cond = null;

	if (this.cond != null) {
	    cond = (Expr) this.cond.visit(v);
	}

	List iters = new ArrayList(this.iters.size());
	for (Iterator i = this.iters.iterator(); i.hasNext(); ) {
	    ForUpdate n = (ForUpdate) i.next();
	    n = (ForUpdate) n.visit(v);
	    iters.add(n);
	}

	Stmt body = (Stmt) this.body.visit(v);

	return reconstruct(inits, cond, iters, body);
    }

    public void enterScope(Context c) {
	c.pushBlock();
    }

    public void leaveScope(Context c) {
	c.popBlock();
    }

    /** Type check the statement. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	if (cond != null &&
	    ! cond.type().isImplicitCastValid(ts.Boolean())) {
	    throw new SemanticException(
		"The condition of a for statement must have boolean type.",
		cond.position());
	}

	return this;
    }

    /** Write the statement to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        Context c = tr.context();

	w.write("for (");
	w.begin(0);

	enterScope(c);

	if (inits != null) {
	    for (Iterator i = inits.iterator(); i.hasNext(); ) {
		ForInit s = (ForInit) i.next();
	        translateForInit(s, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(2, " ");
		}
	    }
	}

	w.write(";"); 
	w.allowBreak(0);

	if (cond != null) {
	    translateBlock(cond, w, tr);
	}

	w.write (";");
	w.allowBreak(0);
	
	if (iters != null) {
	    for (Iterator i = iters.iterator(); i.hasNext();) {
		ForUpdate s = (ForUpdate) i.next();
		translateForUpdate(s, w, tr);
		
		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(2, " ");
		}
	    }
	}

	w.end();
	w.write(")");

	translateSubstmt(body, w, tr);

	leaveScope(c);
    }

    public String toString() {
	return "for (...) ...";
    }

    private void translateForInit(ForInit s, CodeWriter w, Translator tr) {
        tr.appendSemicolon(false);
        translateBlock(s, w, tr);
        tr.appendSemicolon(true);
    }

    private void translateForUpdate(ForUpdate s, CodeWriter w, Translator tr) {
        tr.appendSemicolon(false);
        translateBlock(s, w, tr);
        tr.appendSemicolon(true);
    }
}
