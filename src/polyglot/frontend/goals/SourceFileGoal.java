/*
 * SourceFileGoal.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.goals;

import polyglot.frontend.Job;


/**
 * Comment for <code>SourceFileGoal</code>
 *
 * @author nystrom
 */
public abstract class SourceFileGoal extends AbstractGoal {

    protected boolean runOnce;
    
    public SourceFileGoal(Job job) {
        super(job);
        this.runOnce = false;
    }

    public SourceFileGoal(Job job, String name) {
        super(job, name);
        this.runOnce = false;
    }
    
    public boolean reached() {
        return runOnce;
    }
    
    public void markRun() {
        this.runOnce = true;
    }
    
    public boolean hasBeenRun() {
        return runOnce;
    }

    public String toString() {
        return super.toString() + (runOnce ? " (run)" : " (not-run)");
    }
}
