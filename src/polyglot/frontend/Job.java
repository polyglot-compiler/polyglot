package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.frontend.Compiler;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Job</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public abstract class Job
{
    protected Source source;
    protected Compiler compiler;

    protected ImportTable it;
    protected Node ast;

    public Job(Source s, Compiler c) {
	this.source = s;
	this.compiler = c;
	this.it = new ImportTable(c.typeSystem(),
	                          c.systemResolver(), s, c.errorQueue());
    }

    public Node ast() { return ast; }
    public void ast(Node ast) { this.ast = ast; }

    public abstract Pass buildPass();
    public abstract Pass disambTypesPass();
    public abstract Pass translatePass();

    public String toString() {
	return source.toString();
    }

    public int hashCode() {
	return source.hashCode();
    }

    public boolean equals(Object o) {
	if (o instanceof Job) {
	    Job j = (Job) o;
	    return source.equals(j.source);
	}

	return false;
    }

    public Compiler compiler() {
	return compiler;
    }

    public ImportTable importTable() {
	return it;
    }

    public Source source() {
	return source;
    }

    public void dump(CodeWriter cw) {
	if (ast != null) {
	    ast.dump(cw);
	}
    }
}
