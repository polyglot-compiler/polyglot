package polyglot.types;

/**
 * A <code>NamedQualifier</code> is a qualifier with a name, usually either a
 * top-level class, a member class, or a local class.
 */
public interface NamedQualifier extends Qualifier
{
    /**
     * Name of the qualifier.
     */
    String name();

    /**
     * Full dotted-name of the qualifier.
     */
    String fullName();
}
