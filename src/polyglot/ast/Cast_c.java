package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.visit.*;
import jltools.types.*;

/**
 * A <code>Cast</code> is an immutable representation of a casting
 * operation.  It consists of an <code>Expr</code> being cast and a
 * <code>TypeNode</code> being cast to.
 */ 
public class Cast_c extends Expr_c implements Cast
{
    protected TypeNode castType;
    protected Expr expr;

    public Cast_c(Ext ext, Position pos, TypeNode castType, Expr expr) {
	super(ext, pos);
	this.castType = castType;
	this.expr = expr;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() {
	return Precedence.CAST;
    }

    /** Get the cast type of the expression. */
    public TypeNode castType() {
	return this.castType;
    }

    /** Set the cast type of the expression. */
    public Cast castType(TypeNode castType) {
	Cast_c n = (Cast_c) copy();
	n.castType = castType;
	return n;
    }

    /** Get the expression being cast. */
    public Expr expr() {
	return this.expr;
    }

    /** Set the expression being cast. */
    public Cast expr(Expr expr) {
	Cast_c n = (Cast_c) copy();
	n.expr = expr;
	return n;
    }

    /** Reconstruct the expression. */
    protected Cast_c reconstruct(TypeNode castType, Expr expr) {
	if (castType != this.castType || expr != this.expr) {
	    Cast_c n = (Cast_c) copy();
	    n.castType = castType;
	    n.expr = expr;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode castType = (TypeNode) this.castType.visit(v);
	Expr expr = (Expr) this.expr.visit(v);
	return reconstruct(castType, expr);
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (! expr.type().isCastValid(castType.type())) {
	    throw new SemanticException("Cannot cast the expression of type \"" 
					+ expr.type() + "\" to type \"" 
					+ castType.type() + "\".",
				        position());
	}

	return type(castType.type());
    }
  
    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
      if (expr.type().isReference()) {
	  TypeSystem ts = ec.typeSystem();
	  ec.throwsException(ts.ClassCastException());
      }

      return this;
    }

    public String toString() {
	return "(" + castType + ") " + expr;
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr)
    {
	w.begin(0);
	w.write("(");
	castType.ext().translate(w, tr);
	w.write(")");
	w.allowBreak(2, " ");
	translateSubexpr(expr, w, tr);
	w.end();
    }
}
