/*
 * MissingDependencyException.java
 * 
 * Author: nystrom
 * Creation date: Aug 7, 2005
 */
package polyglot.frontend;

import polyglot.frontend.goals.Goal;

/**
 * A <code>MissingDependencyException</code> is thrown when a goal cannot be
 * reached (yet) because the it is dependent on another, often just discovered,
 * goal.
 *
 * @author nystrom
 */
public class MissingDependencyException extends SchedulerException {
    protected Goal goal;
    protected boolean prerequisite;
    
    public MissingDependencyException(Goal goal) {
        this(goal, false);
    }
    
    public MissingDependencyException(Goal goal, boolean prerequisite) {
        super(goal.toString());
        this.goal = goal;
        this.prerequisite = prerequisite;
    }

    public Goal goal() {
        return goal;
    }
    
    public boolean prerequisite() {
        return prerequisite;
    }
    
    public void printStackTrace() {
        super.printStackTrace();
    }
}
