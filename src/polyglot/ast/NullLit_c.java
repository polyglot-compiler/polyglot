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
    public NullLit_c(Del ext, Position pos) {
	super(ext, pos);
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) {
	return type(tc.typeSystem().Null());
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
	return null;
    }

    public String toString() {
	return "null";
    }

    /** Write the expression to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("null");
    }
}
