package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * A <code>Throw</code> is an immutable representation of a
 * <code>throw</code> statement. Such a statement  contains a single
 * <code>Expr</code> which evaluates to the object being thrown.
 */
public class Throw_c extends Stmt_c implements Throw
{
    protected Expr expr;

    public Throw_c(Ext ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    public Expr expr() {
	return this.expr;
    }

    public Throw expr(Expr expr) {
	Throw_c n = (Throw_c) copy();
	n.expr = expr;
	return n;
    }

    protected Throw_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Throw_c n = (Throw_c) copy();
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) this.expr.visit(v);
	return reconstruct(expr);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	if (! expr.type().isThrowable()) {
	    throw new SemanticException(
		"Can only throw subclasses of \"" +
		tc.typeSystem().Throwable() + "\".", expr.position());
	}

	return this;
    }

    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException
    {
	ec.throwsException(expr.type());
	return this;
    }

    public String toString() {
	return "throw " + expr;
    }

    public void translate_(CodeWriter w, Translator tr)
    {
	w.write("throw ");
	expr.ext().translate(w, tr);
	w.write(";");
    }
}
