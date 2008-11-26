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

import java.util.*;
import java.util.Collection;
import java.util.LinkedHashSet;

import polyglot.frontend.*;
import polyglot.util.InternalCompilerError;
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
    protected Set corequisites;
    protected Set prerequisites;

    private AbstractGoal() {
        this.state = UNREACHED;
        this.prerequisites = Collections.EMPTY_SET;
        this.corequisites = Collections.EMPTY_SET;
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
    public boolean conflictsWith(Goal goal) {
        return job() != null && job() == goal.job();
    }
   
    /** Creates a pass to attempt to satisfy the goal. */
    public abstract Pass createPass(ExtensionInfo extInfo);
    
    public String name() {
        return name;
    }

    public Job job() {
        return job;
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        return prerequisites;
    }
    
    public Collection corequisiteGoals(Scheduler scheduler) {
        return corequisites;
    }

    public void addPrerequisiteGoal(Goal g, Scheduler scheduler) throws CyclicDependencyException {
        // This takes a hell of a long time.  Disable the check for now.
        // checkCycles(g, scheduler);
        if (prerequisites == Collections.EMPTY_SET) {
            prerequisites = new LinkedHashSet();
        }
        prerequisites.add(g);
    }
    
    protected void checkCycles(Goal current, Scheduler scheduler) throws CyclicDependencyException {
        if (this == current) {
            throw new CyclicDependencyException("Goal " + this + " cannot depend on itself.");
        }
        
        for (Iterator i = current.prerequisiteGoals(scheduler).iterator(); i.hasNext(); ) {
            Goal subgoal = (Goal) i.next();
            checkCycles(subgoal, scheduler);
        }
    }
    
    public void addCorequisiteGoal(Goal g, Scheduler scheduler) {
        if (corequisites == Collections.EMPTY_SET) {
            corequisites = new LinkedHashSet();
        }
        corequisites.add(g);
    }

    /** Mark the goal as reached or not reached. */
    public void setUnreachableThisRun() {
        setState(UNREACHABLE_THIS_RUN);
    }
    
    public int state() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }

    public boolean hasBeenReached() {
        return state == REACHED;
    }
    
    public void setUnreachable() {
        setState(UNREACHABLE);
    }
    
    public boolean isReachable() {
        return this.state != UNREACHABLE;
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
    
    public String toString() {
        return job + ":" + (job != null ? job.extensionInfo() + ":" : "")
            + name + " (" + stateString() + ")";
    }
}
