package jltools.types;

import jltools.util.*;
import java.io.*;

/**
 * A <code>TypeObject</code> is an entity created by the type system.
 */
public interface TypeObject extends Copy, Serializable
{
    boolean isCanonical();
    TypeSystem typeSystem();
    Position position();
    TypeObject restore() throws SemanticException;
}
