/*
 * Goal.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend.goals;

import java.util.Collection;

import polyglot.frontend.*;

/**
 * Comment for <code>Goal</code>
 *
 * @author nystrom
 */
public interface Goal {
    /**
     * Create a pass that will attempt to reach the goal. Note that the goal may
     * not be reached even if the pass succeeds. In this case, the scheduler
     * will create another pass and try again.
     */
    public Pass createPass(ExtensionInfo extInfo);
    
    /**
     * Goals on which this goal may mutually depend. If the passes for all
     * concurrent goals are run (possibly more than once) the concurrent goals
     * should eventually be reached.
     */
    public Collection concurrentGoals();
    
    /**
     * Goals that must be completed before attempting this goal. The graph of
     * dependencies between prerequisite goals should be acyclic.
     */
    public Collection prerequisiteGoals();
     
    /**
     * Add a new concurrent subgoal <code>g</code>.<code>g</code> is a
     * goal on which this goal mutually depends.  The caller must be careful
     * to ensure that all concurrent goals can be eventually reached.
     * <code>g</code> should be interned.
     */
    public void addConcurrentGoal(Goal g);
    
    /**
     * Add a new subgoal <code>g</code>.<code>g</code> must be completed
     * before this goal is attempted.  <code>g</code> should be interned.
     * 
     * @throws CyclicDependencyException
     *             if a prerequisite of <code>g</code> is <code>this</code>
     */
    public void addPrerequisiteGoal(Goal g) throws CyclicDependencyException;

    /** Return true if this goal is reachable. */
    public boolean isReachable();

    /** Set a flag indicating that this rule is unreachable. */
    public void setUnreachable();
    
    /** Return true if this goal has been reached.  Equivalent to distanceFromGoal() == 0. */
    public boolean hasBeenReached();
    
    /** Return 0 if the goal is reached, > 0 if unreached.  The return value
     * should monotonically decrease (possibly by 0) with each invocation. */
    public int distanceFromGoal();

    /** Get the job associated with this goal, or null. */
    public Job job();
    
    /** Get the name of the goal for debugging. */
    public String name();
}
