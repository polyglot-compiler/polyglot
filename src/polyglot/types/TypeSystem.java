package polyglot.types;

import java.util.*;
import polyglot.util.Position;
import polyglot.frontend.Job;
import polyglot.frontend.Compiler;
import polyglot.types.reflect.ClassFile;

/**
 * The <code>TypeSystem</code> defines the types of the language and
 * how they are related.
 */
public interface TypeSystem {
    /**
     * Initialize the type system with the compiler.  This method must be
     * called before any other type system method is called.
     */
    void initialize(LoadedClassResolver resolver)
                    throws SemanticException;

    /**
     * Returns the system resolver.  This resolver can load top-level classes
     * with fully qualified names from the class path and the source path.
     */
    Resolver systemResolver();

    /**
     * Return the type system's table resolver.
     * This resolver contains types parsed from source files.
     */
    TableResolver parsedResolver();

    /**
     * Return the type system's loaded resolver.
     * This resolver contains types loaded from class files.
     */
    LoadedClassResolver loadedResolver();

    /** Create an import table for the source file. */
    ImportTable importTable(String sourceName, Package pkg);

    /** Create an import table for the source file. */
    ImportTable importTable(Package pkg);

    /**
     * Return a list of the packages names that will be imported by
     * default.  A list of Strings is returned, not a list of Packages.
     */
    List defaultPackageImports();

    /** Get the class type with the following name. */
    ClassType typeForName(String name) throws SemanticException;

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

    /** Create a default constructor instance. */
    ConstructorInstance defaultConstructor(Position pos, ClassType container);

    /** Get a place-holder for serializing a type object. */
    TypeObject placeHolder(TypeObject o, java.util.Set roots);

    /** Get a place-holder for serializing a type object. */
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
     * Returns true iff child is not ancestor, but child descends from ancestor.     */
    boolean descendsFrom(Type child, Type ancestor);

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
     */
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
     * Checks whether a method or field within target with access flags 'flags'      * can be accessed from Context context.
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
     * Returns true iff an object of type <type> may be thrown.
     */
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
     * We check if the field is accessible from the context 'c'.
     */
    FieldInstance findField(ReferenceType container, String name, Context c)
	throws SemanticException;

    /**
     * Returns the field named 'name' defined on 'type'.
     */
    FieldInstance findField(ReferenceType container, String name)
	throws SemanticException;

    /**
     * Find a method.  We need to pass the context because the method
     * we find depends on whether the method is accessible from the context.
     * We also check if the field is accessible from the context 'c'.
     */
    MethodInstance findMethod(ReferenceType container,
                              String name, List argTypes,
                              Context c) throws SemanticException;

    /**
     * Find a constructor.  We need to pass the context because the constructor
     * we find depends on whether the method is accessible from the context.
     * We also check if the field is accessible from the context 'c'.
     */
    ConstructorInstance findConstructor(ClassType container, List argTypes,
                                        Context c) throws SemanticException;

    /**
     * Find a member class.
     * We check if the field is accessible from the context 'c'.
     */
    MemberClassType findMemberClass(ClassType container, String name, Context c)
	throws SemanticException;

    /**
     * Find a member class.
     */
    MemberClassType findMemberClass(ClassType container, String name)
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
     * Returns true iff <m1> throws fewer exceptions than <m2>.
     */
    boolean throwsSubset(ProcedureInstance m1, ProcedureInstance m2);

    /**
     * Returns true iff <m1> has the same arguments as <m2>.
     */
    boolean hasMethod(ReferenceType t, MethodInstance mi);

    /**
     * Returns true iff <m1> is the same method as <m2>.
     */
    boolean isSameMethod(MethodInstance m1, MethodInstance m2);

    /**
     * Returns true iff <m1> is more specific than <m2>.
     */
    boolean moreSpecific(ProcedureInstance m1, ProcedureInstance m2);

    /**
     * Returns true iff <m1> is more specific than <m2>.
     */
    boolean hasArguments(ProcedureInstance p, List argumentTypes);

    ////
    // Functions which yield particular types.
    ////

    /**
     * The type of <code>null</code>.
     */
    NullType Null();

    /**
     * <code>void</code>
     */
    PrimitiveType Void();

    /**
     * <code>boolean</code>
     */
    PrimitiveType Boolean();

    /**
     * <code>char</code>
     */
    PrimitiveType Char();

    /**
     * <code>byte</code>
     */
    PrimitiveType Byte();

    /**
     * <code>short</code>
     */
    PrimitiveType Short();

    /**
     * <code>int</code>
     */
    PrimitiveType Int();

    /**
     * <code>long</code>
     */
    PrimitiveType Long();

    /**
     * <code>float</code>
     */
    PrimitiveType Float();

    /**
     * <code>double</code>
     */
    PrimitiveType Double();

    /**
     * <code>java.lang.Object</code>
     */
    ClassType Object();

    /**
     * <code>java.lang.String</code>
     */
    ClassType String();

    /**
     * <code>java.lang.Class</code>
     */
    ClassType Class();

    /**
     * <code>java.lang.Throwable</code>
     */
    ClassType Throwable();

    /**
     * <code>java.lang.Error</code>
     */
    ClassType Error();

    /**
     * <code>java.lang.Exception</code>
     */
    ClassType Exception();

    /**
     * <code>java.lang.RuntimeException</code>
     */
    ClassType RuntimeException();

    /**
     * <code>java.lang.Cloneable</code>
     */
    ClassType Cloneable();

    /**
     * <code>java.io.Serializable</code>
     */
    ClassType Serializable();

    /**
     * <code>java.lang.NullPointerException</code>
     */
    ClassType NullPointerException();

    /**
     * <code>java.lang.ClassCastException</code>
     */
    ClassType ClassCastException();

    /**
     * <code>java.lang.ArrayIndexOutOfBoundsException</code>
     */
    ClassType OutOfBoundsException();

    /**
     * <code>java.lang.ArrayStoreException</code>
     */
    ClassType ArrayStoreException();

    /**
     * <code>java.lang.ArithmeticException</code>
     */
    ClassType ArithmeticException();

    /**
     * Return an array of <code>type</code>
     */
    ArrayType arrayOf(Type type);

    /**
     * Return an array of <code>type</code>
     */
    ArrayType arrayOf(Position pos, Type type);

    /**
     * Return a <code>dims</code>-array of <code>type</code>
     */
    ArrayType arrayOf(Type type, int dims);

    /**
     * Return a <code>dims</code>-array of <code>type</code>
     */
    ArrayType arrayOf(Position pos, Type type, int dims);

    /**
     * Return a package by name.
     */
    Package packageForName(String name);

    /**
     * Return a package by name with the given outer package.
     */
    Package packageForName(Package prefix, String name);

    /**
     * Create a new context object for looking up variables, types, etc.
     */
    Context createContext();

    /** Get a resolver for looking up a type in a package. */
    Resolver packageContextResolver(Resolver resolver, Package pkg);

    /** Get a resolver for looking up a type in a class context. */
    Resolver classContextResolver(ClassType ct);

    /**
     * The default lazy class initializer.
     */
    LazyClassInitializer defaultClassInitializer();

    /**
     * Create a top-level class.
     */
    ParsedTopLevelClassType topLevelClassType(LazyClassInitializer init);

    /**
     * Create a member class.
     */
    ParsedMemberClassType memberClassType(LazyClassInitializer init);

    /**
     * Create a local class.
     */
    ParsedLocalClassType localClassType(LazyClassInitializer init);

    /**
     * Create a anonymous class.
     */
    ParsedAnonClassType anonClassType(LazyClassInitializer init);

    /**
     * Create a top-level class.
     */
    ParsedTopLevelClassType topLevelClassType();

    /**
     * Create a member class.
     */
    ParsedMemberClassType memberClassType();

    /**
     * Create a local class.
     */
    ParsedLocalClassType localClassType();

    /**
     * Create a anonymous class.
     */
    ParsedAnonClassType anonClassType();

    /**
     * Return the set of objects that should be serialized into the
     * type information for the given ClassType.
     * Usually only the clazz itself should get encoded, and references
     * to other classes should just have their name written out.
     * If it makes sense for additional types to be fully encoded,
     * (ie, they're necessary to correctly reconstruct the given clazz,
     * and the usual class resolvers can't otherwise find them) they
     * should be returned in the set in addition to clazz.
     */
    Set getTypeEncoderRootSet(Type clazz);

    /**
     * Translate a package.
     */
    String translatePackage(Resolver c, Package p);

    /**
     * Translate a primitive type.
     */
    String translatePrimitive(Resolver c, PrimitiveType t);

    /**
     * Translate an array type.
     */
    String translateArray(Resolver c, ArrayType t);

    /**
     * Translate a top-level class type.
     */
    String translateTopLevelClass(Resolver c, TopLevelClassType t);

    /**
     * Translate a member class type.
     */
    String translateMemberClass(Resolver c, MemberClassType t);

    /**
     * Translate a local class type.
     */
    String translateLocalClass(Resolver c, LocalClassType t);

    /**
     * Return the boxed version of <code>t</code>.
     */
    String wrapperTypeString(PrimitiveType t);

    /**
     * Return true if <code>mi</code> can be called with name <code>name</code>
     * and actual parameters of types <code>actualTypes</code>.
     */
    boolean methodCallValid(MethodInstance mi, String name, List argTypes);

    /**
     * Return true if <code>pi</code> can be called with 
     * actual parameters of types <code>actualTypes</code>.
     */
    boolean callValid(ProcedureInstance mi, List argTypes);

    /**
     * Get the list of methods <code>mi</code> (potentially) overrides, in
     * order from this class (i.e., including <code>this</code>) to super
     * classes.
     */
    List overrides(MethodInstance mi);

    /**
     * Return true if <code>mi</code> can override <code>mj</code>.
     */
    boolean canOverride(MethodInstance mi, MethodInstance mj);

    /**
     * Return the primitive with the given name.
     */
    PrimitiveType primitiveForName(String name) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal method flags.
     */
    void checkMethodFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal local variable flags.
     */
    void checkLocalFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal field flags.
     */
    void checkFieldFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal constructor flags.
     */
    void checkConstructorFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal initializer flags.
     */
    void checkInitializerFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal top-level class flags.
     */
    void checkTopLevelClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal member class flags.
     */
    void checkMemberClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal local class flags.
     */
    void checkLocalClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags <code>f</code> are legal access flags.
     */
    void checkAccessFlags(Flags f) throws SemanticException;

    /**
     * Assert that <code>t</code> has no cycles in the super type+inner class
     * graph starting at <code>t</code>.
     */
    void checkCycles(ReferenceType t) throws SemanticException;

    /**
     * Returns <code>t</code>, modified as necessary to make it a legal
     * static target.
     */
    public Type staticTarget(Type t);
}
