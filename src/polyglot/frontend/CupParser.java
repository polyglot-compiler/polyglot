package jltools.frontend;

import java.io.*;
import jltools.ast.*;
import jltools.util.*;
import jltools.parse.*;

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

	    if (sym.value instanceof Node) {
		return (Node) sym.value;
	    }

	    eq.enqueue(ErrorInfo.SYNTAX_ERROR, "Unable to parse " +
		s.name() + ".");
	}
	catch (SyntaxException e) {
	    eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
	}
	catch (IOException e) {
	    eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
	}
	catch (Exception e) {
	    e.printStackTrace();
	    eq.enqueue(ErrorInfo.INTERNAL_ERROR, e.getMessage());
	}

	return null;
    }

    public String toString() {
	return "CupParser(" + grm.getClass().getName() + ")";
    }
}
