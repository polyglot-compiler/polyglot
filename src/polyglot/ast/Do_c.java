package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A immutable representation of a Java language <code>do</code> statement. 
 * Contains a statement to be executed and an expression to be tested 
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

    public Stmt body() {
	return this.body;
    }

    public Do body(Stmt body) {
	Do_c n = (Do_c) copy();
	n.body = body;
	return n;
    }

    public Expr cond() {
	return this.cond;
    }

    public Do cond(Expr cond) {
	Do_c n = (Do_c) copy();
	n.cond = cond;
	return n;
    }

    protected Do_c reconstruct(Stmt body, Expr cond) {
	if (body != this.body || cond != this.cond) {
	    Do_c n = (Do_c) copy();
	    n.body = body;
	    n.cond = cond;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Stmt body = (Stmt) this.body.visit(v);
	Expr cond = (Expr) this.cond.visit(v);
	return reconstruct(body, cond);
    }

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

    public String toString() {
	return "do { ... } while (" + cond + ")";
    }

    public void translate_(CodeWriter w, Translator tr)
    {
	w.write("do ");
	translateSubstmt(body, w, tr);
	w.write("while(");
	translateBlock(cond, w, tr);
	w.write("); ");
    }

}
