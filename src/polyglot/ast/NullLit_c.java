package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An Lit represents the Java literal <code>null</code>.
 */
public class NullLit_c extends Lit_c implements NullLit
{
    public NullLit_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    public Node typeCheck_(TypeChecker tc) {
	return type(tc.typeSystem().Null());
    }

    public Object objValue() {
	return null;
    }

    public String toString() {
	return "null /* lit */";
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("null");
    }
}
