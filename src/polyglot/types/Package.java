package jltools.types;

/**
 * An <code>Package</code> represents a Java package.
 */
public interface Package extends NamedQualifier
{
    Package prefix();
    String translate(Context c);
}
