package polyglot.types;

/**
 * A <code>ImportableType</code> is a type that can be imported by another
 * type.  An <code>ImportableType</code> is contained in a
 * <code>Package</code>.  In Java, the only <code>ImportableType</code> is a
 * top-level class.
 */
public interface ImportableType extends NamedType
{
    Package package_();
}
