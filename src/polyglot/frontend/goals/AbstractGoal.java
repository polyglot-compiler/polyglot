/*
 * AbstractGoal.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.Collection;
import java.util.HashSet;

import polyglot.frontend.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * Comment for <code>AbstractGoal</code>
 *
 * @author nystrom
 */
public abstract class AbstractGoal implements Goal {
    Job job;
    String name;
    boolean reachable;
    Collection subgoals;
    Collection required;
        
    public AbstractGoal(Job job) {
        this(job, null);
        this.name = StringUtil.getShortNameComponent(getClass().getName());
    }

    public AbstractGoal(Job job, String name) {
        this.job = job;
        this.name = name;
        this.reachable = true;
        this.subgoals = new HashSet();
        this.required = new HashSet();
    }
    
    /** Creates a pass to attempt to satisfy the goal. */
    public abstract Pass createPass(ExtensionInfo extInfo);
    
    public String name() {
        return name;
    }

    public Job job() {
        return job;
    }
    
    public Collection prerequisiteGoals() {
        return required;
    }
    
    public Collection concurrentGoals() {
        return subgoals;
    }

    public void addPrerequisiteGoal(Goal g) throws CyclicDependencyException {
        checkCycles(g);
        required.add(g);
    }
    
    private void checkCycles(Goal current) throws CyclicDependencyException {
        if (this == current) {
            throw new CyclicDependencyException("Goal " + this + " cannot depend on itself.");
        }
        
        for (Iterator i = current.prerequisiteGoals().iterator(); i.hasNext(); ) {
            Goal subgoal = (Goal) i.next();
            checkCycles(subgoal);
        }
    }
    
    private boolean hasConcurrentGoalCycle(Goal current) {
        if (this == current) {
            return true;
        }
        
        for (Iterator i = current.concurrentGoals().iterator(); i.hasNext(); ) {
            Goal subgoal = (Goal) i.next();
            if (! hasConcurrentGoalCycle(subgoal))
                return true;
        }

        return false;
    }
    
    public void addConcurrentGoal(Goal g) {
        if (hasConcurrentGoalCycle(g)) {
            return;
        }
        subgoals.add(g);
    }

    public abstract boolean hasBeenReached();
    
    public void setUnreachable() {
        this.reachable = false;
    }
    
    public boolean isReachable() {
        return this.reachable;
    }

    public int hashCode() {
        return (job != null ? job.hashCode() : 0) + name.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof Goal) {
            Goal g = (Goal) o;
            if (job == null) {
                return g.job() == null && name.equals(g.name());
            }
            else {
                return job.equals(g.job()) && name.equals(g.name());
            }
        }
        return false;
    }
    
    public String toString() {
        return job + ":" + name;
    }
}
