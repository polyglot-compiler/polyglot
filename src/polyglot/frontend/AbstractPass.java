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
import polyglot.util.StringUtil;

/** The base class for most passes. */
public abstract class AbstractPass implements Pass {
    protected Goal goal;

    /**
     * If the pass is running, the time that the pass started.
     * If the pass has completed, the time in ms the pass took to run,
     * excluding spawned passes.
     */
    protected long exclusive_time = 0;

    /**
     * If the pass is running, the time that the pass started.
     * If the pass has completed, the time in ms the pass took to run,
     * including spawned passes.
     */
    protected long inclusive_time = 0;

    public AbstractPass(Goal goal) {
        this.goal = goal;
    }

    @Override
    public Goal goal() {
        return goal;
    }

    /** The human-readable name of the pass. */
    @Override
    public String name() {
        return StringUtil.getShortNameComponent(this.getClass().getName());
    }

    /** Run the pass, returning true on success. */
    @Override
    public abstract boolean run();

    /** Start or stop the pass timer. */
    @Override
    public void toggleTimers(boolean exclusive_only) {
        // How this works:
        // reset: time = 0
        // start: time = T - 0 = T
        //  stop: time = T' - T = delta1
        // start: time = T'' - delta1 = T'' - T' + T
        //  stop: time = T''' - (T'' - T' + T) = delta2 + delta1
        if (!exclusive_only) {
            inclusive_time = System.currentTimeMillis() - inclusive_time;
        }
        exclusive_time = System.currentTimeMillis() - exclusive_time;
    }

    /** Reset the pass timer. */
    @Override
    public void resetTimers() {
        inclusive_time = 0;
        exclusive_time = 0;
    }

    /** Return the time in ms taken to run the pass, excluding the time in
     * spawned passes */
    @Override
    public long exclusiveTime() {
        return exclusive_time;
    }

    /** Return the time in ms taken to run the pass, including the time in
     * spawned passes */
    @Override
    public long inclusiveTime() {
        return inclusive_time;
    }

    @Override
    public String toString() {
        return name();
    }
}
