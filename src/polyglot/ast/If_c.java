package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An immutable representation of a Java language <code>if</code> statement.
 * Contains an expression whose value is tested, a ``then'' statement 
 * (consequent), and optionally an ``else'' statement (alternate).
 */
public class If_c extends Stmt_c implements If
{
    protected Expr cond;
    protected Stmt consequent;
    protected Stmt alternative;

    public If_c(Ext ext, Position pos, Expr cond, Stmt consequent, Stmt alternative) {
	super(ext, pos);
	this.cond = cond;
	this.consequent = consequent;
	this.alternative = alternative;
    }

    public Expr cond() {
	return this.cond;
    }

    public If cond(Expr cond) {
	If_c n = (If_c) copy();
	n.cond = cond;
	return n;
    }

    public Stmt consequent() {
	return this.consequent;
    }

    public If consequent(Stmt consequent) {
	If_c n = (If_c) copy();
	n.consequent = consequent;
	return n;
    }

    public Stmt alternative() {
	return this.alternative;
    }

    public If alternative(Stmt alternative) {
	If_c n = (If_c) copy();
	n.alternative = alternative;
	return n;
    }

    protected If_c reconstruct(Expr cond, Stmt consequent, Stmt alternative) {
	if (cond != this.cond || consequent != this.consequent || alternative != this.alternative) {
	    If_c n = (If_c) copy();
	    n.cond = cond;
	    n.consequent = consequent;
	    n.alternative = alternative;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) this.cond.visit(v);
	Stmt consequent = (Stmt) this.consequent.visit(v);
	Stmt alternative = null;

	if (this.alternative != null) {
	    alternative = (Stmt) this.alternative.visit(v);
	}

	return reconstruct(cond, consequent, alternative);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	if (! cond.type().isSame(ts.Boolean())) {
	    throw new SemanticException(
		"Condition of if statement must have boolean type.",
		cond.position());
	}

	return this;
    }

    public String toString() {
	return "if (" + cond + ") " + consequent +
	    (alternative != null ? " else " + alternative : "");
    }

    public void translate_(CodeWriter w, Translator tr) {    
	w.write("if (");
	translateBlock(cond, w, tr);
	w.write(")");
       
	translateSubstmt(consequent, w, tr);

	if (alternative != null) {
	    if (consequent instanceof Block) {
		// allow the "} else {" formatting
		w.write(" ");
	    }
	    else {
		w.allowBreak(0, " ");
	    }

	    w.write ("else");
	    translateSubstmt(alternative, w, tr);
	}
    }
}
