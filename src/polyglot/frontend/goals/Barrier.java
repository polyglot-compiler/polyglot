/*
 * Barrier.java
 * 
 * Author: nystrom
 * Creation date: Feb 4, 2005
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.Collection;
import java.util.Iterator;

import polyglot.frontend.*;
import polyglot.util.InternalCompilerError;

/**
 * Comment for <code>Barrier</code>
 *
 * @author nystrom
 */
public abstract class Barrier extends AbstractGoal {
    Scheduler scheduler;
    
    public Barrier(Scheduler scheduler) {
        super(null);
        this.scheduler = scheduler;
    }

    public Barrier(String name, Scheduler scheduler) {
        super(null, name);
        this.scheduler = scheduler;
    }

    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#createPass(polyglot.frontend.ExtensionInfo)
     */
    public Pass createPass(ExtensionInfo extInfo) {
        return new EmptyPass(this);
    }
    
    /* (non-Javadoc)
     * @see polyglot.frontend.goals.Goal#reached()
     */
    public int distanceFromGoal() {
        int distance = 0;
        
        for (Iterator i = scheduler.jobs().iterator(); i.hasNext(); ) {
            Job job = (Job) i.next();
            Goal subgoal = goalForJob(job);
            if (! scheduler.reached(subgoal)) {
                try {
                    scheduler.addPrerequisiteDependency(this, subgoal);
                    distance++;
                }
                catch (CyclicDependencyException e) {
                    throw new InternalCompilerError(e.getMessage());
                }
            }
        }
        
        return distance;
    }

//    public Collection prerequisiteGoals(Scheduler scheduler) {
//        List l = new ArrayList();
//        l.addAll(super.prerequisiteGoals(scheduler));
//        
//        for (Iterator i = scheduler.jobs().iterator(); i.hasNext(); ) {
//            Job job = (Job) i.next();
//            Goal subgoal = goalForJob(job);
//            l.add(subgoal);
//        }
//        
//        return l;
//    }
    
    public abstract Goal goalForJob(Job job); 

    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }
    
    public int hashCode() {
        if (name == null) {
            return System.identityHashCode(this);
        }
        else {
            return name.hashCode();
        }
    }

    public boolean equals(Object o) {
        if (name == null) {
            return this == o;
        }
        else if (o instanceof Barrier) {
            Barrier b = (Barrier) o;
            return name.equals(b.name);
        }
        return false;
    }
}
