package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * A <code>Return</code> represents a <code>return</code> statement in Java.
 * It may or may not return a value.  If not <code>expr()</code> should return
 * null.
 */
public class Return_c extends Stmt_c implements Return
{
    protected Expr expr;

    public Return_c(Del ext, Position pos, Expr expr) {
	super(ext, pos);
	this.expr = expr;
    }

    /** Get the expression to return, or null. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the expression to return, or null. */
    public Return expr(Expr expr) {
	Return_c n = (Return_c) copy();
	n.expr = expr;
	return n;
    }

    /** Reconstruct the statement. */
    protected Return_c reconstruct(Expr expr) {
	if (expr != this.expr) {
	    Return_c n = (Return_c) copy();
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
  
    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        if (child == expr) {
            Context c = tc.context();
            CodeInstance ci = c.currentCode();

            if (ci instanceof MethodInstance) {
                MethodInstance mi = (MethodInstance) ci;
                return child.expectedType(mi.returnType());
            }
        }

        return child;
    }

    public String toString() {
	return "return" + (expr != null ? " " + expr : ";");
    }

    /** Write the statement to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("return") ;
	if (expr != null) {
	    w.write(" ");
	    tr.print(expr, w);
	}
	w.write(";");
    }

}
