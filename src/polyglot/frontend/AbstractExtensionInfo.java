package polyglot.frontend;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.UsageError;
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
    protected Options options;
    protected TypeSystem ts = null;
    protected NodeFactory nf = null;
    protected SourceLoader source_loader = null;
    protected TargetFactory target_factory = null;

    /** A map from sources to source jobs. */
    Map jobs;

    /** A list of all the source jobs. */
    LinkedList worklist;

    /** The currently running job, or null. */
    Job currentJob;

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

    /** Run all jobs in the work list to completion. */
    public boolean runToCompletion() {
        boolean okay = true;

        // Run the jobs breadth-first rather than depth first to ensure
        // inter-dependent jobs in the worklist are kept in sync.
        while (okay && ! worklist.isEmpty()) {
            SourceJob job = (SourceJob) worklist.removeFirst();
            if (Report.should_report(Report.frontend, 1))
		Report.report(1, "Running job " + job);
            okay &= runNextPass(job);

            if (! job.completed()) {
                worklist.add(job);
            }
        }

        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Finished all passes -- " +
                        (okay ? "okay" : "failed"));

        return okay;
    }

    /**
     * Read a source file and compile it up to the the current job's last
     * barrier.
     */
    public boolean readSource(FileSource source) {
        Job job = addJob(source);

        // Run the new job up to its owner's (the current job's) barrier.
        Pass.ID barrier;

        if (currentJob != null) {
            if (currentJob.lastBarrier() == null) {
                throw new InternalCompilerError("Job which has not reached a " +                                                "barrier cannot read another " +                                                "source file.");
            }

            barrier = currentJob.lastBarrier().id();
        }
        else {
            barrier = Pass.FIRST_BARRIER;
        }

        return runToPass(job, barrier);
    }

    /** Run a job until the <code>goal</code> pass completes. */
    public boolean runToPass(Job job, Pass.ID goal) {
        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Running " + job + " to pass named " + goal);

        if (job.completed(goal)) {
            return true;
        }

        Pass pass = job.passByID(goal);

        return runToPass(job, pass);
    }

    /** Run a job up to the <code>goal</code> pass. */
    public boolean runToPass(Job job, Pass goal) {
        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Running " + job + " to pass " + goal);

        boolean okay = job.status();

        while (! job.pendingPasses().isEmpty()) {
            Pass pass = (Pass) job.pendingPasses().get(0);

            if (options.disable_passes.contains(pass.name())) {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Skipping pass " + pass);
            }
            else {
                if (Report.should_report(Report.frontend, 1))
                    Report.report(1, "Trying to run pass " + pass);

                if (job.isRunning()) {
                    // We're currently running.  We can't reach the goal.
                    throw new InternalCompilerError(job +
                                                    " cannot reach pass " +
                                                    pass);
                }

                long start_time = System.currentTimeMillis();

                if (okay) {
                    Job oldCurrentJob = currentJob;
                    currentJob = job;

                    Report.should_report.push(pass.name());

                    job.setIsRunning(true);
                    okay &= pass.run();
                    job.setIsRunning(false);

                    Report.should_report.pop();

                    if (options.dump_ast.contains(pass.name())) {
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


                    currentJob = oldCurrentJob;
                }

                if (Report.should_report(Report.time, 1)) {
                    Report.report(1, "Finished " + pass +
                                  " status=" + str(okay) + " time=" +
                                  (System.currentTimeMillis() - start_time));
                }
                else if (Report.should_report(Report.frontend, 1)) {
                    Report.report(1, "Finished " + pass +
                                  " status=" + str(okay));
                }
            }

            job.finishPass(pass, okay);

            if (pass == goal) {
                break;
            }
        }

        if (Report.should_report(Report.frontend, 1))
	    Report.report(1, "Pass " + goal + " " + str(okay));

        // Ensure orphaned jobs don't get lost.
        if (job.completed()) {
            if (Report.should_report(Report.frontend, 1))
                Report.report(1, "Job " + job + " completed");

            for (Iterator i = job.children().iterator(); i.hasNext(); ) {
                Job orphan = (Job) i.next();

                if (orphan.completed()) {
                    continue;
                }

                if (! (orphan instanceof SourceJob)) {
                    throw new InternalCompilerError("Cannot adopt inner job " +
                                                    job + "; it should be " +
                                                    "done already.");
                }


                if (job.parent() != null) {
                    if (Report.should_report(Report.frontend, 2))
                        Report.report(2, "Job " + job.parent() + " adopting " +
                                      orphan);
                    orphan.reparent(job.parent());
                }
                else {
                    if (Report.should_report(Report.frontend, 2))
                        Report.report(2, "Worklist adopting " + orphan);
                    SourceJob sj = (SourceJob) orphan;
                    jobs.put(sj.source(), sj);
                    worklist.add(sj);
                }

                i.remove();
            }
        }

        return okay;
    }
 
    private static String str(boolean okay) {
        if (okay) {
            return "done";
        }
        else {
            return "failed";
        }
    }

    protected boolean runNextPass(Job job) {
        if (! job.pendingPasses().isEmpty()) {
            Pass pass = (Pass) job.pendingPasses().get(0);
            return runToPass(job, pass);
        }

        return true;
    }

    public boolean runAllPasses(Job job) {
        List passes = job.pendingPasses();

        // Run until there are no more passes.
        if (! passes.isEmpty()) {
            Pass pass = (Pass) passes.get(passes.size()-1);
            return runToPass(job, pass);
        }

        return true;
    }

    public String fileExtension() {
	String sx = options == null ? null : options.source_ext;

	if (sx == null) {
	    sx = defaultFileExtension();
        }

        return sx;
    }

    public SourceLoader sourceLoader() {
        if (source_loader == null) {
            source_loader = new SourceLoader(this, options.source_path);
        }

        return source_loader;
    }

    public TargetFactory targetFactory() {
        if (target_factory == null) {
            target_factory = new TargetFactory(options.output_directory,
                                               options.output_ext,
                                               options.output_stdout);
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

    public SourceJob createJob(Job parent, Source source, Node ast) {
        return new SourceJob(this, jobExt(), parent, source, ast);
    }

    public SourceJob addJob(Source source) {
        return addJob(source, null);
    }

    public SourceJob addJob(Source source, Node ast) {
        SourceJob job = (SourceJob) jobs.get(source);

        if (job == null) {
            job = this.createJob(currentJob, source, ast);
            jobs.put(source, job);
            worklist.add(job);
        }

        return job;
    }

    public Job createJob(Node ast, Context context, Job outer, Pass.ID begin, Pass.ID end) {
	return new InnerJob(this, jobExt(), ast, context, outer, begin, end);
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
