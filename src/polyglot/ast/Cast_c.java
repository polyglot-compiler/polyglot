package polyglot.ext.jl.ast;

import polyglot.ast.*;

import polyglot.util.*;
import polyglot.visit.*;
import polyglot.types.*;

/**
 * A <code>Cast</code> is an immutable representation of a casting
 * operation.  It consists of an <code>Expr</code> being cast and a
 * <code>TypeNode</code> being cast to.
 */ 
public class Cast_c extends Expr_c implements Cast
{
    protected TypeNode castType;
    protected Expr expr;

    public Cast_c(Del ext, Position pos, TypeNode castType, Expr expr) {
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
	TypeNode castType = (TypeNode) visitChild(this.castType, v);
	Expr expr = (Expr) visitChild(this.expr, v);
	return reconstruct(castType, expr);
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (! ts.isCastValid(expr.type(), castType.type())) {
	    throw new SemanticException("Cannot cast the expression of type \"" 
					+ expr.type() + "\" to type \"" 
					+ castType.type() + "\".",
				        position());
	}

	return type(castType.type());
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            if (castType.type().isReference()) {
                return ts.Object();
            }
            else if (castType.type().isNumeric()) {
                return ts.Double();
            }
            else if (castType.type().isBoolean()) {
                return ts.Boolean();
            }
        }

        return child.type();
    }
  
    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
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
    public void prettyPrint(CodeWriter w, PrettyPrinter tr)
    {
	w.begin(0);
	w.write("(");
	tr.print(castType, w);
	w.write(")");
	w.allowBreak(2, " ");
	printSubExpr(expr, w, tr);
	w.end();
    }
}
