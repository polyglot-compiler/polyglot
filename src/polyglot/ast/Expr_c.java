package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

public abstract class Expr_c extends Node_c implements Expr
{
    protected Type type;

    public Expr_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    public Type type() {
	return this.type;
    }

    public Expr type(Type type) {
	Expr_c n = (Expr_c) copy();
	n.type = type;
	return n;
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	return type(tb.typeSystem().unknownType(position()));
    }

    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c) 
	throws SemanticException {

	if (type == null) {
	    return type(ts.unknownType(position()));
	}

	return this;
    }

    public void dump(CodeWriter w) {
        super.dump(w);

	if (type != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(type " + type + ")");
	    w.end();
	}
    }

    public Precedence precedence() {
	return Precedence.UNKNOWN;
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
