package jltools.ast;

import java.util.List;

public interface ClassBody extends Node
{
    List members();
    ClassBody members(List members);

    ClassBody addMember(ClassMember member);
}
