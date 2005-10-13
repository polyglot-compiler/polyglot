/*
 * ExceptionsChecked.java
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
import polyglot.visit.ExceptionChecker;

public class ExceptionsChecked extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new ExceptionsChecked(job, ts, nf));
    }

    protected ExceptionsChecked(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new ExceptionChecker(job, ts, nf));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.TypeChecked(job));
        l.add(scheduler.ReachabilityChecked(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
