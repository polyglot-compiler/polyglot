package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.visit.*;
import jltools.types.*;
import jltools.util.*;

/**
 * A <code>Conditional</code> is a representation of a Java ternary
 * expression <code>(cond ? consequent : alternative)</code>.
 */
public class Conditional_c extends Expr_c implements Conditional
{
    protected Expr cond;
    protected Expr consequent;
    protected Expr alternative;

    public Conditional_c(Ext ext, Position pos, Expr cond, Expr consequent, Expr alternative) {
	super(ext, pos);
	this.cond = cond;
	this.consequent = consequent;
	this.alternative = alternative;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() { 
	return Precedence.CONDITIONAL;
    }

    /** Get the conditional of the expression. */
    public Expr cond() {
	return this.cond;
    }

    /** Set the conditional of the expression. */
    public Conditional cond(Expr cond) {
	Conditional_c n = (Conditional_c) copy();
	n.cond = cond;
	return n;
    }

    /** Get the consequent of the expression. */
    public Expr consequent() {
	return this.consequent;
    }

    /** Set the consequent of the expression. */
    public Conditional consequent(Expr consequent) {
	Conditional_c n = (Conditional_c) copy();
	n.consequent = consequent;
	return n;
    }

    /** Get the alternative of the expression. */
    public Expr alternative() {
	return this.alternative;
    }

    /** Set the alternative of the expression. */
    public Conditional alternative(Expr alternative) {
	Conditional_c n = (Conditional_c) copy();
	n.alternative = alternative;
	return n;
    }

    /** Reconstruct the expression. */
    protected Conditional_c reconstruct(Expr cond, Expr consequent, Expr alternative) {
	if (cond != this.cond || consequent != this.consequent || alternative != this.alternative) {
	    Conditional_c n = (Conditional_c) copy();
	    n.cond = cond;
	    n.consequent = consequent;
	    n.alternative = alternative;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) this.cond.visit(v);
	Expr consequent = (Expr) this.consequent.visit(v);
	Expr alternative = (Expr) this.alternative.visit(v);
	return reconstruct(cond, consequent, alternative);
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	if (! cond.type().isSame(ts.Boolean())) {
	     throw new SemanticException(
		 "Condition of ternary expression must be of type boolean.",
		 cond.position());
	}

	// FIXME: The rule is more complicated than this.
	return type(ts.leastCommonAncestor(consequent.type(),
	    				   alternative.type()));
    }

    public String toString() {
	return cond + " ? " + consequent + " : " + alternative;
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr)
    {
	translateSubexpr(cond, w, tr);
	w.write(" ? ");
	translateSubexpr(consequent, w, tr);
	w.write(" : ");
	translateSubexpr(alternative, w, tr);
    }
}
