package polyglot.types;

import java.util.List;
import polyglot.util.Position;

/**
 * A <code>ParsedInnerClassType</code> represents a parsed inner class.
 */
public interface ParsedInnerClassType extends ParsedClassType, InnerClassType
{
    void outer(ClassType t);
}
