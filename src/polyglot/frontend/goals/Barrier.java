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

import polyglot.frontend.AbstractPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;

/**
 * A <code>Barrier</code> goal synchronizes all the jobs to reach the same goal. 
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

    public Collection<Job> jobs() {
        return scheduler.jobs();
    }

    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#createPass(polyglot.frontend.ExtensionInfo)
     */
    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new BarrierPass(scheduler, this);
    }

    protected static class BarrierPass extends AbstractPass {
        public Scheduler scheduler;

        protected BarrierPass(Scheduler scheduler, Barrier barrier) {
            super(barrier);
            this.scheduler = scheduler;
        }

        @Override
        public boolean run() {
            Barrier barrier = (Barrier) goal();
            for (Job job : barrier.jobs()) {
                Goal subgoal = barrier.goalForJob(job);
                if (!subgoal.hasBeenReached()) {
                    scheduler.addDependencyAndEnqueue(barrier, subgoal, true);
                    barrier.setUnreachableThisRun();
                }
            }
            return true;
        }
    }

    public abstract Goal goalForJob(Job job);

    @Override
    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return System.identityHashCode(this);
        }
        else {
            return name.hashCode();
        }
    }

    @Override
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
