/*
 * Serialized.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.Compiler;
import polyglot.types.TypeSystem;
import polyglot.visit.ClassSerializer;
import polyglot.visit.NodeVisitor;


public class Serialized extends SourceFileGoal {
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new Serialized(job));
    }

    protected Serialized(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        Compiler compiler = extInfo.compiler();
        if (compiler.serializeClassInfo()) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new VisitorPass(this,
                                   new ClassSerializer(ts,
                                                       nf,
                                                       job().source().lastModified(),
                                                       compiler.errorQueue(),
                                                       extInfo.version()));
        }
        else {
            return new EmptyPass(this);
        }
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.add(scheduler.TypeChecked(job));
        l.add(scheduler.ConstantsChecked(job));
        l.add(scheduler.ReachabilityChecked(job));
        l.add(scheduler.ExceptionsChecked(job));
        l.add(scheduler.ExitPathsChecked(job));
        l.add(scheduler.InitializationsChecked(job));
        l.add(scheduler.ConstructorCallsChecked(job));
        l.add(scheduler.ForwardReferencesChecked(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}
