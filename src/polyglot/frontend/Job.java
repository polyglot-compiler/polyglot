package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.frontend.Compiler;

import java.util.*;
import java.io.IOException;

/**
 * A <code>Job</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public abstract class Job
{
    Job parent;
    JobExt ext;

    /** List of jobs this Job depends on.  These are the jobs for the sources
     * loaded when this Job references a class not already loaded.  We try to
     * maintain the invariant that a child has run up to the last barrier
     * executed by its parent. */
    List children;

    Compiler compiler;

    /** The AST constructed from the source file. */
    protected Node ast;

    /** List of passes remaining to be run on the work unit. */
    protected ArrayList passes;
    protected int nextPass;
    protected boolean isRunning;
    protected boolean status;
    protected Map passMap;

    public Job(Compiler compiler, JobExt ext, Job parent, Node ast) {
        this.compiler = compiler;
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

    public JobExt ext() {
      return ext;
    }

    public Job parent() {
        if (parent == null) {
            throw new InternalCompilerError("Null parent.");
        }
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
    public abstract ImportTable importTable();

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
        Pass pass = (Pass) passes().get(nextPass);
        return pass;
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

    public Compiler compiler() {
	return compiler;
    }

    public Node spawn(Context c, Node ast, Pass.ID begin, Pass.ID end) {
        Job j = compiler.extensionInfo().createJob(ast, c, this, begin, end);

        Compiler.report(1, this + " spawning " + j);

        if (! compiler.runAllPasses(j)) {
            return null;
        }

        return j.ast();
    }
}
