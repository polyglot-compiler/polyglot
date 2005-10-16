package polyglot.types;

import polyglot.ext.jx.types.JxPackage;

/**
 * An <code>Package</code> represents a Java package.
 */
public interface Package extends Qualifier, Named, Declaration
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
    
    /** Return true if this package is equivalent to <code>p</code>. */
    boolean packageEquals(Package p);

    /** Return true if this package is equivalent to <code>p</code>. */
    boolean packageEqualsImpl(Package p);
}
