package polyglot.types;

/**
 * A <code>ParsedTopLevelClassType</code> represents a parsed top-level class.
 */
public interface ParsedTopLevelClassType extends ParsedClassType,
                                                 TopLevelClassType
{
    void name(String name);
}
