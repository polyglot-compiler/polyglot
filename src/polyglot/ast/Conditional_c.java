package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.visit.*;
import jltools.types.*;
import jltools.util.*;

/**
 * A <code>Conditional</code> represents a Java ternary expression as
 * an immutable triple of expressions.
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

    public Precedence precedence() { 
	return Precedence.CONDITIONAL;
    }

    public Expr cond() {
	return this.cond;
    }

    public Conditional cond(Expr cond) {
	Conditional_c n = (Conditional_c) copy();
	n.cond = cond;
	return n;
    }

    public Expr consequent() {
	return this.consequent;
    }

    public Conditional consequent(Expr consequent) {
	Conditional_c n = (Conditional_c) copy();
	n.consequent = consequent;
	return n;
    }

    public Expr alternative() {
	return this.alternative;
    }

    public Conditional alternative(Expr alternative) {
	Conditional_c n = (Conditional_c) copy();
	n.alternative = alternative;
	return n;
    }

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

    public Node visitChildren(NodeVisitor v) {
	Expr cond = (Expr) this.cond.visit(v);
	Expr consequent = (Expr) this.consequent.visit(v);
	Expr alternative = (Expr) this.alternative.visit(v);
	return reconstruct(cond, consequent, alternative);
    }

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

    public void translate_(CodeWriter w, Translator tr)
    {
	translateSubexpr(cond, w, tr);
	w.write(" ? ");
	translateSubexpr(consequent, w, tr);
	w.write(" : ");
	translateSubexpr(alternative, w, tr);
    }
}
