/*
 * Barrier.java
 * 
 * Author: nystrom
 * Creation date: Feb 4, 2005
 */
package polyglot.frontend.goals;

import java.util.Iterator;

import polyglot.frontend.*;
import polyglot.util.InternalCompilerError;

/**
 * Comment for <code>Barrier</code>
 *
 * @author nystrom
 */
public abstract class Barrier extends AbstractGoal {
    Scheduler scheduler;
    
    public Barrier(Scheduler scheduler) {
        super(null);
        this.scheduler = scheduler;
    }

    public Barrier(String name, Scheduler scheduler) {
        super(null, name);
        this.scheduler = scheduler;
    }

    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#createPass(polyglot.frontend.ExtensionInfo)
     */
    public Pass createPass(ExtensionInfo extInfo) {
        return new EmptyPass(this);
    }
    
    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#reached()
     */
    public boolean reached() {
        boolean reached = true;
        
        for (Iterator i = scheduler.jobs().iterator(); i.hasNext(); ) {
            Job job = (Job) i.next();
            Goal subgoal = goalForJob(job);
            if (! scheduler.reached(subgoal)) {
                try {
                    scheduler.addPrerequisiteDependency(this, subgoal);
                    reached = false;
                }
                catch (CyclicDependencyException e) {
                    throw new InternalCompilerError(e.getMessage());
                }
            }
        }
        
        return reached;
    }
    
    public abstract Goal goalForJob(Job job); 
    
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public boolean equals(Object o) {
        return this == o;
    }
}
