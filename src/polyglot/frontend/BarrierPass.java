package jltools.frontend;

import jltools.frontend.Compiler;
import java.util.*;

/**
 * A <code>BarrierPass</code> is a special pass that ensures that
 * all jobs complete a goal pass before any job continues.
 */
public abstract class BarrierPass extends AbstractPass
{
    Compiler compiler;

    public BarrierPass(Compiler compiler) {
	this.compiler = compiler;
    }

    /** Get the pass we're supposed to run for each job. */
    public abstract Pass pass(Job job);

    public List runAfter() {
	List deps = new ArrayList(compiler.jobs().size());

	for (Iterator i = compiler.jobs().iterator(); i.hasNext(); ) {
	    Job job = (Job) i.next();
	    Pass pass = pass(job);
	    deps.add(pass);
	}

	return deps;
    }

    /** Do nothing.  Getting here is enough. */
    public boolean run() {
	return true;
    }

    public String toString() {
	return "Barrier";
    }
}
