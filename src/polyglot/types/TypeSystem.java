package jltools.types;

import java.util.*;
import jltools.util.Position;
import jltools.frontend.Job;
import jltools.frontend.Compiler;

/**
 * The <code>TypeSystem</code> defines the types of the language and
 * how they are related.
 */
public interface TypeSystem {
    /**
     * Initialize the type system with the compiler.  This method must be
     * called before any other type system method is called.
     */
    void initialize(Compiler compiler) throws SemanticException;

    /**
     * Return the compiler associated with this type system.
     */
    Compiler compiler();

    /**
     * Returns the system resolver.  This resolver can load top-level classes
     * with fully qualified names from the class path and the source path.
     */
    Resolver systemResolver();

    /**
     * Return a list of the packages names that will be imported by
     * default.  A list of Strings is returned, not a list of Packages.
     */
    List defaultPackageImports();

    /** Create an initailizer instance. */
    InitializerInstance initializerInstance(Position pos, ClassType container,
					    Flags flags);

    /** Create a constructor instance. */
    ConstructorInstance constructorInstance(Position pos, ClassType container,
					    Flags flags, List argTypes,
					    List excTypes);

    /** Create a method instance. */
    MethodInstance methodInstance(Position pos, ReferenceType container,
				  Flags flags, Type returnType, String name,
				  List argTypes, List excTypes);

    /** Create a field instance. */
    FieldInstance fieldInstance(Position pos, ReferenceType container,
				Flags flags, Type type, String name);

    /** Create a local variable instance. */
    LocalInstance localInstance(Position pos, Flags flags, Type type,
	                        String name);

    /**
     * Return a class type for the Class object.  Only a
     * Resolver should call this method.
     */
    ClassType forClass(Class clazz) throws SemanticException;

    /** Get a place-holder for serializing a type object. */ 
    TypeObject placeHolder(TypeObject o, java.util.Set roots);
    TypeObject placeHolder(TypeObject o);

    /** Get an unknown type. */
    UnknownType unknownType(Position pos);

    /** Get an unknown type qualifier. */
    UnknownQualifier unknownQualifier(Position pos);

    /**
     * Returns true iff child descends from ancestor or child == ancestor.
     * This is equivalent to:
     *    descendsFrom(child, ancestor) || isSame(child, ancestor)
     */
    boolean isSubtype(Type child, Type ancestor);

    /**
     * Returns true iff child is not ancestor, but child descends from ancestor.
     */
    boolean descendsFrom(Type child, Type ancestor);

    /**
     * Returns true iff child and ancestor are non-primitive types, and a
     * variable of type child may be legally assigned to a variable of type
     * ancestor.
     */
    boolean isAssignableSubtype(Type child, Type ancestor);

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff a cast from fromType to toType is valid; in other
     * words, some non-null members of fromType are also members of toType.
     */
    boolean isCastValid(Type fromType, Type toType);

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff an implicit cast from fromType to toType is valid;
     * in other words, every member of fromType is member of toType.
     */
    boolean isImplicitCastValid(Type fromType, Type toType);

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff type1 and type2 are the same type.
     **/
    boolean isSame(Type type1, Type type2);

    /**
     * Returns true if <code>value</code> can be implicitly cast to
     * Primitive type <code>t</code>.
     */
    boolean numericConversionValid(Type t, long value);

    /**
     * Requires: all type arguments are canonical.
     * Returns the least common ancestor of Type1 and Type2
     */
    Type leastCommonAncestor(Type type1, Type type2) throws SemanticException;

    /**
     * Returns true iff <type> is a canonical (fully qualified) type.
     */
    boolean isCanonical(Type type);

    /**
     * Checks whether a method or field within target with access flags 'flags'
     * can be accessed from Context context. 
     */
    boolean isAccessible(MemberInstance mi, Context context);

    /**
     * Returns whether inner is enclosed within outer
     */
    boolean isEnclosed(ClassType inner, ClassType outer);

    ////
    // Various one-type predicates.
    ////

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff an object of type <type> may be thrown.
     **/
    boolean isThrowable(Type type);

    /**
     * Returns a true iff the type or a supertype is in the list 
     * returned by uncheckedExceptions().
     */
    boolean isUncheckedException(Type type);

    /**
     * Returns a collection of the Throwable types that need not be declared
     * in method and constructor signatures.
     */
    Collection uncheckedExceptions();

    /** Unary promotion for numeric types. */
    PrimitiveType promote(Type t) throws SemanticException;

    /** Binary promotion for numeric types. */
    PrimitiveType promote(Type t1, Type t2) throws SemanticException;

    ////
    // Functions for type membership.
    ////

    /**
     * Returns the field named 'name' defined on 'type'.
     */
    FieldInstance findField(ReferenceType container, String name, Context c)
	throws SemanticException;

    /**
     * Find a method.  We need to pass the context because the method
     * we find depends on whether the method is accessible from the context.
     */
    MethodInstance findMethod(ReferenceType container,
	String name, List argTypes, Context c) throws SemanticException;

    /**
     * Find a constructor.  We need to pass the context because the method
     * we find depends on whether the method is accessible from the context.
     */
    ConstructorInstance findConstructor(ClassType container,
	List argTypes, Context c) throws SemanticException;

    /**
     * Find a member class.  We need to pass the context because the method
     * we find depends on whether the method is accessible from the context.
     */
    MemberClassType findMemberClass(ClassType container, String name, Context c)
	throws SemanticException;

    /**
     * Returns the immediate supertype of type, or null if type has no
     * supertype.
     **/
    Type superType(ReferenceType type);

    /**
     * Returns an immutable list of all the interface types which type
     * implements.
     **/
    List interfaces(ReferenceType type);

    ////
    // Functions for method testing.
    ////

    /**
     * Returns true iff <type1> has the same arguments as <type2>
     */
    boolean hasSameArguments(ProcedureInstance m1, ProcedureInstance m2);

    ////
    // Functions which yield particular types.
    ////
    NullType Null();
    PrimitiveType Void();
    PrimitiveType Boolean();
    PrimitiveType Char();
    PrimitiveType Byte();
    PrimitiveType Short();
    PrimitiveType Int();
    PrimitiveType Long();
    PrimitiveType Float();
    PrimitiveType Double();
    ClassType Object();
    ClassType String();
    ClassType Class();
    ClassType Throwable();
    ClassType Error();
    ClassType Exception();
    ClassType RuntimeException();
    ClassType Cloneable();
    ClassType Serializable();
    ClassType NullPointerException();
    ClassType ClassCastException();
    ClassType OutOfBoundsException();
    ClassType ArrayStoreException();
    ClassType ArithmeticException();

    /**
     * Returns a type identical to <type>, but with <dims> more array
     * dimensions.  <dims> must be >= 0.
     **/
    ArrayType arrayOf(Type type);
    ArrayType arrayOf(Position pos, Type type);
    ArrayType arrayOf(Type type, int dims);
    ArrayType arrayOf(Position pos, Type type, int dims);

    /**
     * Returns a canonical type corresponding to the Java Class object
     * class.  Does not require that <theClass> have a JavaClass
     * theClass.  Does not require that <theClass> have a ClassType
     * registered in this typeSystem.  Does not register the type in
     * this TypeSystem.  
     * this TypeSystem.  For use only by ClassType implementations.
     **/
    Type typeForClass(Class clazz) throws SemanticException;

    Package packageForName(String name);
    Package packageForName(Package prefix, String name);

    Context createContext(ImportTable it);

    /** Get a resolver for looking up a type in a package. */
    Resolver packageContextResolver(Resolver resolver, Package pkg);

    /** Get a resolver for looking up a type in a class context. */
    Resolver classContextResolver(ClassType ct);

    ParsedAnonClassType anonClassType(Job job);
    ParsedTopLevelClassType topLevelClassType(Job job);
    ParsedMemberClassType memberClassType(Job job);
    ParsedLocalClassType localClassType(Job job);

    /**
     * return the set of objects that should be serialized into the
     * type information for the given ClassType. 
     * Usually only the clazz itself should get encoded, and references
     * to other classes should just have their name written out.
     * If it makes sense for additional types to be fully encoded,
     * (ie, they're necessary to correctly reconstruct the given clazz,
     * and the usual class resolvers can't otherwise find them) they
     * should be returned in the set in addition to clazz.
     */
    Set getTypeEncoderRootSet(Type clazz);

    String translatePackage(Context c, Package p);
    String translatePrimitive(Context c, PrimitiveType t);
    String translateArray(Context c, ArrayType t);
    String translateTopLevelClass(Context c, TopLevelClassType t);
    String translateMemberClass(Context c, MemberClassType t);
    String translateLocalClass(Context c, LocalClassType t);
    String wrapperTypeString(PrimitiveType t);

    PrimitiveType primitiveForName(String name) throws SemanticException;

    void checkMethodFlags(Flags f) throws SemanticException;
    void checkLocalFlags(Flags f) throws SemanticException;
    void checkFieldFlags(Flags f) throws SemanticException;
    void checkConstructorFlags(Flags f) throws SemanticException;
    void checkInitializerFlags(Flags f) throws SemanticException;
    void checkTopLevelClassFlags(Flags f) throws SemanticException;
    void checkMemberClassFlags(Flags f) throws SemanticException;
    void checkLocalClassFlags(Flags f) throws SemanticException;
    void checkAccessFlags(Flags f) throws SemanticException;

    void checkCycles(ReferenceType t) throws SemanticException;
}
