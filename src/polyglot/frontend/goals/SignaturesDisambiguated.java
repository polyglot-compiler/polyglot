/*
 * Disambiguated.java
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
import polyglot.visit.AmbiguityRemover;

public class SignaturesDisambiguated extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new SignaturesDisambiguated(job, ts, nf));
    }

    protected SignaturesDisambiguated(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new AmbiguityRemover(job, ts, nf, true, false));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.ImportTableInitialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
