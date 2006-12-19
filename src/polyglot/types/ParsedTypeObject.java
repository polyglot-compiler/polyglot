/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * ParsedTypeObject.java
 * 
 * Author: nystrom
 * Creation date: Feb 6, 2005
 */
package polyglot.types;

import polyglot.frontend.Job;

/**
 * Comment for <code>ParsedTypeObject</code>
 *
 * @author nystrom
 */
public interface ParsedTypeObject extends Named {
    /** Get the job (i.e., compilation unit) associated with this class; or null. */
    Job job();

    LazyInitializer initializer();
    void setInitializer(LazyInitializer init);
}
