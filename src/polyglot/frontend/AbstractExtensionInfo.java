package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;
import jltools.main.UsageError;
import jltools.main.Options;
import jltools.main.Report;
import jltools.frontend.Compiler;

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

    public void setOptions(Options options) throws UsageError {
	this.options = options;
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
            Compiler.report(1, "Running job " + job);
            okay &= runNextPass(job);

            if (! job.completed()) {
                worklist.add(job);
            }
        }

        Compiler.report(1, "Finished all passes -- " +
                        (okay ? "okay" : "failed"));

        return okay;
    }

    /**
     * Read a source file and compile it up to the the current job's last
     * barrier.
     */
    public boolean readSource(Source source) {
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
        Compiler.report(1, "Running " + job + " to pass named " + goal);

        if (job.completed(goal)) {
            return true;
        }

        Pass pass = job.passByID(goal);

        return runToPass(job, pass);
    }

    /** Run a job up to the <code>goal</code> pass. */
    public boolean runToPass(Job job, Pass goal) {
        Compiler.report(1, "Running " + job + " to pass " + goal);

        boolean okay = job.status();

        while (! job.pendingPasses().isEmpty()) {
            Pass pass = (Pass) job.pendingPasses().get(0);

            Compiler.report(2, "Trying to run pass " + pass);

            if (job.isRunning()) {
                // We're currently running.  We can't reach the goal.
                throw new InternalCompilerError(job + " cannot reach pass " +
                                                pass);
            }

            long start_time = System.currentTimeMillis();

            if (okay) {
                Job oldCurrentJob = currentJob;
                currentJob = job;

                job.setIsRunning(true);
                okay &= pass.run();
                job.setIsRunning(false);

                currentJob = oldCurrentJob;
            }

            job.finishPass(pass, okay);

            Compiler.report(2, "Finished " + pass + " status=" + str(okay));
            Compiler.reportTime(1, "Finished " + pass + " status=" + str(okay)
                                + " time=" + (System.currentTimeMillis() -
                                              start_time));

            if (pass == goal) {
                break;
            }
        }

        Compiler.report(1, "Pass " + goal + " " + str(okay));

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

    /** By default, don't parse anything */
    public int parseCommandLine(String args[], int index, Options options)
	throws UsageError {
	return index;
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

    public abstract Parser parser(Reader reader, Source source, ErrorQueue eq);

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
        boolean in = false;

        for (Iterator i = l.iterator(); i.hasNext(); ) {
            Pass p = (Pass) i.next();
            in = in || begin == p.id();
            if (! (p instanceof BarrierPass) && ! in) i.remove();
            in = in && end != p.id();
        }

        return l;
    }

    static { Report.topics.add("verbose"); }
    static { Report.topics.add("types"); }
    static { Report.topics.add("frontend"); }
    static { Report.topics.add("loader"); }

    public String toString() {
        return getClass().getName() + " worklist=" + worklist;
    }
}
