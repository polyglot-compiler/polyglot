package polyglot.types;

/**
 * An <code>Package</code> represents a Java package.
 */
public interface Package extends NamedQualifier
{
    /**
     * The package's outer package.
     */
    Package prefix();

    /**
     * Return a string that is the translation of this package.
     * @param c A resolver in which to look up the package.
     */
    String translate(Resolver c);

    /**
     * Return true if <code>this</code> is the same package as <code>p</code>.
     */
    boolean isSame(Package p);
}
