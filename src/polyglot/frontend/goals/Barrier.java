/*
 * Barrier.java
 * 
 * Author: nystrom
 * Creation date: Feb 4, 2005
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.Collection;
import java.util.Iterator;

import polyglot.frontend.*;
import polyglot.util.InternalCompilerError;

/**
 * Comment for <code>Barrier</code>
 *
 * @author nystrom
 */
public abstract class Barrier extends AbstractGoal {
    protected Scheduler scheduler;
    
    protected Barrier(Scheduler scheduler) {
        super(null);
        this.scheduler = scheduler;
    }

    protected Barrier(String name, Scheduler scheduler) {
        super(null, name);
        this.scheduler = scheduler;
    }
    
    public Collection jobs() {
        return scheduler.jobs();
    }

    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#createPass(polyglot.frontend.ExtensionInfo)
     */
    public Pass createPass(ExtensionInfo extInfo) {
        return new BarrierPass(scheduler, this);
    }

    protected static class BarrierPass extends AbstractPass {
        Scheduler scheduler;
        
        protected BarrierPass(Scheduler scheduler, Barrier barrier) {
            super(barrier);
            this.scheduler = scheduler;
        }
                
        public boolean run() {
            Barrier barrier = (Barrier) goal();
            for (Iterator i = barrier.jobs().iterator(); i.hasNext(); ) {
                Job job = (Job) i.next();
                Goal subgoal = barrier.goalForJob(job);
                if (! subgoal.hasBeenReached()) {
                    scheduler.addDependencyAndEnqueue(barrier, subgoal, true);
                    barrier.setUnreachableThisRun();
                }
            }
            return true;
        }
    }

    public abstract Goal goalForJob(Job job); 

    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }
    
    public int hashCode() {
        if (name == null) {
            return System.identityHashCode(this);
        }
        else {
            return name.hashCode();
        }
    }

    public boolean equals(Object o) {
        if (name == null) {
            return this == o;
        }
        else if (o instanceof Barrier) {
            Barrier b = (Barrier) o;
            return name.equals(b.name);
        }
        return false;
    }
}
