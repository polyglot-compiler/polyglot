/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * CompileGoal.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

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
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new CodeGenerated(job));
    }

    /**
     * @param job The job to compile.
     */
    protected CodeGenerated(Job job) {
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
        l.add(scheduler.Serialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
    
}
