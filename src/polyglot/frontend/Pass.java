package jltools.frontend;

import jltools.util.Enum;
import java.util.*;

/** A <code>Pass</code> represents a compiler pass. */
public interface Pass
{
    /** The status of the pass. */
    public static class Status extends Enum {
	Status(String name) { super(name); }
    }

    /** The pass is new and has not been added to the run queue. */
    public static final Status NEW      = new Status("new");
    /** The pass is in the run queue. */
    public static final Status ENQUEUED = new Status("enqueued");
    /** The pass is running. */
    public static final Status RUNNING  = new Status("running");
    /** The pass has run and has failed.  It may still be rerun. */
    public static final Status FAILED   = new Status("failed");
    /** The pass has run and has succeeded.  It may still be rerun. */
    public static final Status DONE     = new Status("done");

    /** Get the status of the pass. */
    public Status status();

    /** Set the status of the pass. */
    public void status(Status status);

    /** Return true if the pass should be rerun. */
    public boolean repeat();

    /** Reinitialize the pass so it can be rerun. */
    public void reinit();

    /** Return the list of passes which we must run after. */
    public List runAfter();

    /** Run this pass after <code>pass</code>.  This will cause the pass to be rerun if it is currently running. */
    public void runAfter(Pass pass);

    /** Run the pass. */
    public boolean run();
}
