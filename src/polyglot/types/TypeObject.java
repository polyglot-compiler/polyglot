package polyglot.types;

import polyglot.util.*;
import java.io.*;

/**
 * A <code>TypeObject</code> is an entity created by the type system.
 */
public interface TypeObject extends Copy, Serializable
{
    /**
     * Return true if the type object contains no unknown/ambiguous types.
     */
    boolean isCanonical();

    /**
     * The object's type system.
     */
    TypeSystem typeSystem();

    /**
     * The object's position, or null.
     */
    Position position();

    /**
     * Restore the type object after de-serialization.
     */
    TypeObject restore() throws SemanticException;
}
