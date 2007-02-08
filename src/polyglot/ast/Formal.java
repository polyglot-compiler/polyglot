/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.types.Flags;
import polyglot.types.LocalInstance;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public interface Formal extends VarDecl
{
    /** Get the flags of the formal. */
    public Flags flags();

    /** Set the flags of the formal. */
    public Formal flags(Flags flags);
    
    /** Get the type node of the formal. */
    public TypeNode type();

    /** Set the type node of the formal. */
    public Formal type(TypeNode type);
    
    /** Get the name of the formal. */
    public Id id();
    
    /** Set the name of the formal. */
    public Formal id(Id name);

    /** Get the name of the formal. */
    public String name();
    
    /** Set the name of the formal. */
    public Formal name(String name);

    /** Get the local instance of the formal. */
    public LocalInstance localInstance();

    /** Set the local instance of the formal. */
    public Formal localInstance(LocalInstance li);
}
