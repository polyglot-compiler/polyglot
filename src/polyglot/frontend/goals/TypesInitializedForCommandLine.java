/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * TypesInitializedForCommandLine.java
 * 
 * Author: nystrom
 * Creation date: Oct 11, 2005
 */
package polyglot.frontend.goals;

import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;

public class TypesInitializedForCommandLine extends Barrier {
    public static Goal create(Scheduler scheduler) {
        return scheduler.internGoal(new TypesInitializedForCommandLine(scheduler));
    }

    protected TypesInitializedForCommandLine(Scheduler scheduler) {
        super("TYPES_INIT_BARRIER", scheduler);
    }
    
    public Goal goalForJob(Job j) {
        return scheduler.TypesInitialized(j);
    }
}
