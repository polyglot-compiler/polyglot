package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A <code>FloatLit</code> represents a literal in java of type
 * <code>float</code> or <code>double</code>.
 */
public class FloatLit_c extends Lit_c implements FloatLit
{
    protected FloatLit.Kind kind;
    protected double value;

    public FloatLit_c(Del ext, Position pos, FloatLit.Kind kind, double value) {
	super(ext, pos);
	this.kind = kind;
	this.value = value;
    }

    /** Get the kind of the literal. */
    public FloatLit.Kind kind() {
	return this.kind;
    }

    /** Set the kind of the literal. */
    public FloatLit kind(FloatLit.Kind kind) {
	FloatLit_c n = (FloatLit_c) copy();
	n.kind = kind;
	return n;
    }

    /** Get the value of the expression. */
    public double value() {
	return this.value;
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return new Double(this.value);
    }

    /** Set the value of the expression. */
    public FloatLit value(double value) {
	FloatLit_c n = (FloatLit_c) copy();
	n.value = value;
	return n;
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	if (kind == FLOAT) {
	    return type(tc.typeSystem().Float());
	}
	else if (kind == DOUBLE) {
	    return type(tc.typeSystem().Double());
	}
	else {
	    throw new InternalCompilerError("Unrecognized FloatLit kind " +
		kind);
	}
    }  

    public String toString() {
	return Double.toString(value);
    }

    /** Write the expression to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (kind == FLOAT) {
	    w.write(Float.toString((float) value) + "F");
	}
	else if (kind == DOUBLE) {
	    w.write(Double.toString(value));
	}
	else {
	    throw new InternalCompilerError("Unrecognized FloatLit kind " +
		kind);
	}
    }
}
