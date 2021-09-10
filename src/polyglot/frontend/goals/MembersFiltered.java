package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.types.TypeSystem;
import polyglot.visit.MemberFilterer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** See {@link MemberFilterer}. */
public class MembersFiltered extends VisitorGoal {

    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new MembersFiltered(job, ts, nf));
    }

    protected MembersFiltered(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new MemberFilterer(job, ts, nf));
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.TypesInitialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
