package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.frontend.Compiler;

import java.util.*;
import java.io.IOException;

/**
 * A <code>State</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public class SourceJob extends Job
{
    protected Source source;
    protected ImportTable it;
    protected Context context;

    /** Construct a new job for a given source and compiler. */
    public SourceJob(Compiler c, JobExt ext, Job parent, Source source) {
        super(c, ext, parent, null);
        this.source = source;
        this.it = new ImportTable(c.typeSystem(), c.systemResolver(),
				  source, c.errorQueue());
    }

    public List getPasses() {
      	return compiler.extensionInfo().passes(this);
    }

    public Context context() {
	if (context == null) {
	    context = compiler.typeSystem().createContext(it);
	}

	return context;
    }

    public ImportTable importTable() {
        return it;
    }

    public Source source() {
	return source;
    }

    public String toString() {
        return source.toString() + " (" +
            (isRunning() ? "running " : "before ") + nextPass() + ")";
    }
}
