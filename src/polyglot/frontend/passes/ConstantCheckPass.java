/*
 * DisambiguationPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.VisitorPass;
import polyglot.frontend.goals.*;
import polyglot.visit.ConstantChecker;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>DisambiguationPass</code>
 *
 * @author nystrom
 */
public class ConstantCheckPass extends VisitorPass {
    public ConstantCheckPass(ConstantsCheckedForFile goal, ConstantChecker v) {
        this((Goal) goal, v);
    }
    public ConstantCheckPass(FieldConstantsChecked goal, ConstantChecker v) {
        this((Goal) goal, v);
    }
    private ConstantCheckPass(Goal goal, ConstantChecker v) {
        super(goal, v);
    }

    public void markGoalReached() {
        if (goal instanceof ConstantsCheckedForFile) {
            // Record that we've run the pass at least once.
            ((ConstantsCheckedForFile) goal).markRun();
        }
        // Don't mark the goal reached; the pass will be rerun only if necessary
    }
}
