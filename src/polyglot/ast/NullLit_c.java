package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * The Java literal <code>null</code>.
 */
public class NullLit_c extends Lit_c implements NullLit
{
    public NullLit_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) {
	return type(tc.typeSystem().Null());
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return null;
    }

    public String toString() {
	return "null /* lit */";
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("null");
    }
}
