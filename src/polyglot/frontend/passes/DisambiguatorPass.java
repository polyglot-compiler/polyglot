/*
 * DisambiguationPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.Scheduler;
import polyglot.frontend.VisitorPass;
import polyglot.frontend.goals.*;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>DisambiguationPass</code>
 *
 * @author nystrom
 */
public class DisambiguatorPass extends VisitorPass {

    public DisambiguatorPass(Goal goal, AmbiguityRemover v) {
        super(goal, v);
    }

    public void markGoalReached() {
        AmbiguityRemover v = (AmbiguityRemover) visitor();
        // Don't mark the goal reached; the pass will be rerun only if necessary
        // Record that we've run the pass at least once.
        Scheduler scheduler = v.typeSystem().extensionInfo().scheduler();
        ((Disambiguated) scheduler.Disambiguated(v.job())).markRun();
    }
}
