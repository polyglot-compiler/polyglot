package polyglot.types;

/**
 * A <code>ParsedTopLevelClassType</code> represents a parsed top-level class.
 */
public interface ParsedTopLevelClassType extends ParsedClassType,
                                                 TopLevelClassType
{
    /**
     * Set the name of the class.
     */
    void name(String name);
}
