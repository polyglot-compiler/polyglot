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

    public If_c(Del ext, Position pos, Expr cond, Stmt consequent, Stmt alternative) {
	super(ext, pos);
	this.cond = cond;
	this.consequent = consequent;
	this.alternative = alternative;
    }

    /** Get the conditional of the statement. */
    public Expr cond() {
	return this.cond;
    }

    /** Set the conditional of the statement. */
    public If cond(Expr cond) {
	If_c n = (If_c) copy();
	n.cond = cond;
	return n;
    }

    /** Get the consequent of the statement. */
    public Stmt consequent() {
	return this.consequent;
    }

    /** Set the consequent of the statement. */
    public If consequent(Stmt consequent) {
	If_c n = (If_c) copy();
	n.consequent = consequent;
	return n;
    }

    /** Get the alternative of the statement. */
    public Stmt alternative() {
	return this.alternative;
    }

    /** Set the alternative of the statement. */
    public If alternative(Stmt alternative) {
	If_c n = (If_c) copy();
	n.alternative = alternative;
	return n;
    }

    /** Reconstruct the statement. */
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

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) visitChild(this.cond, v);
	Stmt consequent = (Stmt) visitChild(this.consequent, v);
	Stmt alternative = (Stmt) visitChild(this.alternative, v);
	return reconstruct(cond, consequent, alternative);
    }

    /** Type check the statement. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	if (! cond.type().isSame(ts.Boolean())) {
	    throw new SemanticException(
		"Condition of if statement must have boolean type.",
		cond.position());
	}

	return this;
    }

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == cond) {
            return child.expectedType(ts.Boolean());
        }

        return child;
    }

    public String toString() {
	return "if (" + cond + ") " + consequent +
	    (alternative != null ? " else " + alternative : "");
    }

    /** Write the statement to an output file. */
    public void translate(CodeWriter w, Translator tr) {    
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
