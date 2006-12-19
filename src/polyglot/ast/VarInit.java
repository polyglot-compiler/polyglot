/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * VarInit.java
 * 
 * Author: nystrom
 * Creation date: Feb 4, 2005
 */
package polyglot.ast;

import polyglot.types.VarInstance;

/**
 * <code>VarInit</code> should be implemented by any node that could declare and
 * initialize a variable or field.
 */
public interface VarInit {
    /** The variable being initialized. */
    VarInstance varInstance();
    
    /** Whether the initializer has been determined to be constant or not. */
    boolean constantValueSet();
}
