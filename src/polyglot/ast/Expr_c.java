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

    public Expr_c(Del ext, Position pos) {
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
        if (type == this.type) return this;
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
        if (expectedType == this.expectedType) return this;
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
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(expectedType " + expectedType + ")");
	    w.end();
	}
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    /**
     * Correctly parenthesize the subexpression <code>expr<code> given
     * the its precendence and the precedence of the current expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we do not parenthesize.
     *
     * @param expr The subexpression.
     * (right-) associative operator.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    public void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp) {
        printSubExpr(expr, true, w, pp);
    }

    /**
     * Correctly parenthesize the subexpression <code>expr<code> given
     * the its precendence and the precedence of the current expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we parenthesize if the sub-expression does not associate; e.g.,
     * we parenthesis the right sub-expression of a left-associative
     * operator.
     *
     * @param expr The subexpression.
     * @param associative Whether expr is the left (right) child of a left-
     * (right-) associative operator.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    public void printSubExpr(Expr expr, boolean associative,
                             CodeWriter w, PrettyPrinter pp) {
        if (! associative && precedence().isSame(expr.precedence()) ||
	    precedence().isTighter(expr.precedence())) {
	    w.write("(");
            printBlock(expr, w, pp);
	    w.write( ")");
	}
        else {
            printBlock(expr, w, pp);
        }
    }
}
