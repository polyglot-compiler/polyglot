/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

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
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.main.Report;


public class TypeExistsPass extends ClassFilePass {
    protected Scheduler scheduler;
    protected TypeExists goal;
    protected TypeSystem ts;
    
    public TypeExistsPass(Scheduler scheduler, TypeSystem ts, TypeExists goal) {
        super(goal);
        this.scheduler = scheduler;
        this.ts = ts;
        this.goal = goal;
    }
    
    public boolean run() {
        String name = goal.typeName();
        try {
            // Try to resolve the type; this may throw a
            // MissingDependencyException on the job to load the file
            // containing the type.
            Named n = ts.systemResolver().find(name);
            if (n instanceof Type) {
                return true;
            }
        }
        catch (SemanticException e) {
            ErrorQueue eq = ts.extensionInfo().compiler().errorQueue();
            eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), e.position());
        }
        return false;
    }
}
