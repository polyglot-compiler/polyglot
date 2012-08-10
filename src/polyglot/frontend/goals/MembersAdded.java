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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.frontend.AbstractPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.SchedulerException;
import polyglot.frontend.passes.AddMembersPass;
import polyglot.types.ParsedClassType;

/**
 * Comment for <code>MembersAdded</code>
 *
 * @author nystrom
 */
public class MembersAdded extends ClassTypeGoal {
    public static Goal create(Scheduler scheduler, ParsedClassType ct) {
        return scheduler.internGoal(new MembersAdded(ct));
    }

    protected MembersAdded(ParsedClassType ct) {
        super(ct);
    }

    protected static class MembersAddedPass extends AbstractPass {
        MembersAddedPass(Goal goal) {
            super(goal);
        }

        @Override
        public boolean run() {
            MembersAdded goal = (MembersAdded) this.goal;
            if (!goal.type().membersAdded()) {
                throw new SchedulerException();
            }
            return true;
        }
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            return new MembersAddedPass(this);
        }
        return new AddMembersPass(extInfo.scheduler(), this);
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<Goal>();
        if (ct.job() != null) {
            l.add(scheduler.Parsed(ct.job()));
        }
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    @Override
    public Collection<Goal> corequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<Goal>();
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MembersAdded && super.equals(o);
    }
}
