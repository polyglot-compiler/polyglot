/*
 * SuperTypesResolved.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.*;
import polyglot.frontend.passes.ResolveSuperTypesPass;
import polyglot.frontend.passes.TypeCheckPass;
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
    public SupertypesResolved(ParsedClassType ct) {
        super(ct);
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
//            TypeSystem ts = extInfo.typeSystem();
//            NodeFactory nf = extInfo.nodeFactory();
//            return new DisambiguatorPass(this, new AmbiguityRemover(job(), ts, nf, false, false));
            return new EmptyPass(this) {
                public boolean run() {
                    if (! SupertypesResolved.this.type().supertypesResolved()) {
                        throw new SchedulerException();
                    }
                    return true;
                }
            };
        }
        return new ResolveSuperTypesPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList(super.prerequisiteGoals(scheduler));
        l.add(scheduler.MembersAdded(ct));
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        return l;
    }

    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList(super.corequisiteGoals(scheduler));
        if (ct.job() != null) {
            l.add(scheduler.Disambiguated(ct.job()));
        }
        return l;
    }
    
    public boolean equals(Object o) {
        return o instanceof SupertypesResolved && super.equals(o);
    }
}
