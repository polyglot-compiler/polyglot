/*
 * DisambiguationPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.VisitorPass;
import polyglot.frontend.goals.*;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>DisambiguationPass</code>
 *
 * @author nystrom
 */
public class TypeCheckPass extends VisitorPass {
    public TypeCheckPass(TypeChecked goal, TypeChecker v) {
        this((Goal) goal, v);
    }
    public TypeCheckPass(SupertypesResolved goal, TypeChecker v) {
        this((Goal) goal, v);
    }
    public TypeCheckPass(AllMembersAdded goal, TypeChecker v) {
        this((Goal) goal, v);
    }
    public TypeCheckPass(SignaturesResolved goal, TypeChecker v) {
        this((Goal) goal, v);
    }
    private TypeCheckPass(Goal goal, TypeChecker v) {
        super(goal, v);
    }

    public void markGoalReached() {
        if (goal instanceof TypeChecked) {
            // Record that we've run the pass at least once.
            ((TypeChecked) goal).markRun();
        }
        // Don't mark the goal reached; the pass will be rerun only if necessary
    }
}
