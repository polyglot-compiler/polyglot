/*
 * MembersAdded.java
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
import polyglot.frontend.passes.AddMembersPass;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.TypeBuilder;

/**
 * Comment for <code>MembersAdded</code>
 *
 * @author nystrom
 */
public class MembersAdded extends ClassTypeGoal {
    public MembersAdded(ParsedClassType ct) {
        super(ct);
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
//          TypeSystem ts = extInfo.typeSystem();
//          NodeFactory nf = extInfo.nodeFactory();
//          return new VisitorPass(this, new TypeBuilder(job(), ts, nf));
            return new EmptyPass(this) {
                public boolean run() {
                    if (! MembersAdded.this.type().membersAdded()) {
                        throw new SchedulerException();
                    }
                    return true;
                }
            };
        }
        return new AddMembersPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList(super.prerequisiteGoals(scheduler));
        if (ct.job() != null) {
            l.add(scheduler.Parsed(ct.job()));
        }
        return l;
    }

    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList(super.corequisiteGoals(scheduler));
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        return l;
    }
    
    public boolean equals(Object o) {
        return o instanceof MembersAdded && super.equals(o);
    }
}
