/*
 * CyclicSourceFileGoal.java
 * 
 * Author: nystrom
 * Creation date: Jan 31, 2005
 */
package polyglot.frontend.goals;

import polyglot.frontend.Job;

/**
 * Comment for <code>CyclicSourceFileGoal</code>
 *
 * @author nystrom
 */
public abstract class CyclicSourceFileGoal extends SourceFileGoal {
    protected boolean runOnce;

    public CyclicSourceFileGoal(Job job) {
        super(job);
        runOnce = false;
    }

    public CyclicSourceFileGoal(Job job, String name) {
        super(job, name);
        runOnce = false;
    }
    
    public boolean hasBeenRun() {
        return runOnce;
    }

    public void markRun() {
        runOnce = true;
    }
}
