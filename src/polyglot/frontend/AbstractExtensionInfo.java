package polyglot.frontend;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.main.Options;
import polyglot.main.Report;
import polyglot.frontend.Compiler;

import java.io.*;
import java.util.*;

/**
 * This is an abstract <code>ExtensionInfo</code>.
 */
public abstract class AbstractExtensionInfo implements ExtensionInfo {
    protected Compiler compiler;
    private Options options;
    protected TypeSystem ts = null;
    protected NodeFactory nf = null;
    protected SourceLoader source_loader = null;
    protected TargetFactory target_factory = null;

    /** 
     * A list of  <code>SourceJob</code>s. We maintain the invariant that if
     * a <code>SourceJob s</code> is in the <code>worklist</code>, then
     * <code>s.parent == null</code>.
     */
    protected LinkedList worklist;

    /** 
     * A map from <code>Source</code>s to <code>SourceJob</code>s or to
     * the <code>COMPLETED_JOB</code> object if the SourceJob previously existed
     * but has now finished. The map contains entries for all 
     * <code>Source</code>s that have had <code>Job</code>s added for them.
     */
    protected Map jobs;

    protected static final Object COMPLETED_JOB = "COMPLETED JOB";

    /** The currently running job, or null if no job is running. */
    protected Job currentJob;

    public Options getOptions() {
        if (this.options == null) {
            this.options = createOptions();
        }
        return options;
    }

    protected Options createOptions() {
        return new Options(this);
    }
        
    public Compiler compiler() {
        return compiler;
    }

    public void initCompiler(Compiler compiler) {
	this.compiler = compiler;
        jobs = new HashMap();
        worklist = new LinkedList();
        currentJob = null;

        // Create the type system and node factory.
        typeSystem();
        nodeFactory();

        initTypeSystem();
    }

    protected abstract void initTypeSystem();

    /** 
     * Run all jobs in the work list (and any children they have) to 
     * completion. This method returns <code>true</code> if all jobs were
     * successfully completed. If all jobs were successfully completed, then
     * the worklist will be empty.
     * 
     * The scheduling of <code>Job</code>s uses two methods to maintain 
     * scheduling invariants: <code>selectJobFromWorklist</code> selects
     * a <code>SourceJob</code> from <code>worklist</code> (a list of 
     * jobs that still need to be processed); <code>enforceInvariants</code> is
     * called before a pass is performed on a <code>SourceJob</code> and is
     * responsible for ensuring all dependencies are satisfied before the
     * pass proceeds, i.e. enforcing any scheduling invariants.    
     */
    public boolean runToCompletion() {
        boolean okay = true;


        while (okay && ! worklist.isEmpty()) {
            SourceJob job = selectJobFromWorklist();
            
            if (Report.should_report(Report.frontend, 1)) {
		Report.report(1, "Running job " + job);
            }

            okay &= runAllPasses(job);

            if (job.completed()) {
                // the job has finished. Let's remove it from the map so it 
                // can be garbage collected, and free up the AST.
                jobs.put(job.source(), COMPLETED_JOB);
                
                if (Report.should_report(Report.frontend, 1)) {
                    Report.report(1, "Completed job " + job);
                }
            }
            else {
                // the job is not yet completed (although, it really 
                // should be...)
                if (Report.should_report(Report.frontend, 1)) {
                    Report.report(1, "Failed to complete job " + job);
                }
                worklist.add(job);
            }
        }

        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Finished all passes -- " +
                        (okay ? "okay" : "failed"));

        return okay;
    }
    
    /**
     * Select and remove a <code>SourceJob</code> from the non-empty 
     * <code>worklist</code>. Return the selected <code>SourceJob</code>
     * which will be scheduled to run all of its remaining passes. 
     */
    protected SourceJob selectJobFromWorklist() {
        return (SourceJob)worklist.remove(0);
    }

    /**
     * Read a source file and compile it up to the the current job's last
     * barrier.
     */
    public boolean readSource(FileSource source) {
        // Add a new SourceJob for the given source. If a Job for the source
        // already exists, then we will be given the existing job.
        SourceJob job = addJob(source);

        // Run the new job up to the currentJob's SourceJob's last barrier, to
        // make sure that dependencies are satisfied.
        Pass.ID barrier;

        if (currentJob != null) {
            if (currentJob.sourceJob().lastBarrier() == null) {
                throw new InternalCompilerError("A Source Job which has " +
                            "not reached a barrier cannot read another " +
                            "source file.");
            }

            barrier = currentJob.sourceJob().lastBarrier().id();
        }
        else {
            barrier = Pass.FIRST_BARRIER;
        }

        return runToPass(job, barrier);
    }

    /**
     * Run all pending passes on <code>job</code>.
     */
    public boolean runAllPasses(Job job) {
        List pending = job.pendingPasses();
    
        // Run until there are no more passes.
        if (!pending.isEmpty()) {
            Pass lastPass = (Pass)pending.get(pending.size() - 1);
            return runToPass(job, lastPass);
        }
    
        return true;
    }

    /** 
     * Run a job until the <code>goal</code> pass completes. 
     */
    public boolean runToPass(Job job, Pass.ID goal) {
        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Running " + job + " to pass named " + goal);

        if (job.completed(goal)) {
            return true;
        }

        Pass pass = job.passByID(goal);

        return runToPass(job, pass);
    }

    /** 
     * Run a job up to the <code>goal</code> pass. 
     */
    public boolean runToPass(Job job, Pass goal) {
        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Running " + job + " to pass " + goal);

        while (! job.pendingPasses().isEmpty()) {
            Pass pass = (Pass) job.pendingPasses().get(0);

            runPass(job, pass);

            if (pass == goal) {
                break;
            }
        }

        if (job.completed()) {
            if (Report.should_report(Report.frontend, 1))
                Report.report(1, "Job " + job + " completed");

            // Ensure orphaned jobs don't get lost.
            if (job instanceof SourceJob) {
                rescueOrphans((SourceJob)job);
            }
        }

        return job.status();
    }
    
    /**
     * Run the pass <code>pass</code> on the job. Before running the pass on
     * the job, if the job is a <code>SourceJob</code>, then this method will 
     * ensure that the scheduling invariants are enforced by calling 
     * <code>enforceInvariants</code>.
     */
    protected void runPass(Job job, Pass pass) {
        // make sure that all scheduling invariants are satisfied before running
        // the next pass. We may thus execute some other passes on other 
        // jobs running the given pass.
        if (job instanceof SourceJob) {
            enforceInvariants((SourceJob)job, pass);
        }
        
        
        if (getOptions().disable_passes.contains(pass.name())) {
            if (Report.should_report(Report.frontend, 1))
                Report.report(1, "Skipping pass " + pass);
            job.finishPass(pass, true);
            return;                
        }
        
        if (Report.should_report(Report.frontend, 1))
            Report.report(1, "Trying to run pass " + pass);

        if (job.isRunning()) {
            // We're currently running.  We can't reach the goal.
            throw new InternalCompilerError(job +
                                            " cannot reach pass " +
                                            pass);
        }

        long start_time = System.currentTimeMillis();
        
        boolean result = false;
        if (job.status()) {            
            Job oldCurrentJob = this.currentJob;
            this.currentJob = job;
            Report.should_report.push(pass.name());
            job.setIsRunning(true);
            
            result = pass.run();
            
            job.setIsRunning(false);
            Report.should_report.pop();
            this.currentJob = oldCurrentJob;

            // dump this pass if we need to.
            if (getOptions().dump_ast.contains(pass.name())) {
                System.err.println("--------------------------------" +
                                   "--------------------------------");
                System.err.println("Dumping AST for " + job +
                                   " after " + pass.name());

                PrettyPrinter pp = new PrettyPrinter();
                pp.printAst(job.ast(), new CodeWriter(System.err, 78));
            }

            // This seems to work around a VM bug on linux with JDK
            // 1.4.0.  The mark-sweep collector will sometimes crash.
            // Running the GC explicitly here makes the bug go away.
            // If this fails, maybe run with bigger heap.

            // System.gc();
        }

        if (Report.should_report(Report.time, 1)) {
            Report.report(1, "Finished " + pass +
                          " status=" + str(result) + " time=" +
                          (System.currentTimeMillis() - start_time));
        }
        else if (Report.should_report(Report.frontend, 1)) {
            Report.report(1, "Finished " + pass +
                          " status=" + str(result));
        }

        job.finishPass(pass, result);
    }
    
    /**
     * Before running <code>Pass pass</code> on <code>SourceJob job</code>
     * make sure that all appropriate scheduling invariants are satisfied,
     * to ensure that all passes of other jobs that <code>job</code> depends
     * on will have already been done.
     * 
     */
    protected void enforceInvariants(SourceJob job, Pass pass) {
        BarrierPass lastBarrier = job.lastBarrier();
        if (lastBarrier != null) {
            // make sure that _all_ jobs have completed at least up to 
            // the last barrier (not just children).
            //
            // Ideally the invariant should be that only the source jobs that
            // job _depends on_ should be brought up to the last barrier.
            // This is work to be done in the future...
            List allJobs = new ArrayList(jobs.values());
            Iterator i = allJobs.iterator();
            //Iterator i = job.children.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o == COMPLETED_JOB) continue;
                SourceJob sj = (SourceJob)o;
                if (sj.pending(lastBarrier.id())) {
                    // Make the job run up to the last barrier.
                    // We ignore the return result, since even if the job 
                    // fails, we will keep on going and see
                    // how far we get...
                    if (Report.should_report(Report.frontend, 3)) {
                        Report.report(3, "Running " + sj +
                                  " to " + lastBarrier.id() + " for " + job);
                    } 
                    runToPass(sj, lastBarrier.id());
                }
            }
        }
    }
            
    /**
     * Make sure that the children of the completed Job <code>job</code>
     * are looked after. To wit, if <code>job</code> was on the worklist, then
     * the children of <code>job</code> are added to the worklist; otherwise
     * the parent of <code>job</code> adopts the children of <code>job</code>.
     */
    protected void rescueOrphans(SourceJob job) {
        SourceJob newParent = job.parent();
        
        for (Iterator i = job.children().iterator(); i.hasNext(); ) {
            SourceJob orphan = (SourceJob) i.next();
            i.remove();
            orphan.reparent(newParent);
            
            if (orphan.completed()) {
                continue;
            }
            if (newParent != null) {
                if (Report.should_report(Report.frontend, 2))
                    Report.report(2, "Job " + newParent + " adopting " +
                                  orphan);
            }
            else {
                if (Report.should_report(Report.frontend, 2)) {
                    Report.report(2, "Worklist adopting " + orphan);
                }
                worklist.add(orphan);
            }
        }
    }
 
    private static String str(boolean okay) {
        if (okay) {
            return "done";
        }
        else {
            return "failed";
        }
    }


    public String fileExtension() {
	String sx = getOptions() == null ? null : getOptions().source_ext;

	if (sx == null) {
	    sx = defaultFileExtension();
        }

        return sx;
    }

    public SourceLoader sourceLoader() {
        if (source_loader == null) {
            source_loader = new SourceLoader(this, getOptions().source_path);
        }

        return source_loader;
    }

    public TargetFactory targetFactory() {
        if (target_factory == null) {
            target_factory = new TargetFactory(getOptions().output_directory,
                                               getOptions().output_ext,
                                               getOptions().output_stdout);
        }
        
        return target_factory;
    }

    /** Create the type system for this extension. */
    protected abstract TypeSystem createTypeSystem();

    public TypeSystem typeSystem() {
	if (ts == null) {
	    ts = createTypeSystem();
	}
	return ts;
    }

    /** Create the node factory for this extension. */
    protected abstract NodeFactory createNodeFactory();

    public NodeFactory nodeFactory() {
	if (nf == null) {
	    nf = createNodeFactory();
	}
	return nf;
    }

    public JobExt jobExt() {
      return null;
    }

    /**
     * Add a new <code>SourceJob</code> for the <code>Source source</code>.
     */
    public SourceJob addJob(Source source) {
        return addJob(source, null);
    }

    /**
     * Add a new <code>SourceJob</code> for the <code>Source source</code>,
     * with AST <code>ast</code>.
     */
    public SourceJob addJob(Source source, Node ast) {
        SourceJob job = (SourceJob) jobs.get(source);
        
        if (job == COMPLETED_JOB) {
            // XXX is this correct? maybe return null, and change the
            // semantics of the method?
            throw new InternalCompilerError("A job for the source " + source 
                 + " was requested, even though that job has finished.");
        }        
        else if (job == null) {
            // No appropriate job yet exists, we will create one.
            
            // find an appropriate parent, if there is one.
            SourceJob parent = null;
            if (currentJob != null) {
                parent = currentJob.sourceJob();
            }
            job = this.createSourceJob(parent, source, ast);
            
            // record the job in the map.
            jobs.put(source, job);

            if (Report.should_report(Report.frontend, 3)) {
                Report.report(3, "Adding job for " + source + " at the " + 
                    "request of job " + job.parent());
            }
                        
            // If there is no parent, then add the job to the worklist.
            if (job.parent() == null) {
                worklist.add(job);
            }
        }

        return job;
    }

    /**
     * Create a new <code>SourceJob</code> for the given source and AST. 
     * In general, this method should only be called by <code>addJob</code>.
     */
    protected SourceJob createSourceJob(SourceJob parent, Source source, Node ast) {
        return new SourceJob(this, jobExt(), parent, source, ast);
    }

    /**
     * Create a new non-<code>SourceJob</code> <code>Job</code>, for the
     * given AST. In general this method should only be called by
     * <code>spawnJob</code>. 
     * 
     * @param ast the AST the new Job is for.
     * @param context the context that the AST occurs in
     * @param outer the <code>Job</code> that spawned this job. 
     * @param begin the first pass to perform for this job.
     * @param end the last pass to perform for this job.
     */
    protected Job createJob(Node ast, Context context, Job outer, Pass.ID begin, Pass.ID end) {
	return new InnerJob(this, jobExt(), ast, context, outer, begin, end);
    }

    /**
     * Spawn a new job. All passes between the pass <code>begin</code>
     * and <code>end</code> inclusive will be performed immediately on
     * the AST <code>ast</code>.
     * 
     * @param ast the AST the new Job is for.
     * @param context the context that the AST occurs in
     * @param outerJob the <code>Job</code> that spawned this job. 
     * @param begin the first pass to perform for this job.
     * @param end the last pass to perform for this job.
     */
    public Node spawnJob(Context c, Node ast, Job outerJob, 
                           Pass.ID begin, Pass.ID end) {
        Job j = createJob(ast, c, outerJob, begin, end);

        if (Report.should_report(Report.frontend, 1))
            Report.report(1, this +" spawning " + j);

        if (!runAllPasses(j)) {
            return null;
        }

        return j.ast();
    }

    public abstract Parser parser(Reader reader, FileSource source, ErrorQueue eq);

    public void replacePass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            if (p instanceof BarrierPass) {
              throw new InternalCompilerError("Cannot replace a barrier pass.");
            }

            i.remove();

            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void removePass(List passes, Pass.ID id) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            if (p instanceof BarrierPass) {
              throw new InternalCompilerError("Cannot remove a barrier pass.");
            }

            i.remove();
            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void beforePass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            // Backup one position.
            i.previous();

            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void afterPass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void replacePass(List passes, Pass.ID id, Pass pass) {
        replacePass(passes, id, Collections.singletonList(pass));
    }

    public void beforePass(List passes, Pass.ID id, Pass pass) {
        beforePass(passes, id, Collections.singletonList(pass));
    }

    public void afterPass(List passes, Pass.ID id, Pass pass) {
        afterPass(passes, id, Collections.singletonList(pass));
    }

    public abstract List passes(Job job);

    public List passes(Job job, Pass.ID begin, Pass.ID end) {
        List l = passes(job);
        Pass p = null;

        Iterator i = l.iterator();

        while (i.hasNext()) {
            p = (Pass) i.next();
            if (begin == p.id()) break;
            if (! (p instanceof BarrierPass)) i.remove();
        }

        while (p.id() != end && i.hasNext()) {
            p = (Pass) i.next();
        }

        while (i.hasNext()) {
            p = (Pass) i.next();
            i.remove();
        }

        return l;
    }

    public String toString() {
        return getClass().getName() + " worklist=" + worklist;
    }
}
