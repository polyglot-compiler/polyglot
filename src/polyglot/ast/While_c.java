package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An immutable representation of a Java language <code>while</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */ 
public class While_c extends Stmt_c implements While
{
    protected Expr cond;
    protected Stmt body;

    public While_c(Ext ext, Position pos, Expr cond, Stmt body) {
	super(ext, pos);
	this.cond = cond;
	this.body = body;
    }

    public Expr cond() {
	return this.cond;
    }

    public While cond(Expr cond) {
	While_c n = (While_c) copy();
	n.cond = cond;
	return n;
    }

    public Stmt body() {
	return this.body;
    }

    public While body(Stmt body) {
	While_c n = (While_c) copy();
	n.body = body;
	return n;
    }

    protected While_c reconstruct(Expr cond, Stmt body) {
	if (cond != this.cond || body != this.body) {
	    While_c n = (While_c) copy();
	    n.cond = cond;
	    n.body = body;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) this.cond.visit(v);
	Stmt body = (Stmt) this.body.visit(v);
	return reconstruct(cond, body);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	
	if (! cond.type().isSame(ts.Boolean())) {
	    throw new SemanticException(
		"Condition of while statement must have boolean type.",
		cond.position());
	}
	
	return this;
    }

    public String toString() {
	return "while (" + cond + ") ...";
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("while (");
	translateBlock(cond, w, tr);
	w.write(")");
	translateSubstmt(body, w, tr);

	// FIXME: we used to construct with null body for while (c) ;
	// check the creators of While nodes.
    }
}
