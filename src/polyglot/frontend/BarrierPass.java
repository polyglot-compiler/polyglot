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
        Compiler.report(1, job + " at barrier " + id);

        if (job.compiler().errorQueue().hasErrors()) {
            return false;
        }

        // Bring all our children up to the barrier.
        for (Iterator i = job.children().iterator(); i.hasNext(); ) {
            Job child = (Job) i.next();

            if (! job.extensionInfo().runToPass(child, id)) {
                return false;
	    }
        }

	return true;
    }
}
