package polyglot.types;

import polyglot.util.Position;

/**
 * A <code>ParsedInnerClassType</code> represents a parsed inner class.
 */
public interface ParsedInnerClassType extends ParsedClassType, InnerClassType
{
    /**
     * Set the class's outer class.
     */
    void outer(ClassType t);
}
