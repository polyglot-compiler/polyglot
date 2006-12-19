/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * Scheduler.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend;

import java.util.*;

import polyglot.ast.Node;
import polyglot.frontend.goals.*;
import polyglot.main.Report;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
import polyglot.util.*;
import polyglot.visit.*;


/**
 * The <code>Scheduler</code> manages <code>Goal</code>s and runs
 * <code>Pass</code>es.
 * 
 * The basic idea is to have the scheduler try to satisfy goals.
 * To reach a goal, a pass is run.  The pass could modify an AST or it
 * could, for example, initialize the members of a class loaded from a
 * class file.  Passes may be rerun if a goal is not reached.  Goals are
 * processed via a worklist.  A goal may have <i>prerequisite</i>
 * dependencies and <i>corequisite</i> dependencies.  All prerequisites
 * must be reached before the goal is attempted.  A corequisite may be
 * reached while satisfying the goal itself, or vice versa.
 *
 * Recursive passes are not allowed.  If a goal cannot be reached a
 * SchedulerException (or more usually the subclass
 * MissingDependencyException) is thrown.  The scheduler catches the
 * exception and adds the goal back onto the worklist and adds the
 * missing dependency, if any, to the dependency graph.  Optionally, a
 * pass may catch the exception, but it must mark the goal as unreachable
 * on this run so that it will be added back to the worklist; the pass
 * must also add any missing dependencies.
 *
 * @author nystrom
 */
public abstract class Scheduler {
    protected ExtensionInfo extInfo;
    
    /**
     * Collection of uncompleted goals.
     */
    protected Set inWorklist;
    protected LinkedList worklist;
    
    /**
     * A map from <code>Source</code>s to <code>Job</code>s or to
     * the <code>COMPLETED_JOB</code> object if the Job previously
     * existed
     * but has now finished. The map contains entries for all
     * <code>Source</code>s that have had <code>Job</code>s added for them.
     */
    protected Map jobs;
    
    protected Collection commandLineJobs;

    /** Map from goals to goals used to intern goals. */
    protected Map goals;
    
    /** Map from goals to number of times a pass was run for the goal. */
    protected Map runCount;
    
    /** True if any pass has failed. */
    protected boolean failed;

    protected static final Object COMPLETED_JOB = "COMPLETED JOB";

    /** The currently running pass, or null if no pass is running. */
    protected Pass currentPass;
    
    public Scheduler(ExtensionInfo extInfo) {
        this.extInfo = extInfo;

        this.jobs = new HashMap();
        this.goals = new HashMap();
        this.runCount = new HashMap();
        this.inWorklist = new HashSet();
        this.worklist = new LinkedList();
        this.currentPass = null;
    }
    
    public Collection commandLineJobs() {
        return this.commandLineJobs;
    }
    
    public void setCommandLineJobs(Collection c) {
        this.commandLineJobs = Collections.unmodifiableCollection(c);
    }
    
    public boolean prerequisiteDependsOn(Goal goal, Goal subgoal) {
        if (goal == subgoal) {
            return true;
        }

        for (Iterator i = goal.prerequisiteGoals(this).iterator(); i.hasNext();) {
            Goal g = (Goal) i.next();
            if (prerequisiteDependsOn(g, subgoal)) {
                return true;
            }
        }
        return false;
    }
        
    /**
     * Add a new corequisite <code>subgoal</code> of the <code>goal</code>.
     * <code>subgoal</code> is a goal on which <code>goal</code> mutually
     * depends. The caller must be careful to ensure that all corequisite goals
     * can be eventually reached.
     */
    public void addCorequisiteDependency(Goal goal, Goal subgoal) {
        if (! goal.corequisiteGoals(this).contains(subgoal)) {
            if (Report.should_report(Report.frontend, 3) || Report.should_report("deps", 1))
                Report.report(3, "Adding coreq edge: " + subgoal + " -> " + goal);
            goal.addCorequisiteGoal(subgoal, this);
        }
    }
    
    public void addCorequisiteDependencyAndEnqueue(Goal goal, Goal subgoal) {
        addCorequisiteDependency(goal, subgoal);
        // addGoal(subgoal);
    }

    public void addDependencyAndEnqueue(Goal goal, Goal subgoal, boolean prerequisite) {
        if (prerequisite) {
            try {
                addPrerequisiteDependency(goal, subgoal);
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e);
            }
        }
        else {
            addCorequisiteDependency(goal, subgoal);
        }
        // addGoal(subgoal);
    }

    /**
     * Add a new <code>subgoal</code> of <code>goal</code>.
     * <code>subgoal</code> must be completed before <code>goal</code> is
     * attempted.
     * 
     * @throws CyclicDependencyException
     *             if a prerequisite of <code>subgoal</code> is
     *             <code>goal</code>
     */
    public void addPrerequisiteDependency(Goal goal, Goal subgoal) throws CyclicDependencyException {
        if (! goal.prerequisiteGoals(this).contains(subgoal)) {
            if (Report.should_report(Report.frontend, 3) || Report.should_report("deps", 1))
                Report.report(3, "Adding prereq edge: " + subgoal + " => " + goal);
            goal.addPrerequisiteGoal(subgoal, this);
        }
    }
    
    /** Add prerequisite dependencies between adjacent items in a list of goals. */
    public void addPrerequisiteDependencyChain(List deps) throws CyclicDependencyException {
        Goal prev = null;
        for (Iterator i = deps.iterator(); i.hasNext(); ) {
            Goal curr = (Goal) i.next();
            if (prev != null)
                addPrerequisiteDependency(curr, prev);
            prev = curr;
        }
    }
    
    /**
     * Intern the <code>goal</code> so that there is only one copy of the goal.
     * All goals passed into and returned by scheduler should be interned.
     * @param goal
     * @return the interned copy of <code>goal</code>
     */
    public synchronized Goal internGoal(Goal goal) {
        Goal g = (Goal) goals.get(goal);
        if (g == null) {
            g = goal;
            goals.put(g, g);
            if (Report.should_report(Report.frontend, 4))
                Report.report(4, "new goal " + g);
            if (Report.should_report(Report.frontend, 5))
                Report.report(5, "goals = " + goals.keySet());
        }
        return g;   
    }

    /** Add <code>goal</code> to the worklist. */
    public void addGoal(Goal goal) {
        if (! inWorklist.contains(goal)) {
            inWorklist.add(goal);
            worklist.add(goal);
        }
    }
    
    /*
    // Dummy pass needed for currentGoal(), currentPass(), etc., to work
    // when checking if a goal was reached.
    Pass schedulerPass(Goal g) {
        return new EmptyPass(g);
    }
    */
    
    public boolean reached(Goal g) {
        return g.hasBeenReached();
    }

    protected void completeJob(Job job) {
        if (job != null) {
            jobs.put(job.source(), COMPLETED_JOB);
            if (Report.should_report(Report.frontend, 1)) {
                Report.report(1, "Completed job " + job);
            }
        }
    }

    protected List worklist() {
        return worklist;
    }

    protected static class TheEndGoal extends AbstractGoal {
        protected Scheduler scheduler;

        protected TheEndGoal(Scheduler scheduler) {
            super(null);
            this.scheduler = scheduler;
        }

        public Collection prerequisiteGoals(Scheduler scheduler) {
            return scheduler.worklist();
        }

        public String toString() {
            return "TheEnd(" + scheduler.getClass().getName() + ")";
        }

        protected Collection goals() {
            return scheduler.worklist();
        }

        public Pass createPass(ExtensionInfo extInfo) {
            return new EndPass(this);
        }

        protected static class EndPass extends AbstractPass {
            protected EndPass(TheEndGoal g) {
                super(g);
            }

            public boolean run() {
                TheEndGoal end = (TheEndGoal) goal();

                for (Iterator i = end.goals().iterator(); i.hasNext(); ) {
                    Goal goal = (Goal) i.next();

                    if (! goal.hasBeenReached()) {
                        throw new MissingDependencyException(goal, true);
                    }
                }

                return true;
            }
        }
        
        public int hashCode() {
            return Boolean.TRUE.hashCode();
        }

        public boolean equals(Object o) {
            return o instanceof TheEndGoal;
        }
    }

    /**
     * Attempt to complete all goals in the worklist (and any subgoals they
     * have). This method returns <code>true</code> if all passes were
     * successfully run and all goals in the worklist were reached. The worklist
     * should be empty at return.
     */ 
    public boolean runToCompletion() {
        Goal theEnd = internGoal(new TheEndGoal(this));

        boolean okay = true;

        while (okay && ! reached(theEnd)) {
            okay = attemptGoal(theEnd);
        }

        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Finished all passes for " + this.getClass().getName() + " -- " +
                        (okay ? "okay" : "failed"));

        return okay;
    }
    
    /**         
     * Load a source file and create a job for it.  Optionally add a goal
     * to compile the job to Java.
     * 
     * @param source The source file to load.
     * @param compile True if the compile goal should be added for the new job.
     * @return The new job or null if the job has already completed.
     */         
    public Job loadSource(FileSource source, boolean compile) {
        // Add a new Job for the given source. If a Job for the source
        // already exists, then we will be given the existing job.
        Job job = addJob(source);

        if (job == null) {
            // addJob returns null if the job has already been completed, in
            // which case we can just ignore the request to read in the
            // source.
            return null;
        }               
        
        // Create a goal for the job; this will set up dependencies for
        // the goal, even if the goal isn't to be added to the work list.
        Goal compileGoal = extInfo.getCompileGoal(job);

        if (compile) {
            // Now, add a goal for compiling the source.
            addGoal(compileGoal);
        }
        
        return job;
    }
    
    public boolean sourceHasJob(Source s) {
        return jobs.get(s) != null;
    }
    
    public Job currentJob() {
        return currentPass != null ? currentPass.goal().job() : null;
    }
    
    public Pass currentPass() {
        return currentPass;
    }
    
    public Goal currentGoal() {
        return currentPass != null ? currentPass.goal() : null;
    }

    /**
     * Run a passes until the <code>goal</code> is attempted. Callers should
     * check goal.completed() and should be able to handle the goal not being
     * reached.
     * 
     * @return false if there was an error trying to reach the goal; true if
     *         there was no error, even if the goal was not reached.
     */ 
    public boolean attemptGoal(Goal goal) {
        return attemptGoal(goal, new HashSet());
    }

    protected boolean attemptGoal(Goal goal, Set above) {
        if (Report.should_report("dump-dep-graph", 2))
            dumpInFlightDependenceGraph();

        if (Report.should_report(Report.frontend, 2))
            Report.report(2, "Running to goal " + goal);
        
        if (Report.should_report(Report.frontend, 4)) {
            Report.report(4, "  Reachable = " + goal.isReachable());
            Report.report(4, "  Prerequisites for " + goal + " = " + goal.prerequisiteGoals(this));
            Report.report(4, "  Corequisites for " + goal + " = " + goal.corequisiteGoals(this));
        }

        if (above.contains(goal)) {
            if (Report.should_report(Report.frontend, 4))
                Report.report(4, goal + " is being attempted by a caller; returning");
            return true;
        }

        boolean progress = true;
    
        Set newAbove = new HashSet();
        newAbove.addAll(above);
        newAbove.add(goal);

        // Loop over the goal and its coreqs as long as progress is made.
        while (progress && ! reached(goal)) {
            progress = false;

            if (Report.should_report(Report.frontend, 4))
                Report.report(4, "outer loop for " + goal);

            // Run the prereqs of the goal.
            List prereqs = new ArrayList(goal.prerequisiteGoals(this));

            for (Iterator j = prereqs.iterator(); j.hasNext(); ) {
                Goal subgoal = (Goal) j.next();

                if (reached(subgoal)) {
                    continue;
                }

                if (Report.should_report(Report.frontend, 4))
                    Report.report(4, "running prereq: " + subgoal + "->" + goal);

                if (! attemptGoal(subgoal, newAbove)) {
                    return false;
                }

                if (reached(goal)) {
                    return true;
                }
            }

            // Make sure all prerequisite subgoals have been completed.
            // If any has not, just return.
            boolean runPass = true;

            for (Iterator j = goal.prerequisiteGoals(this).iterator(); j.hasNext(); ) {
                Goal subgoal = (Goal) j.next();
                if (! reached(subgoal)) {
                    runPass = false;
                }
            }

            if (! runPass) {
                return true;
            }

            // Now, run the goal itself.
            if (Report.should_report(Report.frontend, 4))
                Report.report(4, "running goal " + goal);
            
            boolean result = runGoal(goal);
            
            if (! result) {
                return false;
            }
            
            if (reached(goal)) {
                if (goal instanceof EndGoal) {
                    // The job has finished.  Let's remove it from the map
                    // so it can be garbage collected, and free up the AST.
                    completeJob(goal.job());
                }
                return true;
            }

            // If the goal was not reached, run the coreqs of the goal. 
            List coreqs = new ArrayList(goal.corequisiteGoals(this));

            for (Iterator j = coreqs.iterator(); j.hasNext(); ) {
                Goal subgoal = (Goal) j.next();

                if (reached(subgoal)) {
                    continue;
                }

                if (Report.should_report(Report.frontend, 4))
                    Report.report(4, "running coreq: " + subgoal + "->" + goal);

                if (! attemptGoal(subgoal, newAbove)) {
                    return false;
                }

                if (reached(subgoal)) {
                    progress = true;
                }

                if (reached(goal)) {
                    return true;
                }
            }
        }
        
        return true;
    }

    protected boolean runGoal(Goal goal) {
        if (reached(goal)) {
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "Already reached goal " + goal);
            return true;
        }
        
        if (! goal.isReachable()) {
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "Cannot reach goal " + goal);
            return false;
        }
      
        Pass pass = goal.createPass(extInfo);
        return runPass(pass);
    }
   
    /**         
     * Run the pass <code>pass</code>.  All subgoals of the pass's goal
     * required to start the pass should be satisfied.  Running the pass
     * may not satisfy the goal, forcing it to be retried later with new
     * subgoals.
     */
    protected boolean runPass(Pass pass) {
        Goal goal = pass.goal();
        Job job = goal.job();
                
        if (extInfo.getOptions().disable_passes.contains(pass.name())) {
            if (Report.should_report(Report.frontend, 1))
                Report.report(1, "Skipping pass " + pass);
            
            goal.setState(Goal.REACHED);
            return true;
        }
        
        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Running pass " + pass + " for " + goal);

        if (reached(goal)) {
            throw new InternalCompilerError("Cannot run a pass for completed goal " + goal);
        }
        
        Integer countObj = (Integer) this.runCount.get(goal);
        int count = countObj != null ? countObj.intValue() : 0;
        count++;
        this.runCount.put(goal, new Integer(count));

        
        if (count >= MAX_RUN_COUNT) {
            String[] suffix = new String[] { "th", "st", "nd", "rd" };
            int index = count % 10;
            if (index > 3) index = 0;
            if (11 <= count && count <= 13) index = 0;
            String cardinal = count + suffix[index];
            String message = "Possible infinite loop detected trying to run a pass for " + goal + " for the " + cardinal + " time.";
        
            // Report the infinite loop.
            ErrorQueue eq = extInfo.compiler().errorQueue();

            // Go for one last loop with reporting enabled.
            if (goal.equals(infiniteLoopGoal)) {
                // We've gone around the loop once, abort the compiler.

                if (Report.should_report("dump-dep-graph", 1))
                    dumpInFlightDependenceGraph();
                if (Report.should_report("dump-dep-graph", 1))
                    dumpDependenceGraph();
            
                eq.enqueue(ErrorInfo.INTERNAL_ERROR, message + "  Aborting.");
                System.exit(1);
            }
            else if (infiniteLoopGoal == null) {
                infiniteLoopGoal = goal;
                
                // Enable reporting.
                Report.addTopic(Report.frontend, 4);
                Report.addTopic("deps", 1);
                
                eq.enqueue(ErrorInfo.DEBUG, message + "  The compiler will attempt the goal one more time with reporting enabled, then abort.");
            }
        }
        
        pass.resetTimers();

        boolean result = false;

        if (job == null || job.status()) {
            Pass oldPass = this.currentPass;
            this.currentPass = pass;
            Report.should_report.push(pass.name());

            // Stop the timer on the old pass. */
            if (oldPass != null) {
                oldPass.toggleTimers(true);
            }

            if (job != null) {
                job.setRunningPass(pass);
            }
            
            pass.toggleTimers(false);

            goal.setState(Goal.RUNNING);

            long t = System.currentTimeMillis();
            String key = goal.toString();

            extInfo.getStats().accumPassTimes(key + " attempts", 1, 1);
            extInfo.getStats().accumPassTimes("total goal attempts", 1, 1);
            
            try {
                result = pass.run();

                if (! result) {
                    extInfo.getStats().accumPassTimes(key + " failures", 1, 1);
                    extInfo.getStats().accumPassTimes("total goal failures", 1, 1);

                    goal.setState(Goal.UNREACHABLE);
                    if (Report.should_report(Report.frontend, 1))
                        Report.report(1, "Failed pass " + pass + " for " + goal);
                }
                else {
                    if (goal.state() == Goal.RUNNING) {
                        extInfo.getStats().accumPassTimes(key + " reached", 1, 1);
                        extInfo.getStats().accumPassTimes("total goal reached", 1, 1);

                        goal.setState(Goal.REACHED);
                        if (Report.should_report(Report.frontend, 1))
                            Report.report(1, "Completed pass " + pass + " for " + goal);
                    }
                    else {
                        extInfo.getStats().accumPassTimes(key + " unreached", 1, 1);
                        extInfo.getStats().accumPassTimes("total goal unreached", 1, 1);

                        goal.setState(Goal.ATTEMPTED);                    
                        if (Report.should_report(Report.frontend, 1))
                            Report.report(1, "Completed (unreached) pass " + pass + " for " + goal);
                    }
                }
            }
            catch (MissingDependencyException e) {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Did not complete pass " + pass + " for " + goal + " (missing " + e.goal() + ")");

                if (Report.should_report(Report.frontend, 3))
                    e.printStackTrace();
                
                extInfo.getStats().accumPassTimes(key + " aborts", 1, 1);
                extInfo.getStats().accumPassTimes("total goal aborts", 1, 1);

                addDependencyAndEnqueue(goal, e.goal(), e.prerequisite());
                
                goal.setState(Goal.ATTEMPTED);
                result = true;
            }
            catch (SchedulerException e) {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Did not complete pass " + pass + " for " + goal);

                extInfo.getStats().accumPassTimes(key + " aborts", 1, 1);
                extInfo.getStats().accumPassTimes("goal aborts", 1, 1);
                
                goal.setState(Goal.ATTEMPTED);
                result = true;
            }
            
            t = System.currentTimeMillis() - t;
            extInfo.getStats().accumPassTimes(key, t, t);
            
            pass.toggleTimers(false);
            
            if (job != null) {
                job.setRunningPass(null);
            }

            Report.should_report.pop();
            this.currentPass = oldPass;

            // Restart the timer on the old pass. */
            if (oldPass != null) {
                oldPass.toggleTimers(true);
            }

            // pretty-print this pass if we need to.
            if (job != null && extInfo.getOptions().print_ast.contains(pass.name())) {
                System.err.println("--------------------------------" +
                                   "--------------------------------");
                System.err.println("Pretty-printing AST for " + job +
                                   " after " + pass.name());

                job.ast().prettyPrint(System.err);
            }

            // dump this pass if we need to.
            if (job != null && extInfo.getOptions().dump_ast.contains(pass.name())) {
                System.err.println("--------------------------------" +
                                   "--------------------------------");
                System.err.println("Dumping AST for " + job +
                                   " after " + pass.name());
                
                job.ast().dump(System.err);
            }

            // This seems to work around a VM bug on linux with JDK
            // 1.4.0.  The mark-sweep collector will sometimes crash.
            // Running the GC explicitly here makes the bug go away.
            // If this fails, maybe run with bigger heap.
            
            // System.gc();
        }   
            
        Stats stats = extInfo.getStats();
        stats.accumPassTimes(pass.name(), pass.inclusiveTime(),
                             pass.exclusiveTime());

        if (! result) {
            failed = true;
        }
        
        // Record the progress made before running the pass and then update
        // the current progress.
        if (Report.should_report(Report.time, 2)) {
            Report.report(2, "Finished " + pass +
                          " status=" + statusString(result) + " inclusive_time=" +
                          pass.inclusiveTime() + " exclusive_time=" +
                          pass.exclusiveTime());
        }
        else if (Report.should_report(Report.frontend, 1)) {
            Report.report(1, "Finished " + pass +
                          " status=" + statusString(result));
        }
        
        if (job != null) {
            job.updateStatus(result);
        }
                
        return result;             
    }           
                                   
    protected static String statusString(boolean okay) {
        if (okay) {
            return "done";
        }
        else {
            return "failed";
        }
    }
    
    public abstract Goal TypeExists(String name);
    public abstract Goal MembersAdded(ParsedClassType ct);
    public abstract Goal SupertypesResolved(ParsedClassType ct);
    public abstract Goal SignaturesResolved(ParsedClassType ct);
    public abstract Goal FieldConstantsChecked(FieldInstance fi);
    public abstract Goal Parsed(Job job);
    public abstract Goal TypesInitialized(Job job);
    public abstract Goal TypesInitializedForCommandLine();
    public abstract Goal ImportTableInitialized(final Job job);
    public abstract Goal SignaturesDisambiguated(Job job);
    public abstract Goal SupertypesDisambiguated(Job job);
    public abstract Goal Disambiguated(Job job);
    public abstract Goal TypeChecked(Job job);
    public abstract Goal ConstantsChecked(Job job);
    public abstract Goal ReachabilityChecked(Job job);
    public abstract Goal ExceptionsChecked(Job job);
    public abstract Goal ExitPathsChecked(Job job);
    public abstract Goal InitializationsChecked(Job job);
    public abstract Goal ConstructorCallsChecked(Job job);
    public abstract Goal ForwardReferencesChecked(Job job);
    public abstract Goal Serialized(Job job);
    public abstract Goal CodeGenerated(Job job);
    
    /** Return all compilation units currently being compiled. */
    public Collection jobs() {
        ArrayList l = new ArrayList(jobs.size());
        
        for (Iterator i = jobs.values().iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o != COMPLETED_JOB) {
                l.add(o);
            }
        }
        
        return l;
    }

    /**
     * Add a new <code>Job</code> for the <code>Source source</code>.
     * A new job will be created if
     * needed. If the <code>Source source</code> has already been processed,
     * and its job discarded to release resources, then <code>null</code>
     * will be returned.
     */
    public Job addJob(Source source) {
        return addJob(source, null);
    }

    /**
     * Add a new <code>Job</code> for the <code>Source source</code>,
     * with AST <code>ast</code>.
     * A new job will be created if
     * needed. If the <code>Source source</code> has already been processed,
     * and its job discarded to release resources, then <code>null</code>
     * will be returned.
     */
    public Job addJob(Source source, Node ast) {
        Object o = jobs.get(source);
        Job job = null;
        
        if (o == COMPLETED_JOB) {
            // the job has already been completed.
            // We don't need to add a job
            return null;
        }
        else if (o == null) {
            // No appropriate job yet exists, we will create one.
            
            job = this.createSourceJob(source, ast);

            // record the job in the map and the worklist.
            jobs.put(source, job);
    
            if (Report.should_report(Report.frontend, 4)) {
                Report.report(4, "Adding job for " + source + " at the " +
                    "request of pass " + currentPass);
            }
        }
        else {
            job = (Job) o;
        }
    
        return job;
    }

    /**
     * Create a new <code>Job</code> for the given source and AST.
     * In general, this method should only be called by <code>addJob</code>.
     */
    protected Job createSourceJob(Source source, Node ast) {
        return new Job(extInfo, extInfo.jobExt(), source, ast);
    }

    public String toString() {
        return getClass().getName() + " worklist=" + worklist;
    }   

    protected static int dumpCounter = 0;

    protected static final int MAX_RUN_COUNT = 200;
    protected Goal infiniteLoopGoal = null;
    
    public boolean inInfiniteLoop() {
        return infiniteLoopGoal != null;
    }
    
    /**
     * Dump the dependence graph to a DOT file.
     */
    protected void dumpDependenceGraph() {
        String name = "FullDepGraph";
        name += dumpCounter++;

        String rootName = "";

        Report.report(2, "digraph " + name + " {");
        Report.report(2, "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        for (Iterator i = new ArrayList(goals.keySet()).iterator(); i.hasNext(); ) {
            Goal g = (Goal) i.next();
            g = internGoal(g);
            
            int h = System.identityHashCode(g);
            
            // dump out this node
            Report.report(2,
                          h + " [ label = \"" +
                          StringUtil.escape(g.toString()) + "\" ];");
            
            // dump out the successors.
            for (Iterator j = new ArrayList(g.prerequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h + " [style=bold]");
            }
            
            for (Iterator j = new ArrayList(g.corequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h);
            }
        }
        
        Report.report(2, "}");
    }

    /**
     * Dump the dependence graph to a DOT file.
     */
    protected void dumpInFlightDependenceGraph() {
        String name = "InFlightDepGraph";
        name += dumpCounter++;
    
        String rootName = "";
    
        Report.report(2, "digraph " + name + " {");
        Report.report(2, "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        Set print = new HashSet();
    
        for (Iterator i = new ArrayList(goals.keySet()).iterator(); i.hasNext(); ) {
            Goal g = (Goal) i.next();
            g = internGoal(g);
            
            if (g.state() == Goal.REACHED || g.state() == Goal.UNREACHED || g.state() == Goal.UNREACHABLE) {
                continue;
            }

            print.add(g);

            for (Iterator j = new ArrayList(g.prerequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                print.add(g2);
            }

            for (Iterator j = new ArrayList(g.corequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                print.add(g2);
            }
        }

        for (Iterator i = print.iterator(); i.hasNext(); ) {
            Goal g = (Goal) i.next();
            g = internGoal(g);

            int h = System.identityHashCode(g);
            
            // dump out this node
            Report.report(2,
                          h + " [ label = \"" +
                          StringUtil.escape(g.toString()) + "\" ];");
            
            // dump out the successors.
            for (Iterator j = new ArrayList(g.prerequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                if (! print.contains(g2))
                    continue;
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h + " [style=bold]");
            }
            
            for (Iterator j = new ArrayList(g.corequisiteGoals(this)).iterator(); j.hasNext(); ) {
                Goal g2 = (Goal) j.next();
                g2 = internGoal(g2);
                if (! print.contains(g2))
                    continue;
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h);
            }
        }
        
        Report.report(2, "}");
    }

    /**
     * Dump the dependence graph to a DOT file.
     */
    protected void dumpDependenceGraph(Goal g) {
        String name = "DepGraph";
        name += dumpCounter++;

        String rootName = "";

        Report.report(2, "digraph " + name + " {");
        Report.report(2, "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        g = internGoal(g);
        
        int h = System.identityHashCode(g);
        
        // dump out this node
        Report.report(2,
                      h + " [ label = \"" +
                      StringUtil.escape(g.toString()) + "\" ];");
        
        Set seen = new HashSet();
        seen.add(new Integer(h));
        
        // dump out the successors.
        for (Iterator j = new ArrayList(g.prerequisiteGoals(this)).iterator(); j.hasNext(); ) {
            Goal g2 = (Goal) j.next();
            g2 = internGoal(g2);
            int h2 = System.identityHashCode(g2);
            if (! seen.contains(new Integer(h2))) {
                seen.add(new Integer(h2));
                Report.report(2,
                              h2 + " [ label = \"" +
                              StringUtil.escape(g2.toString()) + "\" ];");
            }        
            Report.report(2, h2 + " -> " + h + " [style=bold]");
        }
        
        for (Iterator j = new ArrayList(g.corequisiteGoals(this)).iterator(); j.hasNext(); ) {
            Goal g2 = (Goal) j.next();
            g2 = internGoal(g2);
            int h2 = System.identityHashCode(g2);
            if (! seen.contains(new Integer(h2))) {
                seen.add(new Integer(h2));
                Report.report(2,
                              h2 + " [ label = \"" +
                              StringUtil.escape(g2.toString()) + "\" ];");
            }        
            Report.report(2, h2 + " -> " + h);
        }
        
        Report.report(2, "}");
    }
}


