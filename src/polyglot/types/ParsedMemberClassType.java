package jltools.types;

/**
 * A <code>MemberClassType</code> is a class type that is a member of another
 * class.
 */
public interface ParsedMemberClassType extends ParsedInnerClassType,
                                               MemberClassType
{
    void name(String name);
}
