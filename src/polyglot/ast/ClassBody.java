package polyglot.ast;

import java.util.List;

/**
 * A <code>ClassBody</code> represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public interface ClassBody extends Node
{
    /**
     * List of the class's members.
     * A list of <code>ClassMember</code>.
     * @see polyglot.ast.ClassMember
     */
    List members();

    /**
     * Set the class's members.
     * A list of <code>ClassMember</code>.
     * @see polyglot.ast.ClassMember
     */
    ClassBody members(List members);

    /**
     * Add a member to the class, returning a new node.
     */
    ClassBody addMember(ClassMember member);
}
