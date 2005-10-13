/*
 * InitializationsChecked.java
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
import polyglot.visit.InitChecker;

public class InitializationsChecked extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new InitializationsChecked(job, ts, nf));
    }

    protected InitializationsChecked(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new InitChecker(job, ts, nf));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.ReachabilityChecked(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
