package polyglot.frontend.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.frontend.EmptyPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;

public class Validated extends SourceFileGoal {
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new Validated(job));
    }

    protected Validated(Job job) {
        super(job);
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new EmptyPass(this);
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.TypeChecked(job));
//        l.add(scheduler.ConstantsChecked(job));
        l.add(scheduler.ReachabilityChecked(job));
        l.add(scheduler.ExceptionsChecked(job));
        l.add(scheduler.ExitPathsChecked(job));
        l.add(scheduler.InitializationsChecked(job));
        l.add(scheduler.ConstructorCallsChecked(job));
        l.add(scheduler.ForwardReferencesChecked(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
