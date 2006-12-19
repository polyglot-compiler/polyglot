/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import polyglot.frontend.goals.Goal;


/** A <code>Pass</code> represents a compiler pass.
 * A <code>Job</code> runs a series of passes over the AST. 
 * A pass is run to attempt to satisfy a goal.
 */
public interface Pass
{
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
