package polyglot.frontend;

import polyglot.ast.*;

import java.util.*;

/**
 * A <code>SourceJob</code> encapsulates work done by the compiler on behalf of
 * one source file.  It includes all information carried between phases
 * of the compiler.
 */
public class SourceJob extends Job
{
    /**
     * The <code>Source</code> that this <code>Job</code> is for.
     */
    protected Source source;

    /** 
     * The <code>SourceJob</code> that caused this job to load. The scheduling
     * of <code>Job</code>s guarantees that child <code>SourceJob</code>s
     * have completed at least up to the last <code>BarrierPass</code> of the
     * parent.
     * 
     * @see polyglot.frontend.AbstractExtensionInfo
     */
    protected SourceJob parent;

    /** 
     * List of <code>SourceJob</code>s that this SourceJob caused to load.
     * These <code>SourceJob</code>s are typically loaded with this 
     * Job references a class not already loaded.
     * 
     * We maintain the invariant that for all suitable <code>i</code>,
     * <code>((SourceJob)this.children.get(i)).parent == this</code>. 
     */
    protected List children;


    /** 
     * Constructor 
     */
    public SourceJob(ExtensionInfo lang, 
                     JobExt ext, 
                     SourceJob parent, 
                     Source source, 
                     Node ast) {
        super(lang, ext, ast);

        this.source = source;
        this.parent = parent;
        this.children = new LinkedList();

        if (parent != null) {
            parent.children().add(this);
        }
    }

    /**
     * Change the parent of this <code>SourceJob</code> to be parent. 
     * <code>newParent</code> may be null.
     */
    public void reparent(SourceJob newParent) {
        this.parent = newParent;
        if (this.parent != null) {
            this.parent.children().add(this);
        }
    }

    public SourceJob parent() {
        return parent;
    }

    public List children() {
        return children;
    }

    /**
     * The initial list of passes is just the list that the language extension
     * provides us with.
     */
    public List getPasses() {
        return lang.passes(this);
    }

    public Source source() {
        return source;
    }

    public SourceJob sourceJob() {
	return this;
    }

    public String toString() {
        return source.toString() + " (" +
            (completed() ? "done"
                    : ((isRunning() ? "running "
                                    : "before ") + nextPass())) + ")";
    }
}
