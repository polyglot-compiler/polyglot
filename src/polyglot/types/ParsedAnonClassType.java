package jltools.types;

/**
 * An <code>AnonClassType</code> represents an anonymous class.
 */
public interface ParsedAnonClassType extends ParsedInnerClassType, AnonClassType
{
    int uid();
}
