package polyglot.frontend;

import polyglot.frontend.goals.Goal;


/** A <code>Pass</code> represents a compiler pass.
 * A <code>Job</code> runs a series of passes over the AST. 
 * Each pass has an ID that is used to identify similar passes across
 * several jobs.  For example, most jobs contain a pass named PARSE
 * that returns an AST for a source file and a pass TYPE_CHECK
 * that performs type checking on the job's AST.
 */
public interface Pass
{
    public static class ID { }
    /** The goal the pass is trying to satisfy. */
    public Goal goal();
    
    /** Return a user-readable name for the pass. */
    public String name();

    /** Run the pass. */
    public boolean run();

    /** Reset the pass timers to 0. */
    public void resetTimers();

    /** Start/stop the pass timers. */
    public void toggleTimers(boolean exclusive_only);

    /** The total accumulated time in ms since the last timer reset
      * that the pass was running, including spawned passes. */
    public long inclusiveTime();

    /** The total accumulated time in ms since the last timer reset
      * that the pass was running, excluding spawned passes. */
    public long exclusiveTime();
}
