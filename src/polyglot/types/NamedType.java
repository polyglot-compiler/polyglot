package polyglot.types;

/**
 * A <code>NamedType</code> is a type with a name, usually either a top-level
 * class, a member class, or a local class.
 */
public interface NamedType extends NamedQualifier, Type
{
    String name();
    String fullName();
}
