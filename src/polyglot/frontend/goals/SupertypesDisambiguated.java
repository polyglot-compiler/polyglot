/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
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

public class SupertypesDisambiguated extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new SupertypesDisambiguated(job, ts, nf));
    }

    protected SupertypesDisambiguated(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new AmbiguityRemover(job, ts, nf, false, false));
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.ImportTableInitialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        Scheduler scheduler = extInfo.scheduler();
        Goal sigDisam = scheduler.SignaturesDisambiguated(job);
        Goal allDisam = scheduler.Disambiguated(job);
        return new MyPass(this, sigDisam, allDisam, v);
    }

    /**
     * This class overrides VisitorPass to mark the SupertypesDisambiguated
     * and SupertypesDisambiguated goals reached when this goal is
     * reached.
     */
    protected static class MyPass extends VisitorPass {
        public Goal allDisam;
        public Goal sigDisam;

        public MyPass(Goal goal, Goal sigDisam, Goal allDisam, NodeVisitor v) {
            super(goal, v);
            this.sigDisam = sigDisam;
            this.allDisam = allDisam;
        }

        @Override
        public boolean run() {
            if (sigDisam.hasBeenReached() || allDisam.hasBeenReached()) {
                // If the goal to disambiguate the entire source file has
                // been reached, we don't need to run the visitor over
                // the AST.
                return true;
            }
            return super.run();
        }
    }
}
