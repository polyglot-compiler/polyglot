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

    /**
     * Return the object's extension.
     */
    TypeExt ext();

    /**
     * Set the object's extension, returning a new version of this node.
     */
    TypeObject ext(TypeExt ext); // non-destructive update

    /**
     * Destructively set the object's extension.
     */
    void setExt(TypeExt ext);    // destructive update
}
