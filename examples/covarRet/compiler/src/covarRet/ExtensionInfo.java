package polyglot.ext.covarRet;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.ext.jl.*;

import java.util.*;
import java.io.*;

public class ExtensionInfo extends JLExtensionInfo {
    public String defaultFileExtension() {
	return "jl";
    }

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
    		
    		Goal g = internGoal(new VisitorGoal(job, new CovarRetRewriter(job, ts, nf)) {
    			public Collection prerequisiteGoals(Scheduler scheduler) {
                    List l = new ArrayList();
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
    	
    	public Goal Serialized(final Job job) {
    		Goal g = internGoal(new Serialized(job) {
    			public Collection prerequisiteGoals(Scheduler scheduler) {
                    List l = new ArrayList();
                    l.addAll(super.prerequisiteGoals(scheduler));
                    l.add(CovarRetRewrite(job));
                    return l;
    			}
    		});
    		
    		return g;
    	}
    }
}
