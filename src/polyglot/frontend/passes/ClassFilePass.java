/*
 * ClassFilePass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.AbstractPass;
import polyglot.frontend.goals.Goal;



public abstract class ClassFilePass extends AbstractPass {
    public ClassFilePass(Goal goal) {
        super(goal);
    }
}