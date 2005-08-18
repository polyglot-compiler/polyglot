/*
 * SignaturesDisambiguated
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;


import java.util.*;
import java.util.ArrayList;
import java.util.Collection;

import polyglot.ast.NodeFactory;
import polyglot.ext.jx.VirtualTypeBoundResolved;
import polyglot.ext.jx.types.ExplicitVirtualClassType;
import polyglot.frontend.*;
import polyglot.frontend.passes.*;
import polyglot.frontend.passes.DisambiguateSignaturesPass;
import polyglot.frontend.passes.TypeCheckPass;
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
    public SignaturesResolved(ParsedClassType ct) {
        super(ct);
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
//          TypeSystem ts = extInfo.typeSystem();
//          NodeFactory nf = extInfo.nodeFactory();
//          return new DisambiguatorPass(this, new AmbiguityRemover(job(), ts, nf, true, false));
            return new EmptyPass(this) {
                public boolean run() {
                    if (! SignaturesResolved.this.type().signaturesResolved()) {
                        throw new SchedulerException();
                    }
                    return true;
                }
            };
        }
        return new DisambiguateSignaturesPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList(super.prerequisiteGoals(scheduler));
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        l.add(scheduler.SupertypesResolved(ct));
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
        return o instanceof SignaturesResolved && super.equals(o);
    }
}
