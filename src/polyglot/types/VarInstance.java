/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public interface VarInstance extends TypeObject, Declaration {
    /**
     * The flags of the variable.
     */
    Flags flags();

    /**
     * The name of the variable.
     */
    String name();

    void setName(String name);

    /**
     * The type of the variable.
     */
    Type type();

    /**
     * Whether the variable's constant value has been set yet.
     */
    boolean constantValueSet();

    /**
     * The variable's constant value, or null.
     */
    Object constantValue();

    /**
     * Destructively set the constant value of the field.
     * @param value the constant value.  Should be an instance of String,
     * Boolean, Byte, Short, Character, Integer, Long, Float, Double, or null.
     */
    void setConstantValue(Object value);

    /**
     * Mark the variable as not a compile time constant.
     */
    void setNotConstant();

    /**
     * Whether the variable has a constant value.
     */
    boolean isConstant();

    /**
     * Destructively set the type of the variable.
     * This method should be deprecated.
     */
    void setType(Type type); //destructive update   

    /**
     * Destructively set the flags of the variable.
     */
    void setFlags(Flags flags);

}
