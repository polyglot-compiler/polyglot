package polyglot.types;

import java.util.List;
import polyglot.util.Position;

/**
 * A <code>ParsedClassType</code> represents a class loaded from a source file.
 * <code>ParsedClassType</code>s are mutable.
 */
public interface ParsedClassType extends ClassType, ParsedType
{
    /**
     * Position of the type's declaration.
     */
    void position(Position pos);

    /**
     * Set the class's package.
     */
    void package_(Package p);

    /**
     * Set the class's super type.
     */
    void superType(Type t);

    /**
     * Add an interface to the class.
     */
    void addInterface(Type t);

    /**
     * Add a field to the class.
     */
    void addField(FieldInstance fi);

    /**
     * Add a method to the class.
     */
    void addMethod(MethodInstance mi);

    /**
     * Add a constructor to the class.
     */
    void addConstructor(ConstructorInstance ci);

    /**
     * Add a member class to the class.
     */
    void addMemberClass(MemberClassType t);

    /**
     * Replace a field in the class.
     */
    void replaceField(FieldInstance old, FieldInstance fi);

    /**
     * Replace a method in the class.
     */
    void replaceMethod(MethodInstance old, MethodInstance mi);

    /**
     * Replace a constructor in the class.
     */
    void replaceConstructor(ConstructorInstance old, ConstructorInstance ci);

    /**
     * Replace a member class in the class.
     */
    void replaceMemberClass(MemberClassType old, MemberClassType t);

    /**
     * Set the flags of the class.  Anonymous classes don't have flags.  This
     * has no effect for anonymous classes.
     */
    void flags(Flags flags);
}
