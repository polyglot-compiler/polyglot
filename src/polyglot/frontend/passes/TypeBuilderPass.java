/*
 * DisambiguationPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 22, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.frontend.goals.TypeExists;
import polyglot.visit.TypeBuilder;

/**
 * Comment for <code>DisambiguationPass</code>
 *
 * @author nystrom
 */
public class TypeBuilderPass extends VisitorPass {
    Job job;
    
    public TypeBuilderPass(TypesInitialized goal, TypeBuilder v) {
        this((Goal) goal, v);
    }
    
    public TypeBuilderPass(TypeExists goal, TypeBuilder v) {
        this((Goal) goal, v);
    }
    
    public TypeBuilderPass(MembersAdded goal, TypeBuilder v) {
        this((Goal) goal, v);
    }
    
    private TypeBuilderPass(Goal goal, TypeBuilder v) {
        super(goal, v);
        this.job = v.job();
    }

    public void markGoalReached() {
        Scheduler scheduler = job.extensionInfo().scheduler();
        TypesInitialized g = (TypesInitialized) scheduler.internGoal(new TypesInitialized(job));
        g.markReached();
    }
}
