package polyglot.types;

/**
 * A <code>Named</code> is a TypeObject that is named.
 */
public interface Named extends TypeObject
{
    /**
     * Name of the type object.
     */
    String name();

    /**
     * Full dotted-name of the type object.
     */
    String fullName();
}
