package polyglot.frontend;

import polyglot.frontend.Compiler;
import polyglot.util.InternalCompilerError;
import java.util.*;

/**
 * A <code>BarrierPass</code> is a special pass that ensures that
 * all jobs complete a goal pass before any job continues.
 */
public class BarrierPass extends AbstractPass
{
    Job job;

    public BarrierPass(Pass.ID id, Job job) {
      	super(id);
	this.job = job;
    }

    /** Run all the other jobs with the same parent up to this pass. */
    public boolean run() {
        if (Compiler.should_report(1))
	    Compiler.report(job + " at barrier " + id, 1);
        if (Compiler.should_report(2))
	    Compiler.report("children of " + job + " = " + job.children(), 1);

        if (job.compiler().errorQueue().hasErrors()) {
            return false;
        }

        // Bring all our children up to the barrier.
        for (Iterator i = job.children().iterator(); i.hasNext(); ) {
            Job child = (Job) i.next();

            if (Compiler.should_report(2))
                Compiler.report(job + " bringing " + child + " to barrier " + id, 1);

            if (! job.extensionInfo().runToPass(child, id)) {
                return false;
	    }
        }

	return true;
    }
}
