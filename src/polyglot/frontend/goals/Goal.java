/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend.goals;

import java.util.Collection;

import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;

/**
 * A goal abstractly represents something that the compiler is supposed to
 * achieve by automatically scheduling passes.  Goals may depend on each other,
 * and a goal is not attempted until its prerequisites are achieved. Goals may
 * also be corequisites (mutually dependent).
 * 
 * Once all the prerequisites of a goal are reached, the goal's
 * {@code Pass} is run. It keeps trying until it is successful or
 * it fails and the compiler quits with an error.
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
    public Collection<Goal> corequisiteGoals(Scheduler scheduler);

    /**
     * Goals that must be completed before attempting this goal. The graph of
     * dependencies between prerequisite goals should be acyclic.
     */
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler);

    /**
     * Add a new corequisite subgoal {@code g}.  {@code g} is a
     * goal on which this goal mutually depends.  The caller must be careful
     * to ensure that all corequisite goals can be eventually reached.
     * {@code g} should be interned.
     */
    public void addCorequisiteGoal(Goal g, Scheduler scheduler);

    /**
     * Add a new subgoal {@code g}.  {@code g} must be completed
     * before this goal is attempted.  {@code g} should be interned.
     * 
     * @throws CyclicDependencyException
     *             if a prerequisite of {@code g} is {@code this}
     */
    public void addPrerequisiteGoal(Goal g, Scheduler scheduler)
            throws CyclicDependencyException;

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
