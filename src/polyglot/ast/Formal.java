package polyglot.ast;

import polyglot.types.Type;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public interface Formal extends Node
{
    /** Get the type object for the declaration's type. */
    Type declType();

    /** Get the declaration's flags. */
    Flags flags();
    /** Set the declaration's flags. */
    Formal flags(Flags flags);

    /** Get the declaration's type. */
    TypeNode type();
    /** Set the declaration's type. */
    Formal type(TypeNode type);

    /** Get the declaration's name. */
    String name();
    /** Set the declaration's name. */
    Formal name(String name);

    /**
     * Get the type object for the local we are declaring.  This field may
     * not be valid until after signature disambiguation.
     */
    LocalInstance localInstance();

    /**
     * Set the type object for the local we are declaring.
     */
    Formal localInstance(LocalInstance li);
}
