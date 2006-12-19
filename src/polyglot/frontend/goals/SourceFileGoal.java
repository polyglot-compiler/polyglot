/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * SourceFileGoal.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.goals;

import polyglot.frontend.Job;


/**
 * Comment for <code>SourceFileGoal</code>
 *
 * @author nystrom
 */
public abstract class SourceFileGoal extends AbstractGoal {
    protected SourceFileGoal(Job job) {
        super(job);
    }

    protected SourceFileGoal(Job job, String name) {
        super(job, name);
    }
}
