package jltools.types;

/**
 * A <code>ParsedLocalClassType</code> is a parsed local class type.
 */
public interface ParsedLocalClassType extends ParsedInnerClassType,
                                              LocalClassType
{
    void name(String name);
    int uid();
}
