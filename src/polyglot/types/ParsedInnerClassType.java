package jltools.types;

import java.util.List;
import jltools.util.Position;

/**
 * A <code>ClassType</code> represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A <code>ClassType</code> is not necessarily named.
 */
public interface ParsedInnerClassType extends ParsedClassType, InnerClassType
{
    void outer(ClassType t);
}
