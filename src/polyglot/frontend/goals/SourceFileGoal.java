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

    private boolean reached;
    
    public SourceFileGoal(Job job) {
        super(job);
        this.reached = false;
    }

    public SourceFileGoal(Job job, String name) {
        super(job, name);
        this.reached = false;
    }
    
    public boolean reached() {
        return reached;
    }
    
    public void markReached() {
        this.reached = true;
    }
    
    public String toString() {
        return super.toString() + (reached ? " (reached)" : " (unreached)");
    }
}
