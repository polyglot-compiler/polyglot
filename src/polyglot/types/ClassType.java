package polyglot.types;

import java.util.List;

/**
 * A <code>ClassType</code> represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A <code>ClassType</code> is not necessarily named.
 */
public interface ClassType extends ReferenceType 
{
    /**
     * The class's constructors.
     */
    List constructors();

    /**
     * The class's member classes.
     */
    List memberClasses();

    /** Returns the member class with the given name, or null. */
    MemberClassType memberClassNamed(String name);

    /**
     * Get a field by name, or null.
     */
    FieldInstance fieldNamed(String name);

    /**
     * Return true if the class is top-level (i.e., not inner).
     * This method will probably get deprecated.
     */
    boolean isTopLevel();

    /**
     * Return true if the class is an inner class.
     * This method will probably get deprecated.
     */
    boolean isInner();

    /**
     * Return true if the class is a member class.
     * This method will probably get deprecated.
     */
    boolean isMember();

    /**
     * Return true if the class is a local class.
     * This method will probably get deprecated.
     */
    boolean isLocal();

    /**
     * Return true if the class is an anonymous class.
     * This method will probably get deprecated.
     */
    boolean isAnonymous();

    /**
     * Cast to a top-level class, returning null if not top-level.
     * This method will probably get deprecated.
     */
    TopLevelClassType toTopLevel();

    /**
     * Cast to an inner class, returning null if not inner.
     * This method will probably get deprecated.
     */
    InnerClassType toInner();

    /**
     * Cast to a member class, returning null if not a member.
     * This method will probably get deprecated.
     */
    MemberClassType toMember();

    /**
     * Cast to a local class, returning null if not local.
     * This method will probably get deprecated.
     */
    LocalClassType toLocal();

    /**
     * Cast to an anonymous class, returning null if not anonymous.
     * This method will probably get deprecated.
     */
    AnonClassType toAnonymous();

    /**
     * The class's package.
     */
    Package package_();

    /**
     * This method is here for convenience.  Anonymous classes have no access
     * flags.  It will return Flags.NONE if invoked on an anonymous class.
     */
    Flags flags();

    /**
     * Return true if the class is strictly contained in <code>outer</code>.
     */
    boolean isEnclosed(ClassType outer);

    /**
     * Implementation of <code>isEnclosed</code>.
     * This method should only be called by the <code>TypeSystem</code>
     * or by a subclass.
     */
    boolean isEnclosedImpl(ClassType outer);
}
