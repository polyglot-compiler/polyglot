package jltools.frontend;

import java.io.*;
import jltools.ast.*;
import jltools.util.*;

/**
 * A parser implemented with a Cup generated-parser.
 */
public class CupParser implements Parser
{
    java_cup.runtime.lr_parser grm;
    Job job;

    public CupParser(java_cup.runtime.lr_parser grm, Job job) {
	this.grm = grm;
	this.job = job;
    }

    public Node parse() {
	Source s = job.source();
	ErrorQueue eq = job.compiler().errorQueue();

	try {
	    java_cup.runtime.Symbol sym = grm.parse();

	    if (sym != null && sym.value instanceof Node) {
		return (Node) sym.value;
	    }

	    eq.enqueue(ErrorInfo.SYNTAX_ERROR, "Unable to parse " +
		s.name() + ".");
	}
	catch (IOException e) {
	    eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
	}
	catch (RuntimeException e) {
	    e.printStackTrace();
	    eq.enqueue(ErrorInfo.INTERNAL_ERROR, e.getMessage());
	}
	catch (Exception e) {
	    // Used by cup to indicate a non-recoverable error.
	    eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
	}

	return null;
    }

    public String toString() {
	return "CupParser(" + grm.getClass().getName() + ")";
    }
}
