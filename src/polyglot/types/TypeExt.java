package polyglot.types;

import polyglot.util.*;
import java.io.*;

/**
 * A <code>TypeExt</code> is an extension object for <code>TypeObject</code>s.
 */
public interface TypeExt extends Copy, Serializable
{
    /**
     * Return the TypeObject we are extending.
     */
    TypeObject base();

    /**
     * Initialize this extension with the TypeObject.
     */
    void init(TypeObject base);

    /**
     * Restore the extension after de-serialization.
     */
    TypeExt restore() throws SemanticException;
}
