package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

public class Case_c extends Stmt_c implements Case
{
    protected Expr expr;
    protected long value;

    public Case_c(Ext ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    public boolean isDefault() {
	return this.expr == null;
    }

    public Expr expr() {
	return this.expr;
    }

    public Case expr(Expr expr) {
	Case_c n = (Case_c) copy();
	n.expr = expr;
	return n;
    }

    public long value() {
	return this.value;
    }

    protected Case value(long value) {
	Case_c n = (Case_c) copy();
	n.value = value;
	return n;
    }

    protected Case_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Case_c n = (Case_c) copy();
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	if (this.expr != null) {
	    Expr expr = (Expr) this.expr.visit(v);
	    return reconstruct(expr);
	}

	return this;
    }

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

    public String toString() {
        if (expr == null) {
	    return "default:";
	}
	else {
	    return "case " + expr + ":";
	}
    }

    public void translate_(CodeWriter w, Translator tr) {
        if (expr == null) {
	    w.write("default:");
	}
	else {
	    w.write("case ");
	    expr.ext().translate(w, tr);
	    w.write(":");
	}
    }
}
