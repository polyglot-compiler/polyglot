package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A <code>StringLit</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public class StringLit_c extends Lit_c implements StringLit
{
    protected String value;

    public StringLit_c(Ext ext, Position pos, String value) {
	super(ext, pos);
	this.value = value;
    }

    /** Get the value of the expression. */
    public String value() {
	return this.value;
    }

    /** Set the value of the expression. */
    public StringLit value(String value) {
	StringLit_c n = (StringLit_c) copy();
	n.value = value;
	return n;
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return this.value;
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().String());
    }

    public String toString() {
	return "\"" + StringUtil.escape(value) + "\"";
    }
    
    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("\"");
	w.write(StringUtil.escape(value));
	w.write("\"");
    }
}
