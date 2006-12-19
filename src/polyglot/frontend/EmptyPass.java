/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import polyglot.frontend.goals.Goal;

/**
 * An <code>EmptyPass</code> does nothing.
 */
public class EmptyPass extends AbstractPass
{
    public EmptyPass(Goal goal) {
      	super(goal);
    }

    public boolean run() {
        return true;
    }
}
