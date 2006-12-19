/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * ImportTableInitialized.java
 * 
 * Author: nystrom
 * Creation date: Oct 11, 2005
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.types.TypeSystem;
import polyglot.visit.InitImportsVisitor;

public class ImportTableInitialized extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new ImportTableInitialized(job, ts, nf));
    }

    protected ImportTableInitialized(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new InitImportsVisitor(job, ts, nf));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.TypesInitialized(job));
        l.add(scheduler.TypesInitializedForCommandLine());
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
