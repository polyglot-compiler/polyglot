package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A ReturnStatment is an immutable representation of a <code>return</code>
 * statement in Java.
 */
public class Return_c extends Stmt_c implements Return
{
    protected Expr expr;

    public Return_c(Ext ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    public Expr expr() {
	return this.expr;
    }

    public Return expr(Expr expr) {
	Return_c n = (Return_c) copy();
	n.expr = expr;
	return n;
    }

    protected Return_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Return_c n = (Return_c) copy();
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr expr = null;

	if (this.expr != null) {
	    expr = (Expr) this.expr.visit(v);
	}

	return reconstruct(expr);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	Context c = tc.context();

	CodeInstance ci = c.currentCode();

	if (ci instanceof InitializerInstance) {
	    throw new SemanticException(
		"Cannot return from an initializer block.", position());
	}

	if (ci instanceof ConstructorInstance) {
	    if (expr != null) {
		throw new SemanticException(
		    "Cannot return a value from " + ci + ".",
		    position());
	    }

	    return this;
	}

	if (ci instanceof MethodInstance) {
	    MethodInstance mi = (MethodInstance) ci;

	    if (expr == null) {
	        if (! mi.returnType().isVoid()) {
		    throw new SemanticException("Must return a value from " +
			mi + ".", position());
		}

		return this;
	    }

	    if (mi.returnType().isVoid()) {
		throw new SemanticException("Cannot return a value from " +
		    mi + ".", position());
	    }

	    if (expr.type().isImplicitCastValid(mi.returnType())) {
	        return this;
	    }

	    if (expr instanceof NumLit) {
	        long value = ((NumLit) expr).longValue();

		if (! ts.numericConversionValid(mi.returnType(), value)) {
		    return this;
		}
	    }

	    throw new SemanticException("Must return an expression of type " +
		mi.returnType() + " from " + mi + ".  Expression has type " +
		expr.type() + ".", expr.position());
	}

	throw new InternalCompilerError("Unrecognized code type.");
    }
  
    public String toString() {
	return "return" + (expr != null ? " " + expr : "");
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("return") ;
	if (expr != null) {
	    w.write(" ");
	    expr.ext().translate(w, tr);
	}
	w.write(";");
    }

}
