/*
 * ConstantsChecked.java
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
import polyglot.visit.ConstantChecker;

public class ConstantsChecked extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new ConstantsChecked(job, ts, nf));
    }

    protected ConstantsChecked(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new ConstantChecker(job, ts, nf));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.Disambiguated(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.TypeChecked(job));
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }
}
