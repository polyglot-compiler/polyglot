/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * SignaturesDisambiguated
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
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
