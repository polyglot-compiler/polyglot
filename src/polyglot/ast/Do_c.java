package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A immutable representation of a Java language <code>do</code> statement. 
 * It contains a statement to be executed and an expression to be tested 
 * indicating whether to reexecute the statement.
 */ 
public class Do_c extends Stmt_c implements Do
{
    protected Stmt body;
    protected Expr cond;

    public Do_c(Ext ext, Position pos, Stmt body, Expr cond) {
	super(ext, pos);
	this.body = body;
	this.cond = cond;
    }

    /** Get the body of the statement. */
    public Stmt body() {
	return this.body;
    }

    /** Set the body of the statement. */
    public Do body(Stmt body) {
	Do_c n = (Do_c) copy();
	n.body = body;
	return n;
    }

    /** Get the conditional of the statement. */
    public Expr cond() {
	return this.cond;
    }

    /** Set the conditional of the statement. */
    public Do cond(Expr cond) {
	Do_c n = (Do_c) copy();
	n.cond = cond;
	return n;
    }

    /** Reconstruct the statement. */
    protected Do_c reconstruct(Stmt body, Expr cond) {
	if (body != this.body || cond != this.cond) {
	    Do_c n = (Do_c) copy();
	    n.body = body;
	    n.cond = cond;
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
	Stmt body = (Stmt) visitChild(this.body, v);
	Expr cond = (Expr) visitChild(this.cond, v);
	return reconstruct(body, cond);
    }

    /** Type check the statement. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (! cond.type().isSame(ts.Boolean())) {
	    throw new SemanticException(
		"Condition of do statement must have boolean type.",
		cond.position());
	}

	return this;
    }

    public Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == cond) {
            return child.expectedType(ts.Boolean());
        }

        return child;
    }

    public String toString() {
	return "do { ... } while (" + cond + ")";
    }

    /** Write the statement to an output file. */
    public void translate_(CodeWriter w, Translator tr)
    {
	w.write("do ");
	translateSubstmt(body, w, tr);
	w.write("while(");
	translateBlock(cond, w, tr);
	w.write("); ");
    }

}
