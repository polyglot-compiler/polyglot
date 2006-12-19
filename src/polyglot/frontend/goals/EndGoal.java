/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * EndGoal.java
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.Translator;

/**
 * The <code>EndGoal</code> interface is used to tag the last goal for a
 * job.  When an EndGoal is reached, the job can be removed from the
 * scheduler.
 *
 * @author nystrom
 */
public interface EndGoal extends Goal {
}
