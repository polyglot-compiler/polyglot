package polyglot.types;

/**
 * A <code>MemberInstance</code> is an entity that can be a member of
 * a class.
 */
public interface MemberInstance extends TypeObject
{
    /**
     * Return the member's flags.
     */
    Flags flags();
    
    /**
     * Destructively set the member's flags.
     * @param flags
     */
    void setFlags(Flags flags);

    /**
     * Return the member's containing type.
     */
    ReferenceType container();
    
    /**
     * Destructively set the member's container.
     * @param container
     */
    void setContainer(ReferenceType container);
}
