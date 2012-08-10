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

package polyglot.frontend.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.VisitorPass;
import polyglot.types.TypeSystem;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;

public class SignaturesDisambiguated extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts,
            NodeFactory nf) {
        return scheduler.internGoal(new SignaturesDisambiguated(job, ts, nf));
    }

    protected SignaturesDisambiguated(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new AmbiguityRemover(job, ts, nf, true, false));
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<Goal>();
        l.add(scheduler.ImportTableInitialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        Scheduler scheduler = extInfo.scheduler();
        Goal allDisam = scheduler.Disambiguated(job);
        return new MyPass(this, allDisam, v);
    }

    /**
     * This class overrides VisitorPass to mark the SupertypesDisambiguated
     * and SignaturesDisambiguated goals reached when this goal is
     * reached.
     */
    protected static class MyPass extends VisitorPass {
        public Goal allDisam;

        public MyPass(Goal goal, Goal allDisam, NodeVisitor v) {
            super(goal, v);
            this.allDisam = allDisam;
        }

        @Override
        public boolean run() {
            if (allDisam.hasBeenReached()) {
                // If the goal to disambiguate the entire source file has
                // been reached, we don't need to run the visitor over
                // the AST.
                return true;
            }
            return super.run();
        }
    }

}
