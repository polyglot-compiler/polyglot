/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend;

import polyglot.frontend.goals.Goal;

/** A <code>Pass</code> represents a compiler pass that runs on a
 * <code>Job</code>. All work in the compiler is done by passes, which are
 * scheduled by the scheduler (<code>Scheduler</code>) to satisfy goals
 * (<code>Goal</code>).
 * 
 */
public interface Pass {
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
