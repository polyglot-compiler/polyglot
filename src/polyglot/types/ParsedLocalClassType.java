package jltools.types;

/**
 * A <code>NamedClassType</code> is a class type with a name: either a
 * top-level class, a member class, or a local class, but not an anonymous
 * class.
 */
public interface ParsedLocalClassType extends ParsedInnerClassType,
                                              LocalClassType
{
    void name(String name);
    int uid();
}
