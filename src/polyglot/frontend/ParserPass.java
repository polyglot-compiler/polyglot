/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import java.io.IOException;
import java.io.Reader;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyglot.main.Report;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

/**
 * A pass which runs a parser.  After parsing it stores the AST in the Job.
 * so it can be accessed by later passes.
 */
public class ParserPass extends AbstractPass
{
    protected Compiler compiler;

    public ParserPass(Compiler compiler, Goal goal) {
        super(goal);
	this.compiler = compiler;
    }

    public boolean run() {
	ErrorQueue eq = compiler.errorQueue();
        
	FileSource source = (FileSource) goal.job().source();

	try {
	    Reader reader = source.open();

	    Parser p = compiler.sourceExtension().parser(reader, source, eq);

	    if (Report.should_report(Report.frontend, 2))
		Report.report(2, "Using parser " + p);

	    Node ast = p.parse();

	    source.close();

	    if (ast != null) {
		goal.job().ast(ast);
		return true;
	    }

	    return false;
	}
	catch (IOException e) {
	    eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
	    return false;
	}
    }

    public String toString() {
	return super.toString() + "(" + goal.job().source() + ")";
    }
}
