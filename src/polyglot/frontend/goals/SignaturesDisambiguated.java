/*
 * Disambiguated.java
 * 
 * Author: nystrom
 * Creation date: Oct 11, 2005
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;

public class SignaturesDisambiguated extends VisitorGoal {
    public static Goal create(Scheduler scheduler, Job job, TypeSystem ts, NodeFactory nf) {
        return scheduler.internGoal(new SignaturesDisambiguated(job, ts, nf));
    }

    protected SignaturesDisambiguated(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, new AmbiguityRemover(job, ts, nf, true, false));
    }

    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.ImportTableInitialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        Scheduler scheduler = extInfo.scheduler();
        Goal allDisam = scheduler.Disambiguated(job);
        return new MyPass(this, allDisam, v);
    }

    /**
     * This class overrides VisitorPass to mark the SupertypesDisambiguated
     * and SignaturesDisambiguated goals reached when this goal is
     * reached.
     */
    protected static class MyPass extends VisitorPass {
        Goal allDisam;

        public MyPass(Goal goal, Goal allDisam, NodeVisitor v) {
            super(goal, v);
            this.allDisam = allDisam;
        }

        public boolean run() {
            if (allDisam.hasBeenReached()) {
                // If the goal to disambiguate the entire source file has
                // been reached, we don't need to run the visitor over
                // the AST.
                return true;
            }
            return super.run();
        }
    }

}
