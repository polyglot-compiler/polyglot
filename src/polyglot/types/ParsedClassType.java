package polyglot.types;

import java.util.List;
import polyglot.util.Position;

/**
 * A <code>ParsedClassType</code> represents a class loaded from a source file.
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
