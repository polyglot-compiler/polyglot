/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

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
 * A goal abstractly represents something that the compiler is supposed to
 * achieve by automatically scheduling passes.  Goals may depend on each other,
 * and a goal is not attempted until its prerequisites are achieved. Goals may
 * also be corequisites (mutually dependent).
 *
 * @author nystrom
 */
public interface Goal {
    public static final int UNREACHABLE = -1;
    public static final int UNREACHABLE_THIS_RUN = -2;
    public static final int UNREACHED = 0;
    public static final int ATTEMPTED = 1;
    public static final int REACHED = 2;
    public static final int RUNNING = 3;

    /**
     * Return true if this goal conflicts with the other; that is passes running
     * over both goals could access the same data.
     */
    public boolean conflictsWith(Goal goal);
    
    /**
     * Create a pass that will attempt to reach the goal. Note that the goal may
     * not be reached even if the pass succeeds. In this case, the scheduler
     * will create another pass and try again.
     */
    public Pass createPass(ExtensionInfo extInfo);
    
    /**
     * Goals on which this goal may mutually depend. If the passes for all
     * corequisite goals are run (possibly more than once) they should all
     * eventually be reached.
     */
    public Collection corequisiteGoals(Scheduler scheduler);
    
    /**
     * Goals that must be completed before attempting this goal. The graph of
     * dependencies between prerequisite goals should be acyclic.
     */
    public Collection prerequisiteGoals(Scheduler scheduler);
     
    /**
     * Add a new corequisite subgoal <code>g</code>.  <code>g</code> is a
     * goal on which this goal mutually depends.  The caller must be careful
     * to ensure that all corequisite goals can be eventually reached.
     * <code>g</code> should be interned.
     */
    public void addCorequisiteGoal(Goal g, Scheduler scheduler);
    
    /**
     * Add a new subgoal <code>g</code>.  <code>g</code> must be completed
     * before this goal is attempted.  <code>g</code> should be interned.
     * 
     * @throws CyclicDependencyException
     *             if a prerequisite of <code>g</code> is <code>this</code>
     */
    public void addPrerequisiteGoal(Goal g, Scheduler scheduler) throws CyclicDependencyException;
    
    /** Return true if this goal is reachable. */
    public boolean isReachable();

    /** Set a flag indicating that this rule is unreachable. */
    public void setUnreachable();

    /** Mark the goal as reached or not reached. */
    public void setUnreachableThisRun();
    public void setState(int state);
    public int state();
    
    /** Return true if this goal has been reached. */
    public boolean hasBeenReached();
    
    /** Get the job associated with this goal, or null. */
    public Job job();
    
    /** Get the name of the goal for debugging. */
    public String name();
}
