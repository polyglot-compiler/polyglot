package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An immutable representation of a Java language <code>synchronized</code>
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is <code>true</code>.
 */
public class Synchronized_c extends Stmt_c implements Synchronized
{
    protected Expr expr;
    protected Block body;

    public Synchronized_c(Ext ext, Position pos, Expr expr, Block body) {
	super(ext, pos);
	this.expr = expr;
	this.body = body;
    }

    public Expr expr() {
	return this.expr;
    }

    public Synchronized expr(Expr expr) {
	Synchronized_c n = (Synchronized_c) copy();
	n.expr = expr;
	return n;
    }

    public Block body() {
	return this.body;
    }

    public Synchronized body(Block body) {
	Synchronized_c n = (Synchronized_c) copy();
	n.body = body;
	return n;
    }

    protected Synchronized_c reconstruct(Expr expr, Block body) {
	if (expr != this.expr || body != this.body) {
	    Synchronized_c n = (Synchronized_c) copy();
	    n.expr = expr;
	    n.body = body;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) this.expr.visit(v);
	Block body = (Block) this.body.visit(v);
	return reconstruct(expr, body);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	if (! expr.type().descendsFrom(ts.Object()) ||
	    ! expr.type().isSame(ts.Object())) {
	     throw new SemanticException(
		 "Cannot synchronize on an expression of type \"" +
		 expr.type() + "\".", expr.position());
	}

	return this;
    }

    public String toString() {
	return "synchronized (" + expr + ") { ... }";
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("synchronized (");
	translateBlock(expr, w, tr);
	w.write(") ");
	translateSubstmt(body, w, tr);
    }
}
