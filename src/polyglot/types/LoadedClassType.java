package jltools.types;

import java.util.List;
import jltools.util.Position;

/**
 * A <code>LoadedClassType</code> represents a class loaded from a class file.
 */
public interface LoadedClassType extends ClassType
{
    Class theClass();
}
