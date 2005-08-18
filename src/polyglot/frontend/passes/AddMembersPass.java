/*
 * AddMembersPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.MembersAdded;
import polyglot.types.ParsedClassType;

public class AddMembersPass extends ClassFilePass {
    Scheduler scheduler;
    MembersAdded goal;
    
    public AddMembersPass(Scheduler scheduler, MembersAdded goal) {
        super(goal);
        this.scheduler = scheduler;
        this.goal = goal;
    }
    
    public boolean run() {
        ParsedClassType ct = goal.type();
        // force the members to get initialized.
        ct.fields();
        ct.methods();
        ct.constructors();
        ct.memberClasses();
        ct.setMembersAdded(true);
        return true;
    }
}