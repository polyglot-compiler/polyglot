/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

/**
 * A <code>LocalInstance</code> contains type information for a local variable.
 */
public interface LocalInstance extends VarInstance
{
    LocalInstance flags(Flags flags);
    LocalInstance name(String name);
    LocalInstance type(Type type);   
    LocalInstance constantValue(Object value);
    LocalInstance notConstant();
    LocalInstance orig();
}
