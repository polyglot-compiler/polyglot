package polyglot.frontend;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.main.Report;
import polyglot.frontend.Compiler;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Job</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public abstract class Job
{
    /** Field used for storing extension-specific information. */
    JobExt ext;

    /** The job that caused this job to load.  Barrier passes executed
     * by the parent job cause this job to run.
     */
    Job parent;

    /** List of jobs this Job depends on.  These are the jobs for the sources
     * loaded when this Job references a class not already loaded.  We try to
     * maintain the invariant that a child has run up to the last barrier
     * executed by its parent. */
    List children;

    /** The language extension used for this job. */
    protected ExtensionInfo lang;

    /** The AST constructed from the source file. */
    protected Node ast;

    /** List of passes remaining to be run on the work unit. */
    protected ArrayList passes;

    /** Index of the next pass to run. */
    protected int nextPass;

    /** True if currently running a pass. */
    protected boolean isRunning;

    /** True if all passes run so far have been successful. */
    protected boolean status;

    /** Map from pass id to pass. */
    protected Map passMap;

    public Job(ExtensionInfo lang, JobExt ext, Job parent, Node ast) {
        this.lang = lang;
        this.ext = ext;
	this.parent = parent;
	this.ast = ast;

        this.children = new LinkedList();
	this.passes = null;
        this.passMap = null;
        this.nextPass = 0;
        this.isRunning = false;
        this.status = true;

        if (parent != null) {
            parent.children().add(this);
        }
    }

    public void reparent(Job parent) {
        if (parent != null) {
            parent.children().add(this);
        }

        this.parent = parent;
    }

    public JobExt ext() {
      return ext;
    }

    public Job parent() {
        return parent;
    }

    public Pass lastBarrier() {
        for (int i = nextPass - 1; i >= 0; i--) {
            Pass pass = (Pass) passes.get(i);

            if (pass instanceof BarrierPass) {
                return pass;
            }
        }

        return null;
    }

    public void setIsRunning(boolean flag) {
        isRunning = flag;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /** Get the state's AST. */
    public Node ast() {
	return ast;
    }

    /** Set the state's AST. */
    public void ast(Node ast) {
        this.ast = ast;
    }

    public void dump(CodeWriter cw) {
	if (ast != null) {
	    ast.dump(cw);
	}
    }

    public abstract Context context();
    public abstract Source source();

    protected abstract List getPasses();

    public List passes() {
        if (passes == null) {
            init();
	}

        return passes;
    }

    private Map passMap() {
        if (passMap == null) {
            init();
        }
        return passMap;
    }

    private void init() {
        passes = new ArrayList(getPasses());
        passMap = new HashMap();
        for (int i = 0; i < passes.size(); i++) {
            Pass pass = (Pass) passes.get(i);
            passMap.put(pass.id(), new Integer(i));
        }
    }

    public boolean completed() {
        return pendingPasses().isEmpty();
    }

    public List completedPasses() {
	return passes().subList(0, nextPass);
    }

    public List pendingPasses() {
	return passes().subList(nextPass, passes.size());
    }

    public boolean completed(Pass.ID id) {
        Integer i = (Integer) passMap().get(id);
        return i != null && i.intValue() < nextPass;
    }

    public boolean pending(Pass.ID id) {
        Integer i = (Integer) passMap().get(id);
        return i != null && i.intValue() >= nextPass;
    }

    public Pass passByID(Pass.ID id) {
        Integer i = (Integer) passMap().get(id);

        if (i != null) {
            return (Pass) passes().get(i.intValue());
        }

        throw new InternalCompilerError("No pass named \"" + id + "\".");
    }

    public Pass nextPass() {
        if (nextPass < passes().size()) {
            Pass pass = (Pass) passes().get(nextPass);
            return pass;
        }
        else {
            return null;
        }
    }

    public boolean done() {
        return nextPass >= passes().size();
    }

    public boolean status() {
        return status;
    }

    public void finishPass(Pass p, boolean okay) {
        List passes = passes();

        status &= okay;

        for (int i = nextPass; i < passes.size(); i++) {
	    Pass pass = (Pass) passes.get(i);

	    if (pass == p) {
                nextPass = i + 1;
	      	break;
	    }
	}
    }

    public List children() {
        return children;
    }

    public ExtensionInfo extensionInfo() {
	return lang;
    }

    public Compiler compiler() {
	return lang.compiler();
    }

    public Node spawn(Context c, Node ast, Pass.ID begin, Pass.ID end) {
        Job j = lang.createJob(ast, c, this, begin, end);

	if (Report.should_report(Report.frontend, 1))
	    Report.report(1, this + " spawning " + j);

        if (! lang.runAllPasses(j)) {
            return null;
        }

        return j.ast();
    }
}
