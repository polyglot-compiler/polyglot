/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * ResolveSuperTypesPass.java
 * 
 * Author: nystrom
 * Creation date: Jan 21, 2005
 */
package polyglot.frontend.passes;

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.SupertypesResolved;
import polyglot.types.ParsedClassType;
import polyglot.util.InternalCompilerError;

/**
 * A pass that resolves the supertype and interfaces of every parsed class type.  
 */
public class ResolveSuperTypesPass extends ClassFilePass {
    protected Scheduler scheduler;
    protected SupertypesResolved goal;
    
    public ResolveSuperTypesPass(Scheduler scheduler, SupertypesResolved goal) {
        super(goal);
        this.scheduler = scheduler;
        this.goal = goal;
    }
    
    public boolean run() {
        ParsedClassType ct = goal.type();
        ct.superType();
        ct.interfaces();
        ct.setSupertypesResolved(true);
        return true;
    }
}
