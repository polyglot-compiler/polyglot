/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

import java.io.Serializable;

import polyglot.util.CannotResolvePlaceHolderException;

/**
 * A place holder used to serialize type objects that cannot be serialized.  
 */
public interface PlaceHolder extends Serializable {
    /**
     * Resolve the place holder into a TypeObject.  This method
     * should <strong>never</strong> throw a SchedulerException.
     * Instead, it should return null if the object cannot be resolved 
     * until after another pass runs.  The method is responsible for setting
     * up the appropriate dependencies to reattempt the current goal.
     */
    TypeObject resolve(TypeSystem ts) throws CannotResolvePlaceHolderException;
}
