package jltools.types;

/**
 * A <code>Qualifier</code> can be used to qualify a type: it can be either
 * a package or a named class type.
 */
public interface Qualifier extends TypeObject
{
    boolean isPackage();
    Package toPackage();

    boolean isType();
    Type toType();
}
