/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend.goals;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.util.StringUtil;

/**
 * An <code>AbstractGoal</code> is the base class of most <code>Goal</code>
 * implementations.
 *
 * @author nystrom
 */
public abstract class AbstractGoal implements Goal {
    protected Job job;
    protected String name;
    protected int state;
    protected Set<Goal> corequisites;
    protected Set<Goal> prerequisites;

    private AbstractGoal() {
        this.state = UNREACHED;
        this.prerequisites = Collections.<Goal> emptySet();
        this.corequisites = Collections.<Goal> emptySet();
    }

    protected AbstractGoal(Job job) {
        this();
        this.job = job;
        this.name = StringUtil.getShortNameComponent(getClass().getName());
    }

    protected AbstractGoal(Job job, String name) {
        this();
        this.job = job;
        this.name = name;
    }

    /**
     * Return true if this goal conflicts with the other; that is passes running
     * over both goals could access the same data.
     */
    @Override
    public boolean conflictsWith(Goal goal) {
        return job() != null && job() == goal.job();
    }

    /** Creates a pass to attempt to satisfy the goal. */
    @Override
    public abstract Pass createPass(ExtensionInfo extInfo);

    @Override
    public String name() {
        return name;
    }

    @Override
    public Job job() {
        return job;
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        return prerequisites;
    }

    @Override
    public Collection<Goal> corequisiteGoals(Scheduler scheduler) {
        return corequisites;
    }

    @Override
    public void addPrerequisiteGoal(Goal g, Scheduler scheduler)
            throws CyclicDependencyException {
        // This takes a hell of a long time.  Disable the check for now.
        // checkCycles(g, scheduler);
        if (prerequisites == Collections.EMPTY_SET) {
            prerequisites = new LinkedHashSet<Goal>();
        }
        prerequisites.add(g);
    }

    protected void checkCycles(Goal current, Scheduler scheduler)
            throws CyclicDependencyException {
        if (this == current) {
            throw new CyclicDependencyException("Goal " + this
                    + " cannot depend on itself.");
        }

        for (Goal subgoal : current.prerequisiteGoals(scheduler)) {
            checkCycles(subgoal, scheduler);
        }
    }

    @Override
    public void addCorequisiteGoal(Goal g, Scheduler scheduler) {
        if (corequisites == Collections.EMPTY_SET) {
            corequisites = new LinkedHashSet<Goal>();
        }
        corequisites.add(g);
    }

    /** Mark the goal as reached or not reached. */
    @Override
    public void setUnreachableThisRun() {
        setState(UNREACHABLE_THIS_RUN);
    }

    @Override
    public int state() {
        return state;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public boolean hasBeenReached() {
        return state == REACHED;
    }

    @Override
    public void setUnreachable() {
        setState(UNREACHABLE);
    }

    @Override
    public boolean isReachable() {
        return this.state != UNREACHABLE;
    }

    @Override
    public int hashCode() {
        return (job != null ? job.hashCode() : 0) + name.hashCode();
    }

    @Override
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

    protected String stateString() {
        switch (state) {
        case UNREACHABLE:
            return "unreachable";
        case UNREACHABLE_THIS_RUN:
            return "running-but-unreachable-this-run";
        case UNREACHED:
            return "unreached";
        case ATTEMPTED:
            return "attempted";
        case REACHED:
            return "reached";
        case RUNNING:
            return "running";
        }
        return "unknown-goal-state";
    }

    @Override
    public String toString() {
        return job + ":" + (job != null ? job.extensionInfo() + ":" : "")
                + name + " (" + stateString() + ")";
    }
}
