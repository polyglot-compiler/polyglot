/*
 * DisambiguateSignaturesPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.Scheduler;
import polyglot.frontend.SchedulerException;
import polyglot.frontend.goals.FieldConstantsChecked;
import polyglot.types.ParsedClassType;
import polyglot.types.FieldInstance;


public class CheckFieldConstantsPass extends ClassFilePass {
    protected Scheduler scheduler;
    protected FieldConstantsChecked goal;
  
    public CheckFieldConstantsPass(Scheduler scheduler, FieldConstantsChecked goal) {
        super(goal);
        this.scheduler = scheduler;
        this.goal = goal;
    }
    
    public boolean run() {
        // Force fields of the container to be initialized.
        goal.container().fields();

        FieldInstance fi = goal.var();
        if (! fi.constantValueSet()) {
            throw new SchedulerException();
        }
        return true;
    }
}
