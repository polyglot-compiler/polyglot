package jltools.types;

/**
 * A <code>TopLevelClassType</code> represents a top-level class.
 */
public interface ParsedTopLevelClassType extends ParsedClassType,
                                                 TopLevelClassType
{
    void name(String name);
}
