/*
 * CompileGoal.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.Translator;

/**
 * Comment for <code>CompileGoal</code>
 *
 * @author nystrom
 */
public class CodeGenerated extends SourceFileGoal implements EndGoal {

    /**
     * @param job The job to compile.
     */
    public CodeGenerated(Job job) {
        super(job);
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new OutputPass(this, new Translator(job(), ts, nf,
                                                   extInfo.targetFactory()));
    }
    
    public Collection prerequisiteGoals(Scheduler scheduler) {
        List l = new ArrayList();
        l.addAll(super.prerequisiteGoals(scheduler));
        l.add(scheduler.Serialized(job));
        return l;
    }
    
}
