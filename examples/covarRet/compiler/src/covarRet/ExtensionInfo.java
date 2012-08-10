package covarRet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.Serialized;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.TypeSystem;

public class ExtensionInfo extends JLExtensionInfo {
    @Override
    public String defaultFileExtension() {
        return "jl";
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new CovarRetTypeSystem();
    }

//    public static final Pass.ID COVAR_RET_CAST_REWRITE = new Pass.ID("covariantReturnCasts");
//    public List passes(Job job) {
//        List l = super.passes(job);
//        beforePass(l, Pass.PRE_OUTPUT_ALL,
//                  new VisitorPass(COVAR_RET_CAST_REWRITE,
//                                  job, new CovarRetRewriter(job, ts, nf)));
//        return l;
//    }
    @Override
    public Scheduler createScheduler() {
        return new CovarRetScheduler(this);
    }

    static class CovarRetScheduler extends JLScheduler {
        public CovarRetScheduler(ExtensionInfo extInfo) {
            super(extInfo);
        }

        public Goal CovarRetRewrite(final Job job) {
            TypeSystem ts = job.extensionInfo().typeSystem();
            NodeFactory nf = job.extensionInfo().nodeFactory();

            Goal g =
                    internGoal(new VisitorGoal(job, new CovarRetRewriter(job,
                                                                         ts,
                                                                         nf)) {
                        @Override
                        public Collection<Goal> prerequisiteGoals(
                                Scheduler scheduler) {
                            List<Goal> l = new ArrayList<Goal>();
                            l.addAll(super.prerequisiteGoals(scheduler));
                            l.add(scheduler.TypeChecked(job));
                            l.add(scheduler.ConstantsChecked(job));
                            l.add(scheduler.ReachabilityChecked(job));
                            l.add(scheduler.ExceptionsChecked(job));
                            l.add(scheduler.ExitPathsChecked(job));
                            l.add(scheduler.InitializationsChecked(job));
                            l.add(scheduler.ConstructorCallsChecked(job));
                            l.add(scheduler.ForwardReferencesChecked(job));
                            return l;
                        }
                    });

            return g;
        }

        @Override
        public Goal Serialized(final Job job) {
            Goal g = internGoal(new Serialized(job) {
                @Override
                public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
                    List<Goal> l = new ArrayList<Goal>();
                    l.addAll(super.prerequisiteGoals(scheduler));
                    l.add(CovarRetRewrite(job));
                    return l;
                }
            });

            return g;
        }
    }
}
