package jltools.types;

/**
 * A <code>NamedQualifier</code> is a qualifier with a name, usually either a
 * top-level class, a member class, or a local class.
 */
public interface NamedQualifier extends Qualifier
{
    String name();
    String fullName();
}
