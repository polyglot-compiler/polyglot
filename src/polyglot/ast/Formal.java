package jltools.ast;

import jltools.types.Type;
import jltools.types.Flags;
import jltools.types.LocalInstance;
import jltools.types.SemanticException;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public interface Formal extends Node
{
    Type declType();

    Flags flags();
    Formal flags(Flags flags);

    TypeNode type();
    Formal type(TypeNode type);

    String name();
    Formal name(String name);

    LocalInstance localInstance();
    Formal localInstance(LocalInstance li);
}
