package polyglot.types;

import polyglot.util.*;
import java.io.*;

/**
 * A <code>TypeObject</code> is an entity created by the type system.
 */
public interface TypeExt extends Copy, Serializable
{
    TypeObject base();
    void init(TypeObject base);

    TypeExt restore() throws SemanticException;
}
