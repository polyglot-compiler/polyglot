package jltools.types;

import java.util.List;

/**
 * A <code>ClassType</code> represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A <code>ClassType</code> is not necessarily named.
 */
public interface ClassType extends ReferenceType 
{
    List constructors();
    List memberClasses();

    /** Returns the member class with the given name, or null. */
    MemberClassType memberClassNamed(String name);
    FieldInstance fieldNamed(String name);

    boolean isTopLevel();
    boolean isInner();
    boolean isMember();
    boolean isLocal();
    boolean isAnonymous();

    TopLevelClassType toTopLevel();
    InnerClassType toInner();
    MemberClassType toMember();
    LocalClassType toLocal();
    AnonClassType toAnonymous();

    Package package_();

    /**
     * This method is here for convenience.  Anonymous classes have no access
     * flags.  It will return Flags.NONE if invoked on an anonymous class.
     */
    Flags flags();
}
