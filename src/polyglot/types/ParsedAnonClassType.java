package jltools.types;

/**
 * A <code>ParsedAnonClassType</code> represents a parsed anonymous class.
 */
public interface ParsedAnonClassType extends ParsedInnerClassType, AnonClassType
{
    int uid();
}
