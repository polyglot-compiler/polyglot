package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A <code>Local</code> corresponds to an immutable reference
 * to a local variable (not a field of a class) in an expression.
 */
public class Local_c extends Expr_c implements Local
{
    protected String name;
    protected LocalInstance li;

    public Local_c(Ext ext, Position pos, String name) {
	super(ext, pos);
	this.name = name;
    }

    public Precedence precedence() { 
	return Precedence.LITERAL;
    }

    public String name() {
	return this.name;
    }

    public Local name(String name) {
	Local_c n = (Local_c) copy();
	n.name = name;
	return n;
    }

    public LocalInstance localInstance() {
	return li;
    }

    public Local localInstance(LocalInstance li) {
	Local_c n = (Local_c) copy();
	n.li = li;
	return n;
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	Context c = tc.context();
	LocalInstance li = c.findLocal(name);
	return type(li.type());
    }

    public String toString() {
	return name;
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write(name);
    }
}
