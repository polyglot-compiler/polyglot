package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Job</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public class Job
{
    protected Source source;
    protected jltools.frontend.Compiler compiler;

    protected ImportTable it;
    protected Set completed;

    protected Node ast;

    public Job(Source s, jltools.frontend.Compiler c, ExtensionInfo extInfo) {
	this.source = s;
	this.compiler = c;
	this.it = new ImportTable(c.systemResolver(), true, s, c.errorQueue());
	this.completed = new HashSet();
    }

    public Node ast() { return ast; }
    public void ast(Node ast) { this.ast = ast; }

    public void complete(PassID pass) {
	completed.add(pass);
    }

    public boolean hasCompleted(PassID pass) {
	return completed.contains(pass);
    }

    public String toString() {
	return source.toString() + " (completed=" + completed + ")";
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

    public jltools.frontend.Compiler compiler() {
	return compiler;
    }

    public ImportTable importTable() {
	return it;
    }

    public Source source() {
	return source;
    }

    public void dump(CodeWriter cw) {
	try {
	    if (ast != null) {
		ast.dump(cw);
	    }
	}
	catch (SemanticException e) {
	}
    }
}
