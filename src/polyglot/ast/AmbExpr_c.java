package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public class AmbExpr_c extends Expr_c implements AmbExpr
{
    protected String name;

    public AmbExpr_c(Ext ext, Position pos, String name) {
	super(ext, pos);
	this.name = name;
    }

    /** Get the name of the expression. */
    public String name() {
	return this.name;
    }

    /** Set the name of the expression. */
    public AmbExpr name(String name) {
	AmbExpr_c n = (AmbExpr_c) copy();
	n.name = name;
	return n;
    }

    /** Disambiguate the expression. */
    public Node disambiguateOverride_(AmbiguityRemover ar)
	throws SemanticException {

	Node n = new Disamb().disambiguate(ar, position(), null, name);

	if (n instanceof Expr) {
	    return n;
	}

	throw new SemanticException("Could not find field or local " +
	    "variable \"" + name + "\".", position());
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot type check ambiguous node " + this + ".");
    } 

    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot exception check ambiguous node " + this + ".");
    } 

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	throw new InternalCompilerError(position(),
	    "Cannot translate ambiguous node " + this + ".");
    }

    public String toString() {
	return name + "{amb}";
    }
}
