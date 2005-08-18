/*
 * TypeExistsPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.TypeExists;
import polyglot.types.*;


public class TypeExistsPass extends ClassFilePass {
    Scheduler scheduler;
    TypeExists goal;
    TypeSystem ts;
    
    public TypeExistsPass(Scheduler scheduler, TypeSystem ts, TypeExists goal) {
        super(goal);
        this.scheduler = scheduler;
        this.ts = ts;
        this.goal = goal;
    }
    
    public boolean run() {
        String name = goal.typeName();
        try {
            Type t = ts.typeForName(name);
        }
        catch (SemanticException e) {
            return false;
        }
        return true;
    }
}