package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A <code>Case</code> is a representation of a Java <code>case</code>
 * statement.  It can only be contained in a <code>Switch</code>.
 */
public class Case_c extends Stmt_c implements Case
{
    protected Expr expr;
    protected long value;

    public Case_c(Ext ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    /** Returns true iff this is the default case. */
    public boolean isDefault() {
	return this.expr == null;
    }

    /**
     * Get the case label.  This must should a constant expression.
     * The case label is null for the <code>default</code> case.
     */
    public Expr expr() {
	return this.expr;
    }

    /** Set the case label.  This must should a constant expression, or null. */
    public Case expr(Expr expr) {
	Case_c n = (Case_c) copy();
	n.expr = expr;
	return n;
    }

    /**
     * Returns the value of the case label.  This value is only valid
     * after type-checking.
     */
    public long value() {
	return this.value;
    }

    /** Set the value of the case label. */
    protected Case value(long value) {
	Case_c n = (Case_c) copy();
	n.value = value;
	return n;
    }

    /** Reconstruct the statement. */
    protected Case_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Case_c n = (Case_c) copy();
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    /** Visit the children of the statement. */
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        return reconstruct(expr);
    }

    /** Type check the statement. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        if (expr == null) {
	    return this;
	}

	TypeSystem ts = tc.typeSystem();

	if (! expr.type().isImplicitCastValid(ts.Int())) {
	    throw new SemanticException(
		"Case label must be an byte, char, short, or int.",
		position());
	}

	long value;

	if (expr instanceof NumLit) {
	    value = ((NumLit) expr).longValue();
	}
	else if (expr instanceof Field) {
	    FieldInstance fi = ((Field) expr).fieldInstance();

	    if (fi == null) {
	        throw new InternalCompilerError(
		    "Undefined FieldInstance after type-checking.");
	    }

	    if (! fi.isConstant()) {
	        throw new SemanticException("Case label must be a constant.",
					    position());
	    }

	    value = ((Number) fi.constantValue()).longValue();
	}
	else if (expr instanceof Local) {
	    LocalInstance li = ((Local) expr).localInstance();

	    if (li == null) {
	        throw new InternalCompilerError(
		    "Undefined LocalInstance after type-checking.");
	    }

	    if (! li.isConstant()) {
	        throw new SemanticException("Case label must be a constant.",
					    position());
	    }

	    value = ((Number) li.constantValue()).longValue();
	}
	else {
	    throw new SemanticException("Case label must be a constant.",
					position());
	}

	return value(value);
    }

    public Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == expr) {
            return child.expectedType(ts.Int());
        }

        return child;
    }

    public String toString() {
        if (expr == null) {
	    return "default:";
	}
	else {
	    return "case " + expr + ":";
	}
    }

    /** Write the statement to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        if (expr == null) {
	    w.write("default:");
	}
	else {
	    w.write("case ");
	    expr.translate(w, tr);
	    w.write(":");
	}
    }
}
