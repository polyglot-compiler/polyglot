package polyglot.types;

/**
 * A <code>ParsedAnonClassType</code> represents a parsed anonymous class.
 */
public interface ParsedAnonClassType extends ParsedInnerClassType, AnonClassType
{
    /**
     * A unique identifier for this type.
     */
    int uid();
}
