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


import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.*;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>SignaturesDisambiguated</code>
 *
 * @author nystrom
 */
public class SignaturesResolved extends ClassTypeGoal {
    public static Goal create(Scheduler scheduler, ParsedClassType ct) {
        return scheduler.internGoal(new SignaturesResolved(ct));
    }

    protected SignaturesResolved(ParsedClassType ct) {
        super(ct);
    }
    
    protected static class SignaturesResolvedPass extends AbstractPass {
        SignaturesResolvedPass(Goal goal) {
            super(goal);
        }
        
        public boolean run() {
            SignaturesResolved goal = (SignaturesResolved) this.goal;
            if (! goal.type().signaturesResolved()) {
                throw new SchedulerException();
            }
            return true;
        }
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            return new SignaturesResolvedPass(this);
        }
        return new DisambiguateSignaturesPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        l.add(scheduler.SupertypesResolved(ct));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    protected boolean isGlobal(ParsedClassType ct) {
        return ct.isTopLevel() || (ct.isMember() && isGlobal((ParsedClassType) ct.container()));
    }
    
    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct.job() != null) {
            if (isGlobal(ct)) {
                l.add(scheduler.SignaturesDisambiguated(ct.job()));
            }
            else {
                l.add(scheduler.Disambiguated(ct.job()));
            }
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }
    
    public boolean equals(Object o) {
        return o instanceof SignaturesResolved && super.equals(o);
    }
}
