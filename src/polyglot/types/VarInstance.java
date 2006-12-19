/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public interface VarInstance extends TypeObject, Declaration
{
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
