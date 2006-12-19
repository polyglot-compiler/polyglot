/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import java.io.IOException;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

/**
 * A parser implemented with a Cup generated-parser.
 */
public class CupParser implements Parser
{
    protected java_cup.runtime.lr_parser grm;
    protected Source source;
    protected ErrorQueue eq;

    public CupParser(java_cup.runtime.lr_parser grm, Source source, ErrorQueue eq) {
	this.grm = grm;
	this.source = source;
       	this.eq = eq;
    }

    public Node parse() {
	try {
	    java_cup.runtime.Symbol sym = grm.parse();

	    if (sym != null && sym.value instanceof Node) {
		SourceFile sf = (SourceFile) sym.value;
                return sf.source(source);
	    }
	}
	catch (IOException e) {
	    eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
	}
	catch (RuntimeException e) {
	    // Let the Compiler catch and report it.
	    throw e;
	}
	catch (Exception e) {
	    // Used by cup to indicate a non-recoverable error.
	    if (e.getMessage() != null) {
	        eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
	    }
	}

        if (! eq.hasErrors()) {
            eq.enqueue(ErrorInfo.SYNTAX_ERROR, "Unable to parse " +
                source.path() + ".");
        }

	return null;
    }

    public String toString() {
	return "CupParser(" + grm.getClass().getName() + ")";
    }
}
