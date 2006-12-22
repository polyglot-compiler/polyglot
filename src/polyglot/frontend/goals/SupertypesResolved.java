/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * SuperTypesResolved.java
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
 * Comment for <code>SuperTypesResolved</code>
 *
 * @author nystrom
 */
public class SupertypesResolved extends ClassTypeGoal {
    public static Goal create(Scheduler scheduler, ParsedClassType ct) {
        return scheduler.internGoal(new SupertypesResolved(ct));
    }

    protected SupertypesResolved(ParsedClassType ct) {
        super(ct);
    }
    
    protected static class SupertypesResolvedPass extends AbstractPass {
        SupertypesResolvedPass(Goal goal) {
            super(goal);
        }
        
        public boolean run() {
            SupertypesResolved goal = (SupertypesResolved) this.goal;
            if (! goal.type().supertypesResolved()) {
                throw new SchedulerException();
            }
            return true;
        }
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            return new SupertypesResolvedPass(this);
        }
        return new ResolveSuperTypesPass(extInfo.scheduler(), this);
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.MembersAdded(ct));
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
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
                l.add(scheduler.SupertypesDisambiguated(ct.job()));
            }
            else {
                l.add(scheduler.Disambiguated(ct.job()));
            }
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }
    
    public boolean equals(Object o) {
        return o instanceof SupertypesResolved && super.equals(o);
    }
}
