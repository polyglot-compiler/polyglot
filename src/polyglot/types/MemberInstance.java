package jltools.types;

/**
 * A <code>MemberInstance</code> is an entity that can be a member of
 * a class.
 */
public interface MemberInstance extends TypeObject
{
    Flags flags();
    ReferenceType container();
}
