package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>Expr</code> represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public abstract class Expr_c extends Node_c implements Expr
{
    protected Type type;
    protected Type expectedType;

    public Expr_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    /**
     * Get the type of the expression.  This may return an
     * <code>UnknownType</code> before type-checking, but should return the
     * correct type after type-checking.
     */
    public Type type() {
	return this.type;
    }

    /** Set the type of the expression. */
    public Expr type(Type type) {
	Expr_c n = (Expr_c) copy();
	n.type = type;
	return n;
    }

    /**
     * Get the expected type of the expression.
     */
    public Type expectedType() {
        if (this.expectedType == null) {
            return this.type;
        }

	return this.expectedType;
    }

    /** Set the type of the expression. */
    public Expr expectedType(Type expectedType) {
	Expr_c n = (Expr_c) copy();
	n.expectedType = expectedType;
	return n;
    }

    public void dump(CodeWriter w) {
        super.dump(w);

	if (type != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(type " + type + ")");
	    w.end();
	    w.begin(0);
	    w.write("(expectedType " + expectedType + ")");
	    w.end();
	}
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    /**
     * Correctly parenthesize the subexpression <code>expr<code> given
     * the its precendence and the precedence of the current expression.
     *
     * @param expr The subexpression.
     * @param c The context of translation.
     * @param w The output writer.
     */
    public void translateSubexpr(Expr expr, CodeWriter w, Translator tr) {
	if (precedence().isTighter(expr.precedence())) {
	    w.write("(");
	}

	translateBlock(expr, w, tr);

	if (precedence().isTighter(expr.precedence())) {
	    w.write( ")");
	}
    }
}
