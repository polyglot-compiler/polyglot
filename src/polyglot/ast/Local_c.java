package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A local variable expression.
 */
public class Local_c extends Expr_c implements Local
{
    protected String name;
    protected LocalInstance li;

    public Local_c(Ext ext, Position pos, String name) {
	super(ext, pos);
	this.name = name;
    }

    /** Get the precedence of the local. */
    public Precedence precedence() { 
	return Precedence.LITERAL;
    }

    /** Get the name of the local. */
    public String name() {
	return this.name;
    }

    /** Set the name of the local. */
    public Local name(String name) {
	Local_c n = (Local_c) copy();
	n.name = name;
	return n;
    }

    /** Get the local instance of the local. */
    public LocalInstance localInstance() {
	return li;
    }

    /** Set the local instance of the local. */
    public Local localInstance(LocalInstance li) {
	Local_c n = (Local_c) copy();
	n.li = li;
	return n;
    }

    /** Type check the local. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	Context c = tc.context();
	LocalInstance li = c.findLocal(name);
	return type(li.type());
    }

    public String toString() {
	return name;
    }

    /** Write the local to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write(name);
    }
}
