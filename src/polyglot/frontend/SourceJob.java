package polyglot.frontend;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

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
    protected Context context;

    /** Construct a new job for a given source and compiler. */
    public SourceJob(ExtensionInfo lang, JobExt ext, Job parent, Source source, Node ast) {
        super(lang, ext, parent, null);
        this.source = source;
        this.ast = ast;
    }

    public List getPasses() {
        return lang.passes(this);
    }

    public Context context() {
        return null;
    }

    public Source source() {
	return source;
    }

    public String toString() {
        return source.toString() + " (" +
            (done() ? "done"
                    : ((isRunning() ? "running "
                                    : "before ") + nextPass()) + ")");
    }
}
