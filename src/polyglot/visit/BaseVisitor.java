package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;

/** Abstract class that is the base of visitors associated with a Job. */
public abstract class BaseVisitor extends NodeVisitor
{
    protected Job job;

    public BaseVisitor(Job job) {
        this.job = job;
    }

    public Job job() {
	return job;
    }

    public ErrorQueue errorQueue() {
	return job.compiler().errorQueue();
    }

    public NodeFactory nodeFactory() {
	return job.compiler().nodeFactory();
    }

    public TypeSystem typeSystem() {
	return job.compiler().typeSystem();
    }

    public TableResolver parsedResolver() {
	return job.compiler().parsedResolver();
    }

    public ImportTable importTable() {
	return job.importTable();
    }
}
