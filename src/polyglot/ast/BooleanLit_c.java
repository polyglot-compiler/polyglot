package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A <code>BooleanLit</code> represents a boolean literal expression.
 */
public class BooleanLit_c extends Lit_c implements BooleanLit
{
    protected boolean value;

    public BooleanLit_c(Ext ext, Position pos, boolean value) {
	super(ext, pos);
	this.value = value;
    }

    /** Get the value of the expression. */
    public boolean value() {
	return this.value;
    }

    /** Set the value of the expression. */
    public BooleanLit value(boolean value) {
	BooleanLit_c n = (BooleanLit_c) copy();
	n.value = value;
	return n;
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return new Boolean(this.value);
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Boolean());
    }

    public String toString() {
	return "" + value;
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("" + value);
    }
}
