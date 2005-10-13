/*
 * MembersAdded.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

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
    public static Goal create(Scheduler scheduler, ParsedClassType ct) {
        return scheduler.internGoal(new MembersAdded(ct));
    }

    protected MembersAdded(ParsedClassType ct) {
        super(ct);
    }

    protected static class MembersAddedPass extends AbstractPass {
        MembersAddedPass(Goal goal) {
            super(goal);
        }
        
        public boolean run() {
            MembersAdded goal = (MembersAdded) this.goal;
            if (! goal.type().membersAdded()) {
                throw new SchedulerException();
            }
            return true;
        }
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            return new MembersAddedPass(this);
        }
        return new AddMembersPass(extInfo.scheduler(), this);
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct.job() != null) {
            l.add(scheduler.Parsed(ct.job()));
        }
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    public Collection corequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        if (ct.job() != null) {
            l.add(scheduler.TypesInitialized(ct.job()));
        }
        l.addAll(super.corequisiteGoals(scheduler));
        return l;
    }
    
    public boolean equals(Object o) {
        return o instanceof MembersAdded && super.equals(o);
    }
}
