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
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.visit.*;


/**
 * Comment for <code>Scheduler</code>
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

    /** Map from goals to goals used to intern goals. */
    protected Map goals;
    
    /** Map from goals to number of times a pass was run for the goal. */
    protected Map runCount;

    /**
     * Map from goals to the number of amount of progress made since just before
     * the goal was last attempted.
     */
    Map progressMap;
    
    /**
     * Progress made since the start of the compilation.  When a pass is run,
     * progress is incremented by the difference in distance from the goal before
     * and after the pass is run. 
     */
    int currentProgress;
    
    /** True if any pass has failed. */
    boolean failed;

    protected static final Object COMPLETED_JOB = "COMPLETED JOB";

    /** The currently running pass, or null if no pass is running. */
    protected Pass currentPass;

    public Scheduler(ExtensionInfo extInfo) {
        this.extInfo = extInfo;

        this.jobs = new HashMap();
        this.goals = new HashMap();
        this.runCount = new HashMap();
        this.progressMap = new HashMap();
        this.inWorklist = new HashSet();
        this.worklist = new LinkedList();
        this.currentPass = null;
        this.currentProgress = 0;
    }
    
    /**
     * Add a new concurrent <code>subgoal</code> of the <code>goal</code>.
     * <code>subgoal</code> is a goal on which <code>goal</code> mutually
     * depends. The caller must be careful to ensure that all concurrent goals
     * can be eventually reached.
     */
    public void addConcurrentDependency(Goal goal, Goal subgoal) {
        goal.addConcurrentGoal(subgoal);
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
        goal.addPrerequisiteGoal(subgoal);
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
            if (Report.should_report(Report.frontend, 2))
                Report.report(2, "new goal " + g);
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "goals = " + goals.keySet());
        }
        return g;   
    }

    /** Add <code>goal</code> to the worklist and return its interned copy. */
    public void addGoal(Goal goal) {
        addGoalToWorklist(goal);
    }

    private synchronized void addGoalToWorklist(Goal g) {
        if (! inWorklist.contains(g)) {
            inWorklist.add(g);
            worklist.add(g);
        }
    }
    
    // Dummy pass needed for currentGoal(), currentPass(), etc., to work
    // when checking if a goal was reached.
    Pass schedulerPass(Goal g) {
        return new EmptyPass(g) {
            public boolean run() {
                return true;
            }
        };
    }
    
    public boolean reached(Goal g) {
        long t = System.currentTimeMillis();
        
        Job job = g.job();
        Pass pass = schedulerPass(g);
        Pass oldPass = this.currentPass;
        this.currentPass = pass;

        // Stop the timer on the old pass. */
        if (oldPass != null) {
            oldPass.toggleTimers(true);
        }

        if (job != null) {
            job.setRunningPass(pass);
        }
        
        boolean result = g.hasBeenReached();
        
        if (job != null) {
            job.setRunningPass(null);
        }
        
        this.currentPass = oldPass;

        // Restart the timer on the old pass. */
        if (oldPass != null) {
            oldPass.toggleTimers(true);
        }

        t = System.currentTimeMillis() - t;
        extInfo.getStats().accumPassTimes("scheduler.reached", t, t);
        
        return result;
    }

    /**
     * Attempt to complete all goals in the worklist (and any subgoals they
     * have). This method returns <code>true</code> if all passes were
     * successfully run and all goals in the worklist were reached. The worklist
     * should be empty at return.
     */ 
    public boolean runToCompletion() {
        boolean okay = true;
        
        while (! worklist.isEmpty()) {
            if (Report.should_report(Report.frontend, 2))
                Report.report(2, "processing next in worklist " + worklist);
    
            Goal goal = selectGoalFromWorklist();
            
            if (reached(goal)) {
                continue;
            }
            
            if (Report.should_report(Report.frontend, 1)) {
                Report.report(1, "Running to goal " + goal);
            }

            try {
                okay &= attemptGoal(goal, true, new HashSet(), new HashSet());
            }
            catch (CyclicDependencyException e) {
                addGoalToWorklist(goal);
                continue;
            }
        
            if (! okay) {
                break;
            }
            
            if (! reached(goal)) {
                if (Report.should_report(Report.frontend, 1)) {
                    Report.report(1, "Failed to reach " + goal + "; will reattempt");
                }
            
                addGoalToWorklist(goal);
            }
            else if (goal instanceof CodeGenerated) {
                // the job has finished. Let's remove it from the map so it
                // can be garbage collected, and free up the AST.
                jobs.put(goal.job().source(), COMPLETED_JOB);
                if (Report.should_report(Report.frontend, 1)) {
                    Report.report(1, "Completed job " + goal.job());
                }
            }
        }

        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Finished all passes -- " +
                        (okay ? "okay" : "failed"));

        return okay;
    }

    /**
     * Select and remove a <code>Goal</code> from the non-empty
     * <code>worklist</code>. Return the selected <code>Goal</code>
     * which will be scheduled to run all of its remaining passes.
     */
    private Goal selectGoalFromWorklist() {
        // TODO: Select the goal that will cause it's associated job to complete
        // first. This is the goal with the fewest subgoals closest to job
        // completion. The idea is to finish a job as quickly as possible in
        // order to free its memory.
        
        // Pick a goal not recently run, if available.
        for (Iterator i = worklist.iterator(); i.hasNext(); ) {
            Goal goal = (Goal) i.next();
            Integer progress = (Integer) progressMap.get(goal);
            if (progress == null || progress.intValue() < currentProgress) {
                i.remove();
                inWorklist.remove(goal);
                return goal;
            }
        }
        
        Goal goal = (Goal) worklist.removeFirst();
        inWorklist.remove(goal);
        return goal;
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
        
        // Create a goal for the job; this will set up dependencies for the goal,
        // even if the goal isn't added to the work list.
        Goal compileGoal = extInfo.getCompileGoal(job);

        if (compile) {
            // Now, add a goal for completing the job.
            addGoal(compileGoal);
        }
        
        return job;
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
    
    public boolean attemptGoal(Goal goal) throws CyclicDependencyException {
        return attemptGoal(goal, true, new HashSet(), new HashSet());
    }
    
    /**
     * Run a passes until the <code>goal</code> is attempted. Callers should
     * check goal.completed() and should be able to handle the goal not being
     * reached.
     * 
     * @return false if there was an error trying to reach the goal; true if
     *         there was no error, even if the goal was not reached.
     */ 
    private boolean attemptGoal(final Goal goal, boolean sameThread,
                                final Set prereqs, final Set goals)
            throws CyclicDependencyException
    {
        if (Report.should_report(Report.frontend, 2))
            Report.report(2, "Running to goal " + goal);
        
        if (Report.should_report(Report.frontend, 3)) {
            Report.report(3, "  Distance = " + goal.distanceFromGoal());
            Report.report(3, "  Reachable = " + goal.isReachable());
            Report.report(3, "  Prerequisites for " + goal + " = " + goal.prerequisiteGoals());
            Report.report(3, "  Concurrent goals for " + goal + " = " + goal.concurrentGoals());
            Report.report(3, "  Prereqs = " + prereqs);
            Report.report(3, "  Dependees = " + goals);
        }
        
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
        
        if (prereqs.contains(goal)) {
            // The goal has itself as a prerequisite.
            throw new InternalCompilerError("Goal " + goal + " depends on itself.");
        }
        
        // Another pass is being run over the same source file.  We cannot reach
        // the goal yet, so just return and let the other pass complete.  This
        // goal will be reattempted later, if necessary.
        if (goal.job() != null && goal.job().isRunning()) {
            throw new CyclicDependencyException();
        }
        
        if (goals.contains(goal)) {
            throw new CyclicDependencyException();
        }

        final boolean FORK = false;
        
        // Fork a new thread so that the stack depth does not get too large.
        if (FORK && ! sameThread) {
            System.out.println("spawning thread for " + goal);
            final int[] r = new int[1];
            
            Thread th = new Thread() {
                public void run() {
                    try {
                        boolean result = Scheduler.this.attemptGoal(goal, true, prereqs, goals);
                        r[0] = result ? 1 : 0;
                    }
                    catch (CyclicDependencyException e) {
                        r[0] = -1;
                    }
                }
            };
            
            th.start();
            
            try {
                th.join();
            }
            catch (InterruptedException e) {
                return false;
            }
            
            if (r[0] == -1) {
                throw new CyclicDependencyException();
            }
            
            return r[0] == 1;
        }
        
        prereqs.add(goal);
        goals.add(goal);
        
        // Make sure all subgoals have been completed,
        // except those that recursively depend on this goal.
        // If a subgoal has not been completed, just return and let
        // it complete before trying this goal again.
        boolean runPass = true;

        for (Iterator i = new ArrayList(goal.prerequisiteGoals()).iterator(); i.hasNext(); ) {
            Goal subgoal = (Goal) i.next();
            
            boolean okay = attemptGoal(subgoal, true, prereqs, goals);
            
            if (! okay) {
                goal.setUnreachable();
                if (Report.should_report(Report.frontend, 3))
                    Report.report(3, "Cannot reach goal " + goal + "; " + subgoal + " failed");
                return false;
            }
            
            if (! reached(subgoal)) {
                // put the subgoal back on the worklist
                addGoal(subgoal);
                runPass = false;
                if (Report.should_report(Report.frontend, 3))
                    Report.report(3, "Will delay goal " + goal + "; " + subgoal + " not reached");
            }
        }

        for (Iterator i = new ArrayList(goal.concurrentGoals()).iterator(); i.hasNext(); ) {
            Goal subgoal = (Goal) i.next();
            
            boolean okay = attemptGoal(subgoal, true, new HashSet(), goals);
            
            if (! okay) {
                goal.setUnreachable();
                if (Report.should_report(Report.frontend, 3))
                    Report.report(3, "Cannot reach goal " + goal + "; " + subgoal + " failed");
                return false;
            }
            
            if (! reached(subgoal)) {
                // put the subgoal on the worklist
                addGoal(subgoal);
                
                // Don't run the pass if another pass over the same job
                // needs to be completed first.
                if (goal.job() != null && subgoal.job() == goal.job() && subgoal != goal) {
                    runPass = false;

                    if (Report.should_report(Report.frontend, 3))
                        Report.report(3, "Will delay goal " + goal + "; " + subgoal + " not reached");
                }
            }
        }

        if (! runPass) {
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "A subgoal wasn't reached, delaying goal " + goal);
            throw new CyclicDependencyException();
        }
            
        // Check for completion again -- the goal may have been reached
        // while processing a subgoal.
        if (reached(goal)) {
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "Already reached goal " + goal + " (second check)");
            return true;
        }
        
        if (! goal.isReachable()) {
            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "Cannot reach goal " + goal + " (second check)");
            return false;
        }
        
        Pass pass = goal.createPass(extInfo);
        boolean result = runPass(pass);

        goals.remove(goal);
        prereqs.remove(goal);
        
        if (result && ! reached(goal)) {
            // Add the goal back on the worklist.
            addGoalToWorklist(goal);
        }
        
        return result;
    }       
   
    private int progressSinceLastAttempt(Goal goal) {
        int progress;
        Integer i = (Integer) progressMap.get(goal);
        if (i == null) {
            return Integer.MAX_VALUE;
        }
        else {
            progress = i.intValue();
        }
    
        return currentProgress - progress;
    }

    /**         
     * Run the pass <code>pass</code>.  All subgoals of the pass's goal
     * required to start the pass should be satisfied.  Running the pass
     * may not satisfy the goal, forcing it to be retried later with new
     * subgoals.
     */
    private boolean runPass(Pass pass) {
        Goal goal = pass.goal();
        Job job = goal.job();
                
        if (goal instanceof SourceFileGoal) {
            if (extInfo.getOptions().disable_passes.contains(pass.name())) {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Skipping pass " + pass);
                ((SourceFileGoal) goal).markRun();
                return true;
            }
        }
        
        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Running pass " + pass + " for " + goal);

        if (reached(goal)) {
            throw new InternalCompilerError("Cannot run a pass for completed goal " + goal);
        }
        
        if (progressSinceLastAttempt(goal) == 0) {
            if (failed) {
                return false;
            }
            throw new InternalCompilerError("Possible infinite loop detected trying to run a pass for " + goal + "; no progress made since last attempt.  Current distance from goal is " + goal.distanceFromGoal() + ".");
        }
        
        final int MAX_RUN_COUNT = 100;
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
            throw new InternalCompilerError("Possible infinite loop detected trying to run a pass for " + goal + " for the " + cardinal + " time.");
        }
        
        pass.resetTimers();

        boolean result = false;
        int oldDistance = goal.distanceFromGoal();

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

            result = pass.run();
            
            pass.toggleTimers(false);
            
            if (! result) {
                goal.setUnreachable();
            }
            
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

                PrettyPrinter pp = new PrettyPrinter();
                pp.printAst(job.ast(), new CodeWriter(System.err, 78));
            }

            // dump this pass if we need to.
            if (job != null && extInfo.getOptions().dump_ast.contains(pass.name())) {
                System.err.println("--------------------------------" +
                                   "--------------------------------");
                System.err.println("Dumping AST for " + job +
                                   " after " + pass.name());
                
                NodeVisitor dumper =
                  new DumpAst(new CodeWriter(System.err, 78));
                dumper = dumper.begin();
                job.ast().visit(dumper);
                dumper.finish();
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
        progressMap.put(goal, new Integer(currentProgress));
        int newDistance = goal.distanceFromGoal();
        int progress = newDistance - oldDistance;
        progress = progress > 0 ? progress : 0; // make sure progress is forward
        if (! result) progress++; // count a failure as progress
        currentProgress += progress;
            
        if (Report.should_report(Report.time, 2)) {
            Report.report(2, "Finished " + pass +
                          " status=" + statusString(result) + " inclusive_time=" +
                          pass.inclusiveTime() + " exclusive_time=" +
                          pass.exclusiveTime());
        }
        else if (Report.should_report(Report.frontend, 1)) {
            Report.report(1, "Finished " + pass +
                          " status=" + statusString(result) +
                          " distance=" + oldDistance + "->" + newDistance);
        }
        
        if (job != null) {
            job.updateStatus(result);
        }
                
        return result;             
    }           
                                   
    private static String statusString(boolean okay) {
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
    
    public abstract Goal InnerTranslated(Job job);
    
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
    
            if (Report.should_report(Report.frontend, 3)) {
                Report.report(3, "Adding job for " + source + " at the " +
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
    private Job createSourceJob(Source source, Node ast) {
        return new Job(extInfo, extInfo.jobExt(), source, ast);
    }

    public String toString() {
        return getClass().getName() + " worklist=" + worklist;
    }   
}


