package jltools.frontend;

import jltools.ast.*;
import jltools.util.*;

/** A pass which runs a visitor. */
public class VisitorPass implements Pass
{
    Job job;
    NodeVisitor v;

    public VisitorPass(Job job) {
	this(job, null);
    }

    public VisitorPass(Job job, NodeVisitor v) {
	this.job = job;
	this.v = v;
    }

    public void visitor(NodeVisitor v) {
	this.v = v;
    }

    public NodeVisitor visitor() {
	return v;
    }

    public boolean run() {
	Node ast = job.ast();

	if (ast == null) {
	    throw new InternalCompilerError("Null AST: did the parser run?");
	}

	ast = ast.visit(v);

	job.ast(ast);

	if (job.compiler().errorQueue().hasErrors()) {
	    return false;
	}

	return true;
    }

    public String toString() {
	return v.getClass().getName();
    }
}
