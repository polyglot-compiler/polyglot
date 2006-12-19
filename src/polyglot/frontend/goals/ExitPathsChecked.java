/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * ExitPathsChecked.java
 * 
 * Author: nystrom
 * Creation date: Oct 11, 2005
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.types.TypeSystem;
import polyglot.visit.ExitChecker;

public class ExitPathsChecked extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new ExitPathsChecked(job, ts, nf));
    }

    protected ExitPathsChecked(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new ExitChecker(job, ts, nf));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.ReachabilityChecked(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
