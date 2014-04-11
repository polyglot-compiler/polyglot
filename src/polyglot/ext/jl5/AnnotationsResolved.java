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

package polyglot.ext.jl5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.visit.ResolveAnnotationsVisitor;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.ClassTypeGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.frontend.passes.ClassFilePass;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;

/**
 * This goal ensures that all AnnotatedElements (i.e., AST nodes that can
 * have annotations applied to them, such as class declarations, method declarations, etc.)
 * have their setAnnotations(Annotations) method called with an appropriate Annotations object.
 * That is, the AnnotationElems are type checked, and converted into an Annotations object,
 * which is then given to the AnnotatedElement. 
 */
public class AnnotationsResolved extends VisitorGoal {
    public static Goal create(Scheduler scheduler, ParsedClassType ct) {
        if (ct.job() != null) {
            return create(scheduler, ct.job());
        }
        return scheduler.internGoal(new AnnotationsResolvedCT(ct));
    }

    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new AnnotationsResolved(job));
    }

    protected AnnotationsResolved(Job job) {
        super(job, new ResolveAnnotationsVisitor(job));
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.Disambiguated(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    static class AnnotationsResolvedCT extends ClassTypeGoal {
        protected AnnotationsResolvedCT(ParsedClassType ct) {
            super(ct);
        }

        @Override
        public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
            List<Goal> l = new ArrayList<>();
            l.add(scheduler.SignaturesResolved(ct));
            l.addAll(super.prerequisiteGoals(scheduler));
            return l;
        }

        @Override
        public Pass createPass(ExtensionInfo extInfo) {
            return new ResolveAnnotationsForClass(extInfo.scheduler(), this);
        }

        protected boolean isGlobal(ClassType ct) {
            return ct.isTopLevel()
                    || (ct.isMember() && isGlobal((ClassType) ct.container()));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof AnnotationsResolvedCT && super.equals(o);
        }

    }

    static class ResolveAnnotationsForClass extends ClassFilePass {
        protected Scheduler scheduler;
        protected AnnotationsResolvedCT goal;

        public ResolveAnnotationsForClass(Scheduler scheduler,
                AnnotationsResolvedCT goal) {
            super(goal);
            this.scheduler = scheduler;
            this.goal = goal;
        }

        @Override
        public boolean run() {
            JL5ParsedClassType ct = (JL5ParsedClassType) goal.type();
            ct.annotations();
            ct.setAnnotationsResolved(true);
            return true;
        }
    }

}
