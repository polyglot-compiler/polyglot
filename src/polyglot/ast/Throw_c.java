package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * A <code>Throw</code> is an immutable representation of a <code>throw</code>
 * statement. Such a statement contains a single <code>Expr</code> which
 * evaluates to the object being thrown.
 */
public class Throw_c extends Stmt_c implements Throw
{
    protected Expr expr;

    public Throw_c(Del ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    /** Get the expression to throw. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the expression to throw. */
    public Throw expr(Expr expr) {
	Throw_c n = (Throw_c) copy();
	n.expr = expr;
	return n;
    }

    /** Reconstruct the statement. */
    protected Throw_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Throw_c n = (Throw_c) copy();
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
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	if (! expr.type().isThrowable()) {
	    throw new SemanticException(
		"Can only throw subclasses of \"" +
		tc.typeSystem().Throwable() + "\".", expr.position());
	}

	return this;
    }

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == expr) {
            return child.expectedType(ts.Throwable());
        }

        return child;
    }

    /** Check exceptions thrown by the statement. */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException
    {
	ec.throwsException(expr.type());
	return this;
    }

    /*
    public String toString() {
	return "throw " + expr;
    }
    */

    /** Write the statement to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("throw ");
	tr.print(expr, w);
	w.write(";");
    }
}
