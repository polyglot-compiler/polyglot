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
    /** The source file for the job. */
    protected Source source;

    /** The compiler which performs work for the job. */
    protected Compiler compiler;

    /** The import table constructed from the source file. */
    protected ImportTable it;

    /** The AST constructed from the source file. */
    protected Node ast;

    /** Construct a new job for a given source and compiler. */
    public Job(Source s, Compiler c) {
	this.source = s;
	this.compiler = c;
	this.it = new ImportTable(c.typeSystem(),
	                          c.systemResolver(), s, c.errorQueue());
    }

    /** Get the job's AST. */
    public Node ast() { return ast; }
    /** Set the job's AST. */
    public void ast(Node ast) { this.ast = ast; }

    /** The build types pass. */
    public abstract Pass buildPass();
    /** The disambiguate types pass. */
    public abstract Pass disambTypesPass();
    /** The translate pass. */
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

    /** The compiler which performs work for the job. */
    public Compiler compiler() {
	return compiler;
    }

    /** The import table constructed from the source file. */
    public ImportTable importTable() {
	return it;
    }

    /** The source file for the job. */
    public Source source() {
	return source;
    }

    public void dump(CodeWriter cw) {
	if (ast != null) {
	    ast.dump(cw);
	}
    }
}
