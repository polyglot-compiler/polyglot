package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.frontend.Compiler;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Options;

/**
 * An <code>Import</code> is an immutable representation of a Java
 * <code>import</code> statement.  It consists of the string representing the
 * item being imported and the kind  which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */
public class Import_c extends Node_c implements Import
{
    protected Kind kind;
    protected String name;

    public Import_c(Ext ext, Position pos, Kind kind, String name) {
	super(ext, pos);
	this.name = name;
	this.kind = kind;
    }

    public String name() {
	return this.name;
    }

    public Import name(String name) {
	Import_c n = (Import_c) copy();
	n.name = name;
	return n;
    }

    public Kind kind() {
	return this.kind;
    }

    public Import kind(Kind kind) {
	Import_c n = (Import_c) copy();
	n.kind = kind;
	return n;
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	ImportTable it = tb.importTable();

	if (kind == CLASS) {
	    it.addClassImport(name);
	}
	else if (kind == PACKAGE) {
	    it.addPackageImport(name);
	}

	return this;
    }
   
    public String toString() {
	return "import " + name + (kind == PACKAGE ? ".*" : "");
    }

    public void translate_(CodeWriter w, Translator tr) {
	if (! Options.global.fully_qualified_names) {
	    w.write("import ");
	    w.write(name);

	    if (kind == PACKAGE) {
	        w.write(".*");
	    }

	    w.write(";");
	    w.newline(0);
	}
    }
}
