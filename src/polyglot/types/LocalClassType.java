package jltools.types;

/**
 * A <code>NamedClassType</code> is a class type with a name: either a
 * top-level class, a member class, or a local class, but not an anonymous
 * class.
 */
public interface LocalClassType extends InnerClassType, NamedType
{
}
