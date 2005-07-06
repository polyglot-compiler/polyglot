package polyglot.ext.jl.types;

import java.util.HashSet;
import java.util.Set;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

/**
 * A LazyClassInitializer is responsible for initializing members of a class
 * after it has been created. Members are initialized lazily to correctly handle
 * cyclic dependencies between classes.
 * 
 * SchedulerClassInitializer ensures that scheduler dependencies are enforced
 * when a ParsedClassType member is accessed.
 */
public class SchedulerClassInitializer implements LazyClassInitializer {
    protected TypeSystem ts;
    protected ParsedClassType ct;
    protected Scheduler scheduler;

    protected boolean superclassInitialized;
    protected boolean interfacesInitialized;
    protected boolean memberClassesInitialized;
    protected boolean constructorsInitialized;
    protected boolean methodsInitialized;
    protected boolean fieldsInitialized;

    public SchedulerClassInitializer(TypeSystem ts) {
        this.ts = ts;
        this.scheduler = ts.extensionInfo().scheduler();
    }
    
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

    public boolean fromClassFile() {
        return false;
    }

    Set requireVisited = new HashSet();

    protected boolean require(Goal g) {
        if (Report.should_report(Report.types, 4)) {
            Report.report(4, scheduler.currentGoal() + " requires " + g);
            Report.report(4, "ct.job = " + ct.job());
            Report.report(4, "sched.currentjob = " + scheduler.currentJob());
        }
        
        if (requireVisited.contains(g)) {
            return false;
        }

        requireVisited.add(g);
        
        if (scheduler.reached(g)) {
            if (Report.should_report("jxtypes", 4))
                Report.report(4, "    reached " + g);
            requireVisited.remove(g);
            return true;
        }

        requireVisited.remove(g);
        
        if (ct.job() != null && ct.job() == scheduler.currentJob()) {
            if (Report.should_report(Report.types, 4))
                Report.report(4, "    same job: allowing " + g + " to be unreached");
            if (Report.should_report(Report.types, 6))
                new Exception().printStackTrace();
            // If the goal can only be reached by running a pass over the same
            // AST, just return.  The pass is required to be robust against
            // unsatisfied dependencies within a given AST.

//          throw new InternalCompilerError("cannot require " + g + " by " + scheduler.currentGoal());
//          throw new UnavailableTypeException(ct);
            return false;
        }
        
        try {
            if (Report.should_report(Report.types, 4))
                Report.report(4, "    attempting " + g);
            boolean result = scheduler.attemptGoal(g);
            if (! result) {
                scheduler.currentGoal().setUnreachable();
            }
            return result;
        }
        catch (CyclicDependencyException e) {
            if (Report.should_report(Report.types, 4))
                Report.report(4, "    cyclic dependency: " + e.getMessage());
            scheduler.addConcurrentDependency(scheduler.currentGoal(), g);
            throw new UnavailableTypeException(ct);
        }
    }

    public void initSuperclass() {
        if (!superclassInitialized) {
            if (require(scheduler.SupertypesResolved(ct))) {
                this.superclassInitialized = true;
            }
        }
    }

    public void initInterfaces() {
        if (!interfacesInitialized) {
            if (require(scheduler.SupertypesResolved(ct))) {
                this.interfacesInitialized = true;
            }
        }
    }

    public void initMemberClasses() {
        if (!memberClassesInitialized) {
            if (require(scheduler.MembersAdded(ct))) {
                this.memberClassesInitialized = true;
            }
        }
    }

    public void initConstructors() {
        if (!constructorsInitialized) {
            if (require(scheduler.SignaturesResolved(ct))) {
                this.constructorsInitialized = true;
            }
        }
    }

    public void initMethods() {
        if (!methodsInitialized) {
            if (require(scheduler.SignaturesResolved(ct))) {
                this.methodsInitialized = true;
            }
        }
    }

    public void initFields() {
        if (!fieldsInitialized) {
            if (require(scheduler.SignaturesResolved(ct))) {
                this.fieldsInitialized = true;
            }
        }
    }
}
