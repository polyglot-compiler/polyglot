/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Source;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.Position;

/**
 * The {@code TypeSystem} defines the types of the language and
 * how they are related.
 */
public interface TypeSystem {
    public static final boolean SERIALIZE_MEMBERS_WITH_CONTAINER = false;

    /**
     * Initialize the type system with the compiler and its internal constants
     * (which depend on the resolver).  This method must be
     * called before any other type system method is called.
     *
     * @param resolver The resolver to use for loading types from class files
     *                 or other source files.
     * @param extInfo The ExtensionInfo the TypeSystem is being created for.
     */
    void initialize(TopLevelResolver resolver, ExtensionInfo extInfo)
            throws SemanticException;

    /** Return the language extension this type system is for. */
    ExtensionInfo extensionInfo();

    /**
     * Returns the system resolver.  This resolver can load top-level classes
     * with fully qualified names from the class path and the source path.
     */
    SystemResolver systemResolver();

    /**
     * Return the system resolver.
     * This resolver contains types parsed from source files.
     * This used to return a different resolver enclosed in the system resolver.
     * @deprecated
     */
    @Deprecated
    CachingResolver parsedResolver();

    /** Create and install a duplicate of the system resolver and return the original. */
    SystemResolver saveSystemResolver();

    /** Set the system resolver to {@code r}. */
    void restoreSystemResolver(SystemResolver r);

    /**
     * Return the type system's loaded resolver.
     * This resolver contains types loaded from class files.
     */
    TopLevelResolver loadedResolver();

    /**
     * Constructs a new ClassFileLazyClassInitializer for the given class file.
     */
    ClassFileLazyClassInitializer classFileLazyClassInitializer(
            ClassFile clazz);

    /**
     * Create an import table for the source file.
     * @param sourceName Name of the source file to import into.  This is used
     * mainly for error messages and for debugging.
     * @param pkg The package of the source file in which to import.
     */
    ImportTable importTable(String sourceName, Package pkg);

    /**
     * Create an import table for the source file.
     * @param pkg The package of the source file in which to import.
     */
    ImportTable importTable(Package pkg);

    /**
     * Return a list of the packages names that will be imported by
     * default.  A list of Strings is returned, not a list of Packages.
     */
    List<String> defaultPackageImports();

    /**
     * Returns true if the package named {@code name} exists.
     */
    boolean packageExists(String name);

    /**
     * Get the named type object with the following name.
     * @param name The name of the type object to look for.
     * @exception SemanticException when object is not found.
     */
    Named forName(String name) throws SemanticException;

    /**
     * Get the  type with the following name.
     * @param name The name to create the type for.
     * @exception SemanticException when type is not found.
     */
    Type typeForName(String name) throws SemanticException;

    /**
     * Create an initializer instance.
     * @param pos Position of the initializer.
     * @param container Containing class of the initializer.
     * @param flags The initializer's flags.
     */
    InitializerInstance initializerInstance(Position pos, ClassType container,
            Flags flags);

    /**
     * Create a constructor instance.
     * @param pos Position of the constructor.
     * @param container Containing class of the constructor.
     * @param flags The constructor's flags.
     * @param argTypes The constructor's formal parameter types.
     * @param excTypes The constructor's exception throw types.
     */
    ConstructorInstance constructorInstance(Position pos, ClassType container,
            Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes);

    /**
     * Create a method instance.
     * @param pos Position of the method.
     * @param container Containing type of the method.
     * @param flags The method's flags.
     * @param returnType The method's return type.
     * @param name The method's name.
     * @param argTypes The method's formal parameter types.
     * @param excTypes The method's exception throw types.
     */
    MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes);

    /**
     * Create a field instance.
     * @param pos Position of the field.
     * @param container Containing type of the field.
     * @param flags The field's flags.
     * @param type The field's type.
     * @param name The field's name.
     */
    FieldInstance fieldInstance(Position pos, ReferenceType container,
            Flags flags, Type type, String name);

    /**
     * Create a local variable instance.
     * @param pos Position of the local variable.
     * @param flags The local variable's flags.
     * @param type The local variable's type.
     * @param name The local variable's name.
     */
    LocalInstance localInstance(Position pos, Flags flags, Type type,
            String name);

    /**
     * Create a default constructor instance.
     * @param pos Position of the constructor.
     * @param container Containing class of the constructor.
     */
    ConstructorInstance defaultConstructor(Position pos, ClassType container);

    /** Get an unknown type. */
    UnknownType unknownType(Position pos);

    /** Get an unknown package. */
    UnknownPackage unknownPackage(Position pos);

    /** Get an unknown type qualifier. */
    UnknownQualifier unknownQualifier(Position pos);

    /**
     * Returns true iff child descends from ancestor or child == ancestor.
     * This is equivalent to:
     * <pre>
     *    descendsFrom(child, ancestor) || equals(child, ancestor)
     * </pre>
     */
    boolean isSubtype(Type child, Type ancestor);

    /**
     * Returns true iff child and ancestor are distinct,
     * but child descends from ancestor.
     */
    boolean descendsFrom(Type child, Type ancestor);

    /**
     * Requires: all type arguments are canonical, and toType is not a NullType.
     *
     * Returns true iff a cast from fromType to toType is valid; in other
     * words, some non-null members of fromType are also members of toType.
     */
    boolean isCastValid(Type fromType, Type toType);

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff fromType and toType are non-primitive
     * types, and a variable of type fromType may be legally assigned
     * to a variable of type toType.
     */
    boolean isImplicitCastValid(Type fromType, Type toType);

    /**
     * Returns true iff type1 and type2 represent the same type object.
     */
    boolean equals(TypeObject type1, TypeObject type2);

    /**
     * Returns true iff type1 and type2 are equivalent.
     * This is usually the same as equals(type1, type2), but may
     * differ in the presence of, say, type aliases.
     */
    boolean typeEquals(Type type1, Type type2);

    /**
     * Returns true iff type1 and type2 are equivalent.
     * This is usually the same as equals(type1, type2), but may
     * differ in the presence of, say, type aliases.
     */
    boolean packageEquals(Package type1, Package type2);

    /**
     * Returns true if {@code value} can be implicitly cast to primitive type
     * {@code t}.
     * @deprecated Use {@link #numericConversionValid(Type, Object)} instead.
     */
    @Deprecated
    boolean numericConversionValid(Type t, long value);

    /**
     * Returns true if {@code value} can be implicitly cast to
     * primitive type {@code t}.
     */
    boolean numericConversionValid(Type t, Object value);

    /**
     * Requires: all type arguments are canonical.
     * Returns the least common ancestor of {@code type1} and {@code type2}.
     * @exception SemanticException if the LCA does not exist
     */
    Type leastCommonAncestor(Type type1, Type type2) throws SemanticException;

    /**
     * Returns true iff {@code type} is a canonical
     * (fully qualified) type.
     */
    boolean isCanonical(Type type);

    /**
     * Checks whether a class member can be accessed from {@code context}.
     */
    boolean isAccessible(MemberInstance mi, Context context);

    /**
     * Checks whether a class member can be accessed from code that is
     * declared in the class {@code contextClass}.
     */
    boolean isAccessible(MemberInstance mi, ClassType contextClass);

    /**
     * Checks whether a class member can be accessed from code that is
     * declared in the class {@code contextClass}.
     */
    boolean isAccessible(MemberInstance mi, ClassType contextClass,
            boolean fromClient);

    /**
     * Checks whether a class member mi, which is declared in container or
     * an ancestor of container, can be accessed from code that is declared
     * in class {@code contextClass}, accessing it via the type container.
     */
    boolean isAccessible(MemberInstance mi, ReferenceType container,
            ClassType contextClass);

    /**
     * Checks whether a member mi, which is declared in container or
     * an ancestor of container, can be accessed from code that is declared
     * in type {@code context}.  fromClient indicates whether this member is
     * being access from a client (true) or by inheritance (false).
     */
    boolean isAccessible(MemberInstance mi, ReferenceType container,
            ReferenceType contextType, boolean fromClient);

    /**
     * Checks whether {@code targetClass} can be accessed from {@code context}.
     */
    boolean classAccessible(ClassType targetClass, Context context);

    /**
     * True if the class {@code targetClass} accessible from the body of class
     * {@code contextClass}.
     */
    boolean classAccessible(ClassType targetClass, ClassType contextClass);

    /**
     * Checks whether a top-level or member class can be accessed from the
     * package {@code pkg}.  Returns false for local and anonymous classes.
     */
    boolean classAccessibleFromPackage(ClassType ct, Package pkg);

    /**
     * Returns whether inner is enclosed within outer
     */
    boolean isEnclosed(ClassType inner, ClassType outer);

    /**
     * Returns whether an object of the inner class {@code inner} has an
     * enclosing instance of class {@code encl}.
     */
    boolean hasEnclosingInstance(ClassType inner, ClassType encl);

    /**
     * Returns whether member {@code mi} is a member of reference type
     * {@code type}, either by definition or by inheritance.
     */
    boolean isMember(MemberInstance mi, ReferenceType type);

    /**
     * Returns whether member {@code mi} is inherited by reference type {@code type}.
     * See JLS 2nd edition section 6.4.
     */
    boolean isInherited(MemberInstance mi, ReferenceType type);

    ////
    // Various one-type predicates.
    ////

    /**
     * Returns true iff the type t can be coerced to a String in the given
     * Context. If a type can be coerced to a String then it can be
     * concatenated with Strings, e.g. if o is of type T, then the code snippet
     *         {@code "" + o}
     * would be allowed.
     */
    boolean canCoerceToString(Type t, Context c);

    /**
     * Returns true iff an object of type {@code type} may be thrown.
     */
    boolean isThrowable(Type type);

    /**
     * Returns a true iff the type or a supertype is in the list
     * returned by {@link #uncheckedExceptions()}.
     */
    boolean isUncheckedException(Type type);

    /**
     * Returns a collection of the {@code Throwable} types that need not be declared
     * in method and constructor signatures.
     */
    Collection<Type> uncheckedExceptions();

    /**
     * Unary promotion for numeric types.
     * @exception SemanticException if the type cannot be promoted.
     */
    PrimitiveType promote(Type t) throws SemanticException;

    /**
     * Binary promotion for numeric types.
     * @exception SemanticException if the types cannot be promoted.
     */
    PrimitiveType promote(Type t1, Type t2) throws SemanticException;

    ////
    // Functions for type membership.
    ////

    /**
     * Deprecated version of the findField method.
     * @deprecated
     */
    @Deprecated
    FieldInstance findField(ReferenceType container, String name, Context c)
            throws SemanticException;

    /**
     * Deprecated version of the findField method.
     * @deprecated
     */
    @Deprecated
    FieldInstance findField(ReferenceType container, String name,
            ClassType currClass) throws SemanticException;

    /**
     * Returns the FieldInstance for the field {@code name} defined
     * in type {@code container} or a supertype, and visible from
     * {@code currClass}.  {@code currClass} may be null.
     * @exception SemanticException if the field cannot be found or is
     * inaccessible.
     */
    FieldInstance findField(ReferenceType container, String name,
            ClassType currClass, boolean fromClient) throws SemanticException;

    /**
     * Returns the FieldInstance for the field {@code name} defined
     * in type {@code container} or a supertype.
     * @exception SemanticException if the field cannot be found or is
     * inaccessible.
     */
    @Deprecated
    FieldInstance findField(ReferenceType container, String name)
            throws SemanticException;

    /**
     * Deprecated version of the findMethod method.
     * @deprecated
     */
    @Deprecated
    MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, Context c) throws SemanticException;

    /**
     * Deprecated version of the findMethod method.
     * @deprecated
     */
    @Deprecated
    MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass)
                    throws SemanticException;

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns the MethodInstance named {@code name} defined in type
     * {@code container} and visible from class {@code curClass}.
     *
     * We need to pass the class from which the method
     * is being found because the method we find depends on whether the method
     * is accessible from that class.
     * @exception SemanticException if the method cannot be found or is
     * inaccessible.
     */
    MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException;

    /**
     * Deprecated version of the findConstructor method.
     * @deprecated
     */
    @Deprecated
    ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, Context c) throws SemanticException;

    /**
     * Deprecated version of the findConstructor method.
     * @deprecated
     */
    @Deprecated
    ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass)
                    throws SemanticException;

    /**
     * Find a constructor.  We need to pass the class from which the constructor
     * is being found because the constructor we find depends on whether the
     * constructor is accessible from that class.
     * @param fromClient indicates whether the constructor is being accessed
     *   from a client or by inheritance
     * @exception SemanticException if the constructor cannot be found or is
     * inaccessible.
     */
    ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException;

    /**
     * Find a member class.
     * We check if the field is accessible from the class currClass.
     * @exception SemanticException if the class cannot be found or is
     * inaccessible.
     */
    ClassType findMemberClass(ClassType container, String name,
            ClassType currClass) throws SemanticException;

    /**
     * Deprecated version of the findMemberClass method.
     * @deprecated
     */
    @Deprecated
    ClassType findMemberClass(ClassType container, String name, Context c)
            throws SemanticException;

    /**
     * Find a member class.
     * @exception SemanticException if the class cannot be found or is
     * inaccessible.
     */
    ClassType findMemberClass(ClassType container, String name)
            throws SemanticException;

    /**
     * Returns the immediate supertype of type, or null if type has no
     * supertype.
     */
    Type superType(ReferenceType type);

    /**
     * Returns an immutable list of all the interface types which type
     * implements.
     */
    List<? extends Type> interfaces(ReferenceType type);

    ////
    // Functions for method testing.
    ////

    /**
     * Returns true iff {@code p1} throws fewer exceptions than {@code p2}.
     */
    boolean throwsSubset(ProcedureInstance p1, ProcedureInstance p2);

    /**
     * Returns true iff {@code t} has the method {@code mi}.
     */
    boolean hasMethod(ReferenceType t, MethodInstance mi);

    /**
     * Returns true iff {@code container} has a method with name {@code name}
     * either defined in {@code container} or inherited into it.
     */
    boolean hasMethodNamed(ReferenceType container, String name);

    /**
     * Returns true iff {@code t} has a method with name {@code name}
     * either defined in {@code t} or inherited into it that is accessible from currClass.
     */
    boolean hasAccessibleMethodNamed(ReferenceType t, String name,
            ClassType currClass);

    /**
     * Returns true iff {@code m1} is the same method as {@code m2}.
     */
    boolean isSameMethod(MethodInstance m1, MethodInstance m2);

    /**
     * Returns true iff {@code c1} is the same constructor as {@code c2}.
     */
    boolean isSameConstructor(ConstructorInstance c1, ConstructorInstance c2);

    /**
     * Returns true iff {@code m1} is more specific than {@code m2}.
     */
    boolean moreSpecific(ProcedureInstance m1, ProcedureInstance m2);

    /**
     * Returns true iff {@code pi} has exactly the formal arguments
     * {@code formalTypes}.
     */
    boolean hasFormals(ProcedureInstance pi, List<? extends Type> formalTypes);

    ////
    // Functions which yield particular types.
    ////

    /**
     * The type of {@code null}.
     */
    NullType Null();

    /**
     * {@code void}
     */
    PrimitiveType Void();

    /**
     * {@code boolean}
     */
    PrimitiveType Boolean();

    /**
     * {@code char}
     */
    PrimitiveType Char();

    /**
     * {@code byte}
     */
    PrimitiveType Byte();

    /**
     * {@code short}
     */
    PrimitiveType Short();

    /**
     * {@code int}
     */
    PrimitiveType Int();

    /**
     * {@code long}
     */
    PrimitiveType Long();

    /**
     * {@code float}
     */
    PrimitiveType Float();

    /**
     * {@code double}
     */
    PrimitiveType Double();

    /**
     * {@code java.lang.Object}
     */
    ClassType Object();

    /**
     * {@code java.lang.String}
     */
    ClassType String();

    /**
     * {@code java.lang.Class}
     */
    ClassType Class();

    /**
     * {@code java.lang.Throwable}
     */
    ClassType Throwable();

    /**
     * {@code java.lang.Error}
     */
    ClassType Error();

    /**
     * {@code java.lang.Exception}
     */
    ClassType Exception();

    /**
     * {@code java.lang.RuntimeException}
     */
    ClassType RuntimeException();

    /**
     * {@code java.lang.AssertionError}
     */
    ClassType AssertionError();

    /**
     * {@code java.lang.Cloneable}
     */
    ClassType Cloneable();

    /**
     * {@code java.io.Serializable}
     */
    ClassType Serializable();

    /**
     * {@code java.lang.NullPointerException}
     */
    ClassType NullPointerException();

    /**
     * {@code java.lang.ClassCastException}
     */
    ClassType ClassCastException();

    /**
     * {@code java.lang.ArrayIndexOutOfBoundsException}
     */
    ClassType OutOfBoundsException();

    /**
     * {@code java.lang.ArrayStoreException}
     */
    ClassType ArrayStoreException();

    /**
     * {@code java.lang.ArithmeticException}
     */
    ClassType ArithmeticException();

    /**
     * Return an array of {@code type}
     */
    ArrayType arrayOf(Type type);

    /**
     * Return an array of {@code type}
     */
    ArrayType arrayOf(Position pos, Type type);

    /**
     * Return a {@code dims}-array of {@code type}
     */
    ArrayType arrayOf(Type type, int dims);

    /**
     * Return a {@code dims}-array of {@code type}
     */
    ArrayType arrayOf(Position pos, Type type, int dims);

    /**
     * Return a package by name.
     * Fail if the package does not exists.
     */
    Package packageForName(String name) throws SemanticException;

    /**
     * Return a package by name with the given outer package.
     * Fail if the package does not exists.
     */
    Package packageForName(Package prefix, String name)
            throws SemanticException;

    /**
     * Return a package by name.
     */
    Package createPackage(String name);

    /**
     * Return a package by name with the given outer package.
     */
    Package createPackage(Package prefix, String name);

    /**
     * Create a new context object for looking up variables, types, etc.
     */
    Context createContext();

    /** Get a resolver for looking up a type in a package. */
    Resolver packageContextResolver(Package pkg, ClassType accessor);

    Resolver packageContextResolver(Package pkg);

    AccessControlResolver createPackageContextResolver(Package pkg);

    /** @deprecated */
    @Deprecated
    Resolver packageContextResolver(Resolver cr, Package pkg);

    /** Get a resolver for looking up a type in a class context. */
    Resolver classContextResolver(ClassType ct, ClassType accessor);

    Resolver classContextResolver(ClassType ct);

    AccessControlResolver createClassContextResolver(ClassType ct);

    /**
     * The default lazy class initializer.
     */
    LazyClassInitializer defaultClassInitializer();

    /**
     * The lazy class initializer for deserialized classes.
     */
    LazyClassInitializer deserializedClassInitializer();

    /**
     * Create a new empty class.
     */
    ParsedClassType createClassType(LazyClassInitializer init);

    /**
     * Create a new empty class.
     */
    ParsedClassType createClassType();

    /**
     * Create a new empty class.
     */
    ParsedClassType createClassType(LazyClassInitializer init,
            Source fromSource);

    /**
     * Create a new empty class.
     */
    ParsedClassType createClassType(Source fromSource);

    /**
     * Return the set of objects that should be serialized into the
     * type information for the given TypeObject.
     * Usually only the object itself should get encoded, and references
     * to other classes should just have their name written out.
     * If it makes sense for additional types to be fully encoded,
     * (i.e., they're necessary to correctly reconstruct the given object,
     * and the usual class resolvers can't otherwise find them) they
     * should be returned in the set in addition to object.
     */
    Set<? extends TypeObject> getTypeEncoderRootSet(TypeObject o);

    /**
     * Get the transformed class name of a class.
     * This utility method returns the "mangled" name of the given class,
     * whereby all periods ('.') following the top-level class name
     * are replaced with dollar signs ('$'). If any of the containing
     * classes is not a member class or a top level class, then null is
     * returned.
     */
    public String getTransformedClassName(ClassType ct);

    /**
     * Get a place-holder for serializing a type object.
     * @param o The object to get the place-holder for.
     * @param roots The root objects for the serialization.  Place holders
     * are not created for these.
     */
    Object placeHolder(TypeObject o, Set<? extends TypeObject> roots);

    /**
     * Get a place-holder for serializing a type object.
     * @param o The object to get the place-holder for.
     */
    Object placeHolder(TypeObject o);

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
    String translateClass(Resolver c, ClassType t);

    /**
     * Return the boxed version of {@code t}.
     */
    String wrapperTypeString(PrimitiveType t);

    /**
     * Return true if {@code mi} can be called with name {@code name}
     * and actual parameters of types {@code actualTypes}.
     */
    boolean methodCallValid(MethodInstance mi, String name,
            List<? extends Type> argTypes);

    /**
     * Return true if {@code pi} can be called with
     * actual parameters of types {@code actualTypes}.
     */
    boolean callValid(ProcedureInstance mi, List<? extends Type> argTypes);

    /**
     * Get the list of methods {@code mi} (potentially) overrides, in
     * order from this class (that is, including {@code this}) to super
     * classes.
     */
    List<MethodInstance> overrides(MethodInstance mi);

    /**
     * Return true if {@code mi} can override {@code mj}.
     */
    boolean canOverride(MethodInstance mi, MethodInstance mj);

    /**
     * Throw a SemanticException if {@code mi} cannot override
     * {@code mj}.
     */
    void checkOverride(MethodInstance mi, MethodInstance mj)
            throws SemanticException;

    /**
     * Get the list of methods {@code mi} implements, in no
     * specified order.
     */
    List<MethodInstance> implemented(MethodInstance mi);

    <T extends ProcedureInstance> Comparator<T> mostSpecificComparator();

    /**
     * Return the primitive with the given name.
     */
    PrimitiveType primitiveForName(String name) throws SemanticException;

    /** All possible <i>access</i> flags. */
    Flags legalAccessFlags();

    /** All flags allowed for a local variable. */
    Flags legalLocalFlags();

    /** All flags allowed for a field. */
    Flags legalFieldFlags();

    /** All flags allowed for a constructor. */
    Flags legalConstructorFlags();

    /** All flags allowed for an initializer block. */
    Flags legalInitializerFlags();

    /** All flags allowed for a method. */
    Flags legalMethodFlags();

    /** All flags allowed for an abstract method. */
    Flags legalAbstractMethodFlags();

    /** All flags allowed for a top-level class. */
    Flags legalTopLevelClassFlags();

    /** All flags allowed for a member class. */
    Flags legalMemberClassFlags();

    /** All flags allowed for a local class. */
    Flags legalLocalClassFlags();

    /** All flags allowed for a field declared in an interface. */
    Flags legalInterfaceFieldFlags();

    /**
     * Assert if the flags {@code f} are legal method flags.
     */
    void checkMethodFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal local variable flags.
     */
    void checkLocalFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal field flags.
     */
    void checkFieldFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal constructor flags.
     */
    void checkConstructorFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal initializer flags.
     */
    void checkInitializerFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal top-level class flags.
     */
    void checkTopLevelClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal member class flags.
     */
    void checkMemberClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal local class flags.
     */
    void checkLocalClassFlags(Flags f) throws SemanticException;

    /**
     * Assert if the flags {@code f} are legal access flags.
     */
    void checkAccessFlags(Flags f) throws SemanticException;

    /**
     * Assert that {@code t} has no cycles in the super type+nested class
     * graph starting at {@code t}.
     */
    void checkCycles(ReferenceType t) throws SemanticException;

    /**
     * Assert that {@code ct} implements all abstract methods required;
     * that is, if it is a concrete class, then it must implement all
     * interfaces and abstract methods that it or its superclasses declare.
     */
    void checkClassConformance(ClassType ct) throws SemanticException;

    /**
     * Assert that if {@code ct} is an interface, its fields' flags is a subset
     * of public, static, final.
     */
    void checkInterfaceFieldFlags(ClassType ct) throws SemanticException;

    /**
     * Find a potentially suitable implementation of the method {@code mi}
     * in the class {@code ct} or a supertype thereof. Since we are
     * looking for implementations, {@code ct} cannot be an interface.
     * The first potentially satisfying method is returned, that is, the method
     * that is visible from {@code ct}, with the correct signature, in
     * the most precise class in the class hierarchy starting from
     * {@code ct}.
     *
     * @return a suitable implementation of the method mi in the class
     *         {@code ct} or a supertype thereof, null if none exists.
     */
    public MethodInstance findImplementingMethod(ClassType ct,
            MethodInstance mi);

    /**
     * Returns {@code t}, modified as necessary to make it a legal
     * static target.
     */
    public Type staticTarget(Type t);

    /**
     * Given the JVM encoding of a set of flags, returns the Flags object
     * for that encoding.
     */
    public Flags flagsForBits(int bits);

    /**
     * Create a new unique Flags object.
     * @param name the name of the flag
     * @param print_after print the new flag after these flags
     */
    public Flags createNewFlag(String name, Flags print_after);

    public Flags NoFlags();

    public Flags Public();

    public Flags Protected();

    public Flags Private();

    public Flags Static();

    public Flags Final();

    public Flags Synchronized();

    public Flags Transient();

    public Flags Native();

    public Flags Interface();

    public Flags Abstract();

    public Flags Volatile();

    public Flags StrictFP();

}
