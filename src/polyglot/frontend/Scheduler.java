package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Scheduler</code> manages dependencies between passes.    It
 * schedules passes for a <code>Job</code>.
 */
public class Scheduler
{
    protected Set passes;
    protected Map deps;
    protected Map bars;
    protected boolean started;
    protected ExtensionInfo extension;

    public Scheduler(ExtensionInfo ext) {
        this.extension = ext;
        this.passes = new HashSet();
        this.deps = new HashMap();
        this.bars = new HashMap();
        this.started = false;
    }

    // Default passes:  These are the passes used by the Java compiler.

    /**
     * The first stage of the compiler. During this stage, lexical and syntatic
     * analysis are performed on the input source. Successful completion of
     * this stage indicates valid lexical and syntactic structure. The AST will
     * be well formed, but may contain ambiguities.
     */
    public static final PassID PARSE = new PassID("parse");

    /**
     * The second stage of the compiler. Here, the visible interface of all
     * classes (including inner classes) are read into a table. This includes
     * all fields and methods (including return, argument, and exception
     * types).  A ClassResolver is available (after completion of this stage)
     * which maps the name of each class found in this file to a ClassType.
     * These ClassType objects, however, may contain ambiguous types.  The AST
     * will still contain ambiguities after this stage.
     */
    public static final PassID READ = new PassID("read");

    /**
     * The third stage of a the compiler. In this stage, ambiguities are
     * removed from the ClassResolver for this source file. That is, for each
     * class defined in this file, the types associated with fields and methods
     * (include return, argument and exception types) will be disambiguated,
     * and replaced with actual ClassType definitions. In addition, upon
     * successful completion of this stage, all super types of any class that
     * is defined in this file will also be in this state. The AST of this
     * source file will continue to contain ambiguities.
     */

    // This job just adds continuations to the worklist.
    public static final PassID CLEAN = new PassID("clean");

    // This job runs all continuations on the worklist.  There must be a
    // barrier between CLEAN and FLUSH_CLEAN.
    public static final PassID FLUSH_CLEAN = new PassID("flush-clean");

    /**
     * The fourth stage of the compiler. During this stage, ambiguities are
     * removed from the AST. Ambiguous dotted expressions, such as "a.b.c", are
     * resolved and replaced with the appropriate nodes. Also, after completion
     * of this stage any AmbiguousTypes referenced in the AST will be replaced
     * with concrete ClassTypes. Note, however, that these ClassTypes
     * themselves may contain ambiguities. (The source files corresponding to
     * these ClassTypes may only be at the READ stage.)
     */
    public static final PassID DISAMBIGUATE = new PassID("disambiguate");

    /**
     * The fifth stage of the compiler. This stage represents the type and flow
     * checking of a source file. Note that all dependencies of this file must
     * be in the CLEAN state before this file can be type checked. To ensure
     * this, the compiler will attempt to bring ALL source in the work list up
     * to (and through) the CLEAN stage. If the compiler is unable to do so,
     * then it will exit with errors. All sources files which successfully
     * complete this stage are semantically valid.
     */
    public static final PassID CHECK = new PassID("check");

    /**
     * The sixth (and final) stage of the compiler. During this stage, the
     * translated version of the source file is written out to the output file. 
     */
    public static final PassID TRANSLATE = new PassID("translate");

    /**
     * Add a list of passes to the schedule and set up dependencies so
     * that they run in list order.
     */
    public void addOrderedPasses(List l) {
        PassID prev = null;

        for (Iterator i = l.iterator(); i.hasNext(); ) {
            PassID p = (PassID) i.next();

            addPass(p);

            if (prev != null) {
        	order(prev, p);
            }

            prev = p;
        }
    }

    /** Add a pass to the schedule.  This cannot be done after a job has
     * started. */
    public void addPass(PassID pass) {
        report(3, "Adding pass " + pass);

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        passes.add(pass);
    }

    /** Remove a pass from the schedule.  This cannot be done after a job has
     * started.  Also, the CLEAN and TRANSLATE passes cannot be removed (they
     * can be made no-ops however). */
    public void removePass(PassID pass) {
        report(3, "Removing pass " + pass);

	if (pass == CLEAN || pass == TRANSLATE) {
            throw new InternalCompilerError(
		"The CLEAN and TRANSLATE passes cannot be removed.  " +
		"They are used by the compiler.");
	}

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        passes.remove(pass);
        removeAllFromMap(deps, pass);
        removeAllFromMap(bars, pass);
    }

    /** Add a barrier to the schedule.  A barrier ensures that all jobs
     * complete the "before" pass before the job starts the "after" pass.
     */
    public void barrier(PassID before, PassID after) {
        report(3, "Adding barrer " + before + " -> " + after);

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        putMap(bars, before, after);
    }

    /** Remove a barrier. */
    public void removeBarrier(PassID before, PassID after) {
        report(3, "Removing barrer " + before + " -> " + after);

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        removeFromMap(bars, before, after);
    }

    /** Add a dependency.  A job must complete the "before" pass before
     * starting the "after" pass. */
    public void order(PassID before, PassID after) {
        report(3, "Adding dependency " + before + " -> " + after);

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        putMap(deps, before, after);
    }

    /** Remove a dependency. */
    public void removeOrder(PassID before, PassID after) {
        report(3, "Removing dependency " + before + " -> " + after);

        if (started) {
            throw new InternalCompilerError(
		"Cannot modify scheduler after start.");
        }

        removeFromMap(deps, before, after);
    }

    private void removeFromMap(Map map, PassID before, PassID after) {
        Collection c = (Collection) map.get(after);

        if (c != null) {
            c.remove(before);
        }
    }

    private void removeAllFromMap(Map map, PassID pass) {
        map.remove(pass);

        // Remove the pass from the RHS of the map.
        for (Iterator i = map.values().iterator(); i.hasNext(); ) {
            Collection c = (Collection) i.next();
            c.remove(pass);
        }
    }

    private void putMap(Map map, PassID before, PassID after) {
        Collection c = (Collection) map.get(after);

        if (c == null) {
            c = new HashSet();
            map.put(after, c);
        }

        c.add(before);
    }

    /*
    public void substPass(PassID old, PassID pass) {
        passes.remove(old);
        passes.add(pass);

        substMap(deps, old, pass);
        substMap(bars, old, pass);
    }

    private void substMap(Map map, PassID old, PassID pass) {
        // Replace the old pass on the LHS of the map.
        Collection c = (Collection) map.get(old);

        if (c != null) {
            map.remove(old);
            map.put(pass, c);
        }

        // Replace the old pass on the RHS of the map.
        for (Iterator i = map.values().iterator(); i.hasNext(); ) {
            c = (Collection) i.next();

            for (Iterator j = c.iterator(); j.hasNext(); ) {
        PassID q = (PassID) j.next();

        if (q == old) {
            // Careful: c must be a Set for this break to be safe
            i.remove();
            c.add(pass);
            break;
        }
            }
        }
    }
    */

    /** Incorporate barriers into the schedule as passes.  A barrier
     * "before->after" must run immediately before the "after" pass; that is,
     * after all other passes prior to the "after" pass.  It must also run
     * after the "before" pass, of course.  A barrier is implemented by
     * inserting a BarrierPass into the schedule for a job.  The BarrierPass
     * brings all other jobs up to date before letting the job continue.
     */
    private void incorporateBarriers() {
	for (Iterator i = bars.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    PassID after = (PassID) e.getKey();
	    Collection cb = (Collection) e.getValue();

	    for (Iterator j = cb.iterator(); j.hasNext(); ) {
		PassID before = (PassID) j.next();

		PassID barrier = new BarrierID(before, after);
		addPass(barrier);

		// The barrier must run immediately before the "after" pass, so
		// ensure it runs after all of the other dependencies.
		Collection c = (Collection) deps.get(after);

		for (Iterator k = c.iterator(); k.hasNext(); ) {
		    PassID dep = (PassID) k.next();

		    if (dep != before) {
			order(dep, barrier);
		    }
		}

		// The barrier must run after the "before" pass and before the
		// "after" pass.
		order(before, barrier);
		order(barrier, after);
	    }
	}
    }

    /** Check the dependency graph for cycles. */
    private void checkDeps() {
        for (Iterator i = passes.iterator(); i.hasNext(); ) {
            PassID p = (PassID) i.next();
            checkDeps(p, (Collection) deps.get(p));
        }
    }

    private void checkDeps(PassID pass, Collection c) {
        if (c == null) {
            return;
        }

        if (c.contains(pass)) {
            throw new InternalCompilerError("Cycle in dependencies at pass " +
		pass + ": " + deps);
        }

        for (Iterator i = c.iterator(); i.hasNext(); ) {
            PassID a = (PassID) i.next();
            checkDeps(pass, (Collection) deps.get(a));
        }
    }

    /** An Iterator over PassIDs.  Given a Job and a goal PassID, it returns
     * all passes that the job has not executed up to the goal in dependency
     * order. */
    private class PassIterator implements Iterator {
        Job job;
        PassID goal;
        LinkedList stack;

        PassIterator(Job job, PassID goal) {
            this.job = job;
            this.goal = goal;

            stack = new LinkedList();
            stack.add(goal);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Object next() {
            if (hasNext()) {
		while (! stack.isEmpty()) {
		    PassID pass = (PassID) stack.getLast();

		    if (job.hasCompleted(pass)) {
			stack.removeLast();
			continue;
		    }

		    Collection c;

		    c = (Collection) deps.get(pass);

		    if (c != null) {
			for (Iterator i = c.iterator(); i.hasNext(); ) {
			    PassID dep = (PassID) i.next();

			    if (! job.hasCompleted(dep)) {
			stack.add(dep);
			    }
			}

			if (stack.getLast() != pass) {
			    // Dependencies got pushed.
			    continue;
			}
		    }

		    stack.removeLast();

		    return pass;
		}
            }

            throw new NoSuchElementException();
        }

        public boolean hasNext() {
            return ! job.hasCompleted(goal) && ! stack.isEmpty();
        }
    }

    /** Instantiate a Pass for a given Job and PassID. */
    public Pass getPass(Job job, PassID pass) {
        if (pass instanceof BarrierID) {
            BarrierID barrier = (BarrierID) pass;
            return new BarrierPass(job.compiler(), barrier.first());
        }

        return extension.getPass(job, pass);
    }

    /** Return an Iterator over PassIDs.  Given a Job and a goal PassID, it
     * returns all passes that the job has not executed up to the goal in
     * dependency order. */
    public Iterator passes(Job job, PassID goal) {
        if (! started) {
            incorporateBarriers();
            checkDeps();
            started = true;
        }

        return new PassIterator(job, goal);
    }

    private static void report(int level, String msg) {
        jltools.frontend.Compiler.report(level, msg);
    }
}
