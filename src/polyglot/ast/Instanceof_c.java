package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>Instanceof</code> is an immutable representation of
 * the use of the <code>instanceof</code> operator.
 */
public class Instanceof_c extends Expr_c implements Instanceof
{
    protected Expr expr;
    protected TypeNode compareType;

    public Instanceof_c(Del ext, Position pos, Expr expr, TypeNode compareType) {
	super(ext, pos);
	this.expr = expr;
	this.compareType = compareType;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return Precedence.INSTANCEOF;
    }

    /** Get the expression to be tested. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the expression to be tested. */
    public Instanceof expr(Expr expr) {
	Instanceof_c n = (Instanceof_c) copy();
	n.expr = expr;
	return n;
    }

    /** Get the type to be compared against. */
    public TypeNode compareType() {
	return this.compareType;
    }

    /** Set the type to be compared against. */
    public Instanceof compareType(TypeNode compareType) {
	Instanceof_c n = (Instanceof_c) copy();
	n.compareType = compareType;
	return n;
    }

    /** Reconstruct the expression. */
    protected Instanceof_c reconstruct(Expr expr, TypeNode compareType) {
	if (expr != this.expr || compareType != this.compareType) {
	    Instanceof_c n = (Instanceof_c) copy();
	    n.expr = expr;
	    n.compareType = compareType;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	Expr expr = (Expr) visitChild(this.expr, v)  ;
	TypeNode compareType = (TypeNode) visitChild(this.compareType, v);
	return reconstruct(expr, compareType);
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (! compareType.type().isReference()) {
	    throw new SemanticException(
		"Type operand of \"instanceof\" must be a reference type.",
		compareType.position());
	}

	if (! expr.type().isCastValid(compareType.type())) {
	    throw new SemanticException(
		"Expression operand incompatible with type in \"instanceof\".",
		expr.position());
	}

	return type(ts.Boolean());
    }

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == expr) {
            return child.expectedType(ts.Object());
        }

        return child;
    }

    public String toString() {
	return expr + " instanceof " + compareType;
    }

    /** Write the expression to an output file. */
    public void translate(CodeWriter w, Translator tr) {
	translateSubexpr(expr, w, tr);
	w.write(" instanceof ");
	compareType.del().translate(w, tr);
    }
}
