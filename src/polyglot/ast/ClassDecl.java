package jltools.ast;

import java.util.List;
import jltools.types.Flags;
import jltools.types.ParsedClassType;

/**
 * A <code>ClassDecl</code> represents a top-level, member, or local class
 * declaration.
 */
public interface ClassDecl extends Node, TopLevelDecl, ClassMember
{
    ParsedClassType type();

    Flags flags();
    ClassDecl flags(Flags flags);

    String name();
    ClassDecl name(String name);

    TypeNode superClass();
    ClassDecl superClass(TypeNode superClass);

    List interfaces();
    ClassDecl interfaces(List interfaces);

    ClassBody body();
    ClassDecl body(ClassBody body);
}
