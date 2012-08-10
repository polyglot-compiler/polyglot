/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.frontend.goals.AbstractGoal;
import polyglot.frontend.goals.EndGoal;
import polyglot.frontend.goals.Goal;
import polyglot.main.Main;
import polyglot.main.Report;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

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
    protected Set<Goal> inWorklist;
    protected LinkedList<Goal> worklist;

    /**
     * A map from <code>Source</code>s to <code>Job</code>s or to
     * the <code>COMPLETED_JOB</code> object if the Job previously
     * existed
     * but has now finished. The map contains entries for all
     * <code>Source</code>s that have had <code>Job</code>s added for them.
     */
    protected Map<Source, Job> jobs;

    protected Collection<Job> commandLineJobs;

    /** Map from goals to goals used to intern goals. */
    protected Map<Goal, Goal> goals;

    /** Map from goals to number of times a pass was run for the goal. */
    protected Map<Goal, Integer> runCount;

    /** True if any pass has failed. */
    protected boolean failed;

    /** The currently running pass, or null if no pass is running. */
    protected Pass currentPass;

    public Scheduler(ExtensionInfo extInfo) {
        this.extInfo = extInfo;

        this.jobs = new HashMap<Source, Job>();
        this.goals = new HashMap<Goal, Goal>();
        this.runCount = new HashMap<Goal, Integer>();
        this.inWorklist = new HashSet<Goal>();
        this.worklist = new LinkedList<Goal>();
        this.currentPass = null;
    }

    public Collection<Job> commandLineJobs() {
        return this.commandLineJobs;
    }

    public void setCommandLineJobs(Collection<Job> c) {
        this.commandLineJobs = Collections.unmodifiableCollection(c);
    }

    public boolean prerequisiteDependsOn(Goal goal, Goal subgoal) {
        if (goal == subgoal) {
            return true;
        }

        for (Goal g : goal.prerequisiteGoals(this)) {
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
        if (!goal.corequisiteGoals(this).contains(subgoal)) {
            if (Report.should_report(Report.frontend, 3)
                    || Report.should_report("deps", 1))
                Report.report(3, "Adding coreq edge: " + subgoal + " -> "
                        + goal);
            goal.addCorequisiteGoal(subgoal, this);
        }
    }

    public void addCorequisiteDependencyAndEnqueue(Goal goal, Goal subgoal) {
        addCorequisiteDependency(goal, subgoal);
        // addGoal(subgoal);
    }

    public void addDependencyAndEnqueue(Goal goal, Goal subgoal,
            boolean prerequisite) {
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
    public void addPrerequisiteDependency(Goal goal, Goal subgoal)
            throws CyclicDependencyException {
        if (!goal.prerequisiteGoals(this).contains(subgoal)) {
            if (Report.should_report(Report.frontend, 3)
                    || Report.should_report("deps", 1))
                Report.report(3, "Adding prereq edge: " + subgoal + " => "
                        + goal);
            goal.addPrerequisiteGoal(subgoal, this);
        }
    }

    /** Add prerequisite dependencies between adjacent items in a list of goals. */
    public void addPrerequisiteDependencyChain(List<Goal> deps)
            throws CyclicDependencyException {
        Goal prev = null;
        for (Goal curr : deps) {
            if (prev != null) addPrerequisiteDependency(curr, prev);
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
        Goal g = goals.get(goal);
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
        if (!inWorklist.contains(goal)) {
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
            jobs.put(job.source(), Job.COMPLETED);
            if (Report.should_report(Report.frontend, 1)) {
                Report.report(1, "Completed job " + job);
            }
        }
    }

    protected List<Goal> worklist() {
        return worklist;
    }

    protected static class TheEndGoal extends AbstractGoal {
        protected Scheduler scheduler;

        protected TheEndGoal(Scheduler scheduler) {
            super(null);
            this.scheduler = scheduler;
        }

        @Override
        public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
            return scheduler.worklist();
        }

        @Override
        public String toString() {
            return "TheEnd(" + scheduler.getClass().getName() + ")";
        }

        protected Collection<Goal> goals() {
            return scheduler.worklist();
        }

        @Override
        public Pass createPass(ExtensionInfo extInfo) {
            return new EndPass(this);
        }

        protected static class EndPass extends AbstractPass {
            protected EndPass(TheEndGoal g) {
                super(g);
            }

            @Override
            public boolean run() {
                TheEndGoal end = (TheEndGoal) goal();

                for (Goal goal : end.goals()) {
                    if (!goal.hasBeenReached()) {
                        throw new MissingDependencyException(goal, true);
                    }
                }

                return true;
            }
        }

        @Override
        public int hashCode() {
            return Boolean.TRUE.hashCode();
        }

        @Override
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

        while (okay && !reached(theEnd)) {
            okay = attemptGoal(theEnd);
        }

        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Finished all passes for "
                    + this.getClass().getName() + " -- "
                    + (okay ? "okay" : "failed"));

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
     * Run a pass until the <code>goal</code> is attempted. Callers should
     * check goal.completed() and should be able to handle the goal not being
     * reached.
     * 
     * @return false if there was an error trying to reach the goal; true if
     *         there was no error, even if the goal was not reached.
     */
    public boolean attemptGoal(Goal goal) {
        return attemptGoal(goal, new HashSet<Goal>());
    }

    protected boolean attemptGoal(Goal goal, Set<Goal> above) {
        if (Report.should_report("dump-dep-graph", 2))
            dumpInFlightDependenceGraph();

        if (Report.should_report(Report.frontend, 2))
            Report.report(2, "Running to goal " + goal);

        if (Report.should_report(Report.frontend, 4)) {
            Report.report(4, "  Reachable = " + goal.isReachable());
            Report.report(4,
                          "  Prerequisites for " + goal + " = "
                                  + goal.prerequisiteGoals(this));
            Report.report(4,
                          "  Corequisites for " + goal + " = "
                                  + goal.corequisiteGoals(this));
        }

        if (above.contains(goal)) {
            if (Report.should_report(Report.frontend, 4))
                Report.report(4, goal
                        + " is being attempted by a caller; returning");
            return true;
        }

        boolean progress = true;

        Set<Goal> newAbove = new HashSet<Goal>();
        newAbove.addAll(above);
        newAbove.add(goal);

        // Loop over the goal and its coreqs as long as progress is made.
        while (progress && !reached(goal)) {
            progress = false;

            if (Report.should_report(Report.frontend, 4))
                Report.report(4, "outer loop for " + goal);

            // Run the prereqs of the goal.
            for (Goal subgoal : new ArrayList<Goal>(goal.prerequisiteGoals(this))) {
                if (reached(subgoal)) {
                    continue;
                }

                if (Report.should_report(Report.frontend, 4))
                    Report.report(4, "running prereq: " + subgoal + "->" + goal);

                if (!attemptGoal(subgoal, newAbove)) {
                    return false;
                }

                if (reached(goal)) {
                    return true;
                }
            }

            // Make sure all prerequisite subgoals have been completed.
            // If any has not, just return.
            boolean runPass = true;

            for (Goal subgoal : goal.prerequisiteGoals(this)) {
                if (!reached(subgoal)) {
                    runPass = false;
                }
            }

            if (!runPass) {
                return true;
            }

            // Now, run the goal itself.
            if (Report.should_report(Report.frontend, 4))
                Report.report(4, "running goal " + goal);

            boolean result = runGoal(goal);

            if (!result) {
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
            for (Goal subgoal : new ArrayList<Goal>(goal.corequisiteGoals(this))) {
                if (reached(subgoal)) {
                    continue;
                }

                if (Report.should_report(Report.frontend, 4))
                    Report.report(4, "running coreq: " + subgoal + "->" + goal);

                if (!attemptGoal(subgoal, newAbove)) {
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

        if (!goal.isReachable()) {
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
            throw new InternalCompilerError("Cannot run a pass for completed goal "
                    + goal);
        }

        Integer countObj = this.runCount.get(goal);
        int count = countObj != null ? countObj.intValue() : 0;
        count++;
        this.runCount.put(goal, count);

        if (count >= MAX_RUN_COUNT) {
            String[] suffix = new String[] { "th", "st", "nd", "rd" };
            int index = count % 10;
            if (index > 3) index = 0;
            if (11 <= count && count <= 13) index = 0;
            String cardinal = count + suffix[index];
            String message =
                    "Possible infinite loop detected trying to run a pass for "
                            + goal + " for the " + cardinal + " time.";

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
                throw new Main.TerminationException(1);
            }
            else if (infiniteLoopGoal == null) {
                infiniteLoopGoal = goal;

                // Enable reporting.
                Report.addTopic(Report.frontend, 4);
                Report.addTopic("deps", 1);

                eq.enqueue(ErrorInfo.DEBUG,
                           message
                                   + "  The compiler will attempt the goal one more time with reporting enabled, then abort.");
            }
        }

        pass.resetTimers();

        boolean result = false;

        if (job == null || job.status()) {
            Pass oldPass = this.currentPass;
            this.currentPass = pass;
            Report.pushTopic(pass.name());

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

                if (!result) {
                    extInfo.getStats().accumPassTimes(key + " failures", 1, 1);
                    extInfo.getStats().accumPassTimes("total goal failures",
                                                      1,
                                                      1);

                    goal.setState(Goal.UNREACHABLE);
                    if (Report.should_report(Report.frontend, 1))
                        Report.report(1, "Failed pass " + pass + " for " + goal);
                }
                else {
                    if (goal.state() == Goal.RUNNING) {
                        extInfo.getStats().accumPassTimes(key + " reached",
                                                          1,
                                                          1);
                        extInfo.getStats().accumPassTimes("total goal reached",
                                                          1,
                                                          1);

                        goal.setState(Goal.REACHED);
                        if (Report.should_report(Report.frontend, 1))
                            Report.report(1, "Completed pass " + pass + " for "
                                    + goal);
                    }
                    else {
                        extInfo.getStats().accumPassTimes(key + " unreached",
                                                          1,
                                                          1);
                        extInfo.getStats()
                               .accumPassTimes("total goal unreached", 1, 1);

                        goal.setState(Goal.ATTEMPTED);
                        if (Report.should_report(Report.frontend, 1))
                            Report.report(1, "Completed (unreached) pass "
                                    + pass + " for " + goal);
                    }
                }
            }
            catch (MissingDependencyException e) {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Did not complete pass " + pass + " for "
                            + goal + " (missing " + e.goal() + ")");

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
                    Report.report(1, "Did not complete pass " + pass + " for "
                            + goal);

                extInfo.getStats().accumPassTimes(key + " aborts", 1, 1);
                extInfo.getStats().accumPassTimes("goal aborts", 1, 1);

                goal.setState(Goal.ATTEMPTED);
                result = true;
            }
            finally {
                t = System.currentTimeMillis() - t;
                extInfo.getStats().accumPassTimes(key, t, t);

                pass.toggleTimers(false);

                if (job != null) {
                    job.setRunningPass(null);
                }

                Report.popTopic();
                this.currentPass = oldPass;

                // Restart the timer on the old pass. */
                if (oldPass != null) {
                    oldPass.toggleTimers(true);
                }
            }

            // pretty-print this pass if we need to.
            if (job != null
                    && extInfo.getOptions().print_ast.contains(pass.name())) {
                System.err.println("--------------------------------"
                        + "--------------------------------");
                System.err.println("Pretty-printing AST for " + job + " after "
                        + pass.name());

                job.ast().prettyPrint(System.err);
            }

            // dump this pass if we need to.
            if (job != null
                    && extInfo.getOptions().dump_ast.contains(pass.name())) {
                System.err.println("--------------------------------"
                        + "--------------------------------");
                System.err.println("Dumping AST for " + job + " after "
                        + pass.name());

                job.ast().dump(System.err);
            }

            // This seems to work around a VM bug on linux with JDK
            // 1.4.0.  The mark-sweep collector will sometimes crash.
            // Running the GC explicitly here makes the bug go away.
            // If this fails, maybe run with bigger heap.

            // System.gc();
        }

        Stats stats = extInfo.getStats();
        stats.accumPassTimes(pass.name(),
                             pass.inclusiveTime(),
                             pass.exclusiveTime());

        if (!result) {
            failed = true;
        }

        // Record the progress made before running the pass and then update
        // the current progress.
        if (Report.should_report(Report.time, 2)) {
            Report.report(2,
                          "Finished " + pass + " status="
                                  + statusString(result) + " inclusive_time="
                                  + pass.inclusiveTime() + " exclusive_time="
                                  + pass.exclusiveTime());
        }
        else if (Report.should_report(Report.frontend, 1)) {
            Report.report(1, "Finished " + pass + " status="
                    + statusString(result));
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
    public Collection<Job> jobs() {
        ArrayList<Job> l = new ArrayList<Job>(jobs.size());

        for (Job job : jobs.values()) {
            if (job != Job.COMPLETED) {
                l.add(job);
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
        Job job = jobs.get(source);

        if (job == Job.COMPLETED) {
            // the job has already been completed.
            // We don't need to add a job
            return null;
        }

        if (job != null) return job;

        // No appropriate job yet exists, we will create one.
        job = this.createSourceJob(source, ast);

        // record the job in the map and the worklist.
        jobs.put(source, job);

        if (Report.should_report(Report.frontend, 4)) {
            Report.report(4, "Adding job for " + source + " at the "
                    + "request of pass " + currentPass);
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

    @Override
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

        Report.report(2, "digraph " + name + " {");
        Report.report(2,
                      "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        for (Goal g : new ArrayList<Goal>(goals.keySet())) {
            g = internGoal(g);

            int h = System.identityHashCode(g);

            // dump out this node
            Report.report(2,
                          h + " [ label = \"" + StringUtil.escape(g.toString())
                                  + "\" ];");

            // dump out the successors.
            for (Goal g2 : g.prerequisiteGoals(this)) {
                g2 = internGoal(g2);
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h + " [style=bold]");
            }

            for (Goal g2 : g.corequisiteGoals(this)) {
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

        Report.report(2, "digraph " + name + " {");
        Report.report(2,
                      "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        Set<Goal> print = new HashSet<Goal>();

        for (Goal g : new ArrayList<Goal>(goals.keySet())) {
            g = internGoal(g);

            if (g.state() == Goal.REACHED || g.state() == Goal.UNREACHED
                    || g.state() == Goal.UNREACHABLE) {
                continue;
            }

            print.add(g);

            for (Goal g2 : g.prerequisiteGoals(this)) {
                g2 = internGoal(g2);
                print.add(g2);
            }

            for (Goal g2 : g.corequisiteGoals(this)) {
                g2 = internGoal(g2);
                print.add(g2);
            }
        }

        for (Goal g : print) {
            g = internGoal(g);

            int h = System.identityHashCode(g);

            // dump out this node
            Report.report(2,
                          h + " [ label = \"" + StringUtil.escape(g.toString())
                                  + "\" ];");

            // dump out the successors.
            for (Goal g2 : g.prerequisiteGoals(this)) {
                g2 = internGoal(g2);
                if (!print.contains(g2)) continue;
                int h2 = System.identityHashCode(g2);
                Report.report(2, h2 + " -> " + h + " [style=bold]");
            }

            for (Goal g2 : g.corequisiteGoals(this)) {
                g2 = internGoal(g2);
                if (!print.contains(g2)) continue;
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

        Report.report(2, "digraph " + name + " {");
        Report.report(2,
                      "  fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        g = internGoal(g);

        int h = System.identityHashCode(g);

        // dump out this node
        Report.report(2, h + " [ label = \"" + StringUtil.escape(g.toString())
                + "\" ];");

        Set<Integer> seen = new HashSet<Integer>();
        seen.add(h);

        // dump out the successors.
        for (Goal g2 : g.prerequisiteGoals(this)) {
            g2 = internGoal(g2);
            int h2 = System.identityHashCode(g2);
            if (!seen.contains(h2)) {
                seen.add(h2);
                Report.report(2,
                              h2 + " [ label = \""
                                      + StringUtil.escape(g2.toString())
                                      + "\" ];");
            }
            Report.report(2, h2 + " -> " + h + " [style=bold]");
        }

        for (Goal g2 : g.corequisiteGoals(this)) {
            g2 = internGoal(g2);
            int h2 = System.identityHashCode(g2);
            if (!seen.contains(h2)) {
                seen.add(h2);
                Report.report(2,
                              h2 + " [ label = \""
                                      + StringUtil.escape(g2.toString())
                                      + "\" ];");
            }
            Report.report(2, h2 + " -> " + h);
        }

        Report.report(2, "}");
    }
}
