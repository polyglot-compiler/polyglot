package polyglot.types;

/**
 * A <code>ParsedMemberClassType</code> is a parsed class type that is a member
 * of another class.
 */
public interface ParsedMemberClassType extends ParsedInnerClassType,
                                               MemberClassType
{
    /**
     * Set the name of the class.
     */
    void name(String name);
}
