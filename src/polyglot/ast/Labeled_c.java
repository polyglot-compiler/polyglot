package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * Am immutable representation of a Java statement with a
 * label.  A labeled statement contains the statement being labelled
 * and a string label.
 */
public class Labeled_c extends Stmt_c implements Labeled
{
    protected String label;
    protected Stmt statement;

    public Labeled_c(Ext ext, Position pos, String label, Stmt statement) {
	super(ext, pos);
	this.label = label;
	this.statement = statement;
    }

    public String label() {
	return this.label;
    }

    public Labeled label(String label) {
	Labeled_c n = (Labeled_c) copy();
	n.label = label;
	return n;
    }

    public Stmt statement() {
	return this.statement;
    }

    public Labeled statement(Stmt statement) {
	Labeled_c n = (Labeled_c) copy();
	n.statement = statement;
	return n;
    }

    protected Labeled_c reconstruct(Stmt statement) {
	if (statement != this.statement) {
	    Labeled_c n = (Labeled_c) copy();
	    n.statement = statement;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Stmt statement = (Stmt) this.statement.visit(v);
	return reconstruct(statement);
    }

    public String toString() {
	return label + ": " + statement;
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write(label + ": ");
	statement.ext().translate(w, tr);
    }
}
