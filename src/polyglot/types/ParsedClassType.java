package jltools.types;

import java.util.List;
import jltools.util.Position;

/**
 * A <code>ClassType</code> represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A <code>ClassType</code> is not necessarily named.
 */
public interface ParsedClassType extends ClassType, ParsedType
{
    void position(Position pos);
    void package_(Package p);
    void superType(Type t);
    void addInterface(Type t);
    void addField(FieldInstance fi);
    void addMethod(MethodInstance mi);
    void addConstructor(ConstructorInstance ci);
    void addMemberClass(MemberClassType t);

    void replaceField(FieldInstance old, FieldInstance fi);
    void replaceMethod(MethodInstance old, MethodInstance mi);
    void replaceConstructor(ConstructorInstance old, ConstructorInstance ci);
    void replaceMemberClass(MemberClassType old, MemberClassType t);

    /** Set the flags of the class.  Anonymous classes don't have flags.  This
     * has no effect for anonymous classes. */
    void flags(Flags flags);
}
