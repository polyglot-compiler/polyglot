package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A <code>Branch</code> is an immutable representation of a branch
 * statment in Java (a break or continue).  It consists of a kind corresponding
 *  to either break or continue and an optional label specifing where to 
 * branch to.
 */
public class Branch_c extends Stmt_c implements Branch
{
    protected Branch.Kind kind;
    protected String label;

    public Branch_c(Ext ext, Position pos, Branch.Kind kind, String label) {
	super(ext, pos);
	this.kind = kind;
	this.label = label;
    }

    public Branch.Kind kind() {
	return this.kind;
    }

    public Branch kind(Branch.Kind kind) {
	Branch_c n = (Branch_c) copy();
	n.kind = kind;
	return n;
    }

    public String label() {
	return this.label;
    }

    public Branch label(String label) {
	Branch_c n = (Branch_c) copy();
	n.label = label;
	return n;
    }

    public String toString() {
	return kind.toString() + (label != null ? " " + label : "");
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write(kind.toString());
	if (label != null) {
	    w.write(" " + label);
	}
	w.write(";");
    }
}
