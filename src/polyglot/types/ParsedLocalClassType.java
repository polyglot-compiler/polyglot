package polyglot.types;

/**
 * A <code>ParsedLocalClassType</code> is a parsed local class type.
 */
public interface ParsedLocalClassType extends ParsedInnerClassType,
                                              LocalClassType
{
    /**
     * Set the name of the class.
     */
    void name(String name);

    /**
     * A unique identifier for this type.
     */
    int uid();
}
