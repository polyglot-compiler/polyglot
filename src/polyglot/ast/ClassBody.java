package jltools.ast;

import java.util.List;

/**
 * A <code>ClassBody</code> represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public interface ClassBody extends Node
{
    List members();
    ClassBody members(List members);

    ClassBody addMember(ClassMember member);
}
