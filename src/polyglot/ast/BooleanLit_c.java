package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

public class BooleanLit_c extends Lit_c implements BooleanLit
{
    protected boolean value;

    public BooleanLit_c(Ext ext, Position pos, boolean value) {
	super(ext, pos);
	this.value = value;
    }

    public boolean value() {
	return this.value;
    }

    public BooleanLit value(boolean value) {
	BooleanLit_c n = (BooleanLit_c) copy();
	n.value = value;
	return n;
    }

    public Object objValue() {
	return new Boolean(this.value);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Boolean());
    }

    public String toString() {
	return "" + value;
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("" + value);
    }
}
