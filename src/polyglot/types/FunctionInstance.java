/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 2007 IBM Corporation
 * 
 */

package polyglot.types;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.CollectionUtil;

/**
 * A <code>FunctionInstance</code> represents the type information for a
 * function.
 */
public interface FunctionInstance extends ProcedureInstance
{
    /**
     * The functions's return type.
     */
    Type returnType();
    
    /**
     * Destructively set the functions's return type.
     * @param type
     */
    void setReturnType(Type type);
}
