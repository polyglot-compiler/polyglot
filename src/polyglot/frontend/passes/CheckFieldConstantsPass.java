/*
 * DisambiguateSignaturesPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.FieldConstantsChecked;
import polyglot.types.ParsedClassType;


public class CheckFieldConstantsPass extends ClassFilePass {
    Scheduler scheduler;
    FieldConstantsChecked goal;
  
    public CheckFieldConstantsPass(Scheduler scheduler, FieldConstantsChecked goal) {
        super(goal);
        this.scheduler = scheduler;
        this.goal = goal;
    }
    
    public boolean run() {
        ParsedClassType ct = goal.container();
        ct.fields();
        return true;
    }
}