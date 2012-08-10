/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StringUtil;

/**
 * TypeSystem_c
 *
 * Overview:
 *    A TypeSystem_c is a universe of types, including all Java types.
 **/
public class TypeSystem_c implements TypeSystem {
    protected SystemResolver systemResolver;
    protected TopLevelResolver loadedResolver;
    protected Map<String, Flags> flagsForName;
    protected ExtensionInfo extInfo;

    public TypeSystem_c() {
    }

    /**
     * Initializes the type system and its internal constants (which depend on
     * the resolver).
     */
    @Override
    public void initialize(TopLevelResolver loadedResolver,
            ExtensionInfo extInfo) throws SemanticException {

        if (Report.should_report(Report.types, 1))
            Report.report(1, "Initializing " + getClass().getName());

        this.extInfo = extInfo;

        // The loaded class resolver.  This resolver automatically loads types
        // from class files and from source files not mentioned on the command
        // line.
        this.loadedResolver = loadedResolver;

        // The system class resolver. The class resolver contains a map from
        // fully qualified names to instances of Named. A pass over a
        // compilation unit looks up classes first in its
        // import table and then in the system resolver.
        this.systemResolver = new SystemResolver(loadedResolver, extInfo);

        initEnums();
        initFlags();
        initTypes();
    }

    protected void initEnums() {
        // Ensure the enums in the type system are initialized and interned
        // before any deserialization occurs.

        // Just force the static initializers of ClassType and PrimitiveType
        // to run.
        @SuppressWarnings("unused")
        Object o;
        o = ClassType.TOP_LEVEL;
        o = PrimitiveType.VOID;
    }

    /**
     * @throws SemanticException  
     */
    protected void initTypes() throws SemanticException {
        // FIXME: don't do this when rewriting a type system!

        // Prime the resolver cache so that we don't need to check
        // later if these are loaded.

        // We cache the most commonly used ones in fields.
        /* DISABLED CACHING OF COMMON CLASSES; CAUSES PROBLEMS IF
           COMPILING CORE CLASSES (e.g. java.lang package).
           TODO: Longer term fix. Maybe a flag to tell if we are compiling
                 core classes? XXX
        Object();
        Class();
        String();
        Throwable();

        systemResolver.find("java.lang.Error");
        systemResolver.find("java.lang.Exception");
        systemResolver.find("java.lang.RuntimeException");
        systemResolver.find("java.lang.Cloneable");
        systemResolver.find("java.io.Serializable");
        systemResolver.find("java.lang.NullPointerException");
        systemResolver.find("java.lang.ClassCastException");
        systemResolver.find("java.lang.ArrayIndexOutOfBoundsException");
        systemResolver.find("java.lang.ArrayStoreException");
        systemResolver.find("java.lang.ArithmeticException");
        */
    }

    /** Return the language extension this type system is for. */
    @Override
    public ExtensionInfo extensionInfo() {
        return extInfo;
    }

    @Override
    public SystemResolver systemResolver() {
        return systemResolver;
    }

    @Override
    public SystemResolver saveSystemResolver() {
        SystemResolver r = this.systemResolver;
        this.systemResolver = r.copy();
        return r;
    }

    @Override
    public void restoreSystemResolver(SystemResolver r) {
        if (r != this.systemResolver.previous()) {
            throw new InternalCompilerError("Inconsistent systemResolver.previous");
        }
        this.systemResolver = r;
    }

    /**
     * Return the system resolver.  This used to return a different resolver.
     * enclosed in the system resolver.
     * @deprecated
     */
    @Deprecated
    @Override
    public CachingResolver parsedResolver() {
        return systemResolver;
    }

    @Override
    public TopLevelResolver loadedResolver() {
        return loadedResolver;
    }

    @Override
    public ClassFileLazyClassInitializer classFileLazyClassInitializer(
            ClassFile clazz) {
        return new ClassFileLazyClassInitializer(clazz, this);
    }

    @Override
    public ImportTable importTable(String sourceName, Package pkg) {
        assert_(pkg);
        return new ImportTable(this, pkg, sourceName);
    }

    @Override
    public ImportTable importTable(Package pkg) {
        assert_(pkg);
        return new ImportTable(this, pkg);
    }

    /**
     * Returns true if the package named <code>name</code> exists.
     */
    @Override
    public boolean packageExists(String name) {
        return systemResolver.packageExists(name);
    }

    protected void assert_(Collection<? extends TypeObject> l) {
        for (TypeObject o : l) {
            assert_(o);
        }
    }

    protected void assert_(TypeObject o) {
        if (o != null && o.typeSystem() != this) {
            throw new InternalCompilerError("we are " + this + " but " + o
                    + " (" + o.getClass() + ")" + " is from " + o.typeSystem());
        }
    }

    @Override
    public String wrapperTypeString(PrimitiveType t) {
        assert_(t);

        if (t.kind() == PrimitiveType.BOOLEAN) {
            return "java.lang.Boolean";
        }
        if (t.kind() == PrimitiveType.CHAR) {
            return "java.lang.Character";
        }
        if (t.kind() == PrimitiveType.BYTE) {
            return "java.lang.Byte";
        }
        if (t.kind() == PrimitiveType.SHORT) {
            return "java.lang.Short";
        }
        if (t.kind() == PrimitiveType.INT) {
            return "java.lang.Integer";
        }
        if (t.kind() == PrimitiveType.LONG) {
            return "java.lang.Long";
        }
        if (t.kind() == PrimitiveType.FLOAT) {
            return "java.lang.Float";
        }
        if (t.kind() == PrimitiveType.DOUBLE) {
            return "java.lang.Double";
        }
        if (t.kind() == PrimitiveType.VOID) {
            return "java.lang.Void";
        }

        throw new InternalCompilerError("Unrecognized primitive type.");
    }

    @Override
    public Context createContext() {
        return new Context_c(this);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public Resolver packageContextResolver(Resolver cr, Package p) {
        return packageContextResolver(p);
    }

    @Override
    public AccessControlResolver createPackageContextResolver(Package p) {
        assert_(p);
        return new PackageContextResolver(this, p);
    }

    @Override
    public Resolver packageContextResolver(Package p, ClassType accessor) {
        if (accessor == null) {
            return p.resolver();
        }
        else {
            return new AccessControlWrapperResolver(createPackageContextResolver(p),
                                                    accessor);
        }
    }

    @Override
    public Resolver packageContextResolver(Package p) {
        assert_(p);
        return packageContextResolver(p, null);
    }

    @Override
    public Resolver classContextResolver(ClassType type, ClassType accessor) {
        assert_(type);
        if (accessor == null) {
            return type.resolver();
        }
        else {
            return new AccessControlWrapperResolver(createClassContextResolver(type),
                                                    accessor);
        }
    }

    @Override
    public Resolver classContextResolver(ClassType type) {
        return classContextResolver(type, null);
    }

    @Override
    public AccessControlResolver createClassContextResolver(ClassType type) {
        assert_(type);
        return new ClassContextResolver(this, type);
    }

    @Override
    public FieldInstance fieldInstance(Position pos, ReferenceType container,
            Flags flags, Type type, String name) {
        assert_(container);
        assert_(type);
        return new FieldInstance_c(this, pos, container, flags, type, name);
    }

    @Override
    public LocalInstance localInstance(Position pos, Flags flags, Type type,
            String name) {
        assert_(type);
        return new LocalInstance_c(this, pos, flags, type, name);
    }

    @Override
    public ConstructorInstance defaultConstructor(Position pos,
            ClassType container) {
        assert_(container);

        // access for the default constructor is determined by the 
        // access of the containing class. See the JLS, 2nd Ed., 8.8.7.
        Flags access = Flags.NONE;
        if (container.flags().isPrivate()) {
            access = access.Private();
        }
        if (container.flags().isProtected()) {
            access = access.Protected();
        }
        if (container.flags().isPublic()) {
            access = access.Public();
        }
        return constructorInstance(pos,
                                   container,
                                   access,
                                   Collections.<Type> emptyList(),
                                   Collections.<Type> emptyList());
    }

    @Override
    public ConstructorInstance constructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes) {
        assert_(container);
        assert_(argTypes);
        assert_(excTypes);
        return new ConstructorInstance_c(this,
                                         pos,
                                         container,
                                         flags,
                                         argTypes,
                                         excTypes);
    }

    @Override
    public InitializerInstance initializerInstance(Position pos,
            ClassType container, Flags flags) {
        assert_(container);
        return new InitializerInstance_c(this, pos, container, flags);
    }

    @Override
    public MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes) {

        assert_(container);
        assert_(returnType);
        assert_(argTypes);
        assert_(excTypes);
        return new MethodInstance_c(this,
                                    pos,
                                    container,
                                    flags,
                                    returnType,
                                    name,
                                    argTypes,
                                    excTypes);
    }

    /**
     * Returns true iff child and ancestor are distinct
     * reference types, and child descends from ancestor.
     **/
    @Override
    public boolean descendsFrom(Type child, Type ancestor) {
        assert_(child);
        assert_(ancestor);
        return child.descendsFromImpl(ancestor);
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from fromType to toType is valid; in other
     * words, some non-null members of fromType are also members of toType.
     **/
    @Override
    public boolean isCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isCastValidImpl(toType);
    }

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns true iff an implicit cast from fromType to toType is valid;
     * in other words, every member of fromType is member of toType.
     *
     * Returns true iff child and ancestor are non-primitive
     * types, and a variable of type child may be legally assigned
     * to a variable of type ancestor.
     *
     */
    @Override
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isImplicitCastValidImpl(toType);
    }

    /**
     * Returns true iff type1 and type2 represent the same type object.
     */
    @Override
    public boolean equals(TypeObject type1, TypeObject type2) {
        assert_(type1);
        assert_(type2);
        if (type1 == type2) return true;
        if (type1 == null || type2 == null) return false;
        return type1.equalsImpl(type2);
    }

    /**
     * Returns true iff type1 and type2 are equivalent.
     */
    @Override
    public boolean typeEquals(Type type1, Type type2) {
        assert_(type1);
        assert_(type2);
        return type1.typeEqualsImpl(type2);
    }

    /**
     * Returns true iff type1 and type2 are equivalent.
     */
    @Override
    public boolean packageEquals(Package type1, Package type2) {
        assert_(type1);
        assert_(type2);
        return type1.packageEqualsImpl(type2);
    }

    /**
     * Returns true if <code>value</code> can be implicitly cast to Primitive
     * type <code>t</code>.
     */
    @Override
    public boolean numericConversionValid(Type t, Object value) {
        assert_(t);
        return t.numericConversionValidImpl(value);
    }

    /**
     * Returns true if <code>value</code> can be implicitly cast to Primitive
     * type <code>t</code>.  This method should be removed.  It is kept for
     * backward compatibility.
     */
    @Override
    public boolean numericConversionValid(Type t, long value) {
        assert_(t);
        return t.numericConversionValidImpl(value);
    }

    ////
    // Functions for one-type checking and resolution.
    ////

    /**
     * Returns true iff <type> is a canonical (fully qualified) type.
     */
    @Override
    public boolean isCanonical(Type type) {
        assert_(type);
        return type.isCanonical();
    }

    /**
     * Checks whether the member mi can be accessed from Context "context".
     */
    @Override
    public boolean isAccessible(MemberInstance mi, Context context) {
        return isAccessible(mi, context.currentClass());
    }

    /**
     * Checks whether the member mi can be accessed from code that is
     * declared in the class contextClass.
     */
    @Override
    public boolean isAccessible(MemberInstance mi, ClassType contextClass) {
        assert_(mi);

        ReferenceType target = mi.container();
        return isAccessible(mi, target, contextClass);
    }

    /**
     * Checks whether the member mi of container container can be accessed from code that is
     * declared in the class contextClass.
     */
    @Override
    public boolean isAccessible(MemberInstance mi, ReferenceType container,
            ClassType contextClass) {
        Flags flags = mi.flags();

        ReferenceType target;
        // does container inhereit mi?
        if (container.descendsFrom(mi.container()) && mi.flags().isPublic()) {
            target = container;
        }
        else {
            target = mi.container();
        }

        if (!target.isClass()) {
            // public members of non-classes are accessible;
            // non-public members of non-classes are inaccessible
            return flags.isPublic();
        }

        ClassType targetClass = target.toClass();

        if (!classAccessible(targetClass, contextClass)) {
            return false;
        }

        if (equals(targetClass, contextClass)) return true;

        // If the current class and the target class are both in the
        // same class body, then protection doesn't matter, i.e.
        // protected and private members may be accessed. Do this by
        // working up through contextClass's containers.
        if (isEnclosed(contextClass, targetClass)
                || isEnclosed(targetClass, contextClass)) return true;

        ClassType ct = contextClass;
        while (!ct.isTopLevel()) {
            ct = ct.outer();
            if (isEnclosed(targetClass, ct)) return true;
        }

        // protected
        if (flags.isProtected()) {
            // If the current class is in a
            // class body that extends/implements the target class, then
            // protected members can be accessed. Do this by
            // working up through contextClass's containers.
            if (descendsFrom(contextClass, targetClass)) {
                return true;
            }

            ct = contextClass;
            while (!ct.isTopLevel()) {
                ct = ct.outer();
                if (descendsFrom(ct, targetClass)) {
                    return true;
                }
            }
        }

        return accessibleFromPackage(flags,
                                     targetClass.package_(),
                                     contextClass.package_());
    }

    /** True if the class targetClass accessible from the context. */
    @Override
    public boolean classAccessible(ClassType targetClass, Context context) {
        if (context.currentClass() == null) {
            return classAccessibleFromPackage(targetClass,
                                              context.importTable().package_());
        }
        else {
            return classAccessible(targetClass, context.currentClass());
        }
    }

    /** True if the class targetClass accessible from the body of class contextClass. */
    @Override
    public boolean classAccessible(ClassType targetClass, ClassType contextClass) {
        assert_(targetClass);

        if (targetClass.isMember()) {
            return isAccessible(targetClass, contextClass);
        }

        // Local and anonymous classes are accessible if they can be named.
        // This method wouldn't be called if they weren't named.
        if (!targetClass.isTopLevel()) {
            return true;
        }

        // targetClass must be a top-level class

        // same class
        if (equals(targetClass, contextClass)) return true;

        if (isEnclosed(contextClass, targetClass)) return true;

        return classAccessibleFromPackage(targetClass, contextClass.package_());
    }

    /** True if the class targetClass accessible from the package pkg. */
    @Override
    public boolean classAccessibleFromPackage(ClassType targetClass, Package pkg) {
        assert_(targetClass);

        // Local and anonymous classes are not accessible from the outermost
        // scope of a compilation unit.
        if (!targetClass.isTopLevel() && !targetClass.isMember()) return false;

        Flags flags = targetClass.flags();

        if (targetClass.isMember()) {
            if (!targetClass.container().isClass()) {
                // public members of non-classes are accessible
                return flags.isPublic();
            }

            if (!classAccessibleFromPackage(targetClass.container().toClass(),
                                            pkg)) {
                return false;
            }
        }

        return accessibleFromPackage(flags, targetClass.package_(), pkg);
    }

    /**
     * Return true if a member (in an accessible container) or a
     * top-level class with access flags <code>flags</code>
     * in package <code>pkg1</code> is accessible from package
     * <code>pkg2</code>.
     */
    protected boolean accessibleFromPackage(Flags flags, Package pkg1,
            Package pkg2) {
        // Check if public.
        if (flags.isPublic()) {
            return true;
        }

        // Check if same package.
        if (flags.isPackage() || flags.isProtected()) {
            if (pkg1 == null && pkg2 == null) return true;
            if (pkg1 != null && pkg1.equals(pkg2)) return true;
        }

        // Otherwise private.
        return false;
    }

    @Override
    public boolean isEnclosed(ClassType inner, ClassType outer) {
        return inner.isEnclosedImpl(outer);
    }

    @Override
    public boolean hasEnclosingInstance(ClassType inner, ClassType encl) {
        return inner.hasEnclosingInstanceImpl(encl);
    }

    @Override
    public void checkCycles(ReferenceType goal) throws SemanticException {
        checkCycles(goal, goal);
    }

    protected void checkCycles(ReferenceType curr, ReferenceType goal)
            throws SemanticException {

        assert_(curr);
        assert_(goal);

        if (curr == null) {
            return;
        }

        ReferenceType superType = null;

        if (curr.superType() != null) {
            superType = curr.superType().toReference();
        }

        if (goal == superType) {
            throw new SemanticException("Circular inheritance involving "
                    + goal, curr.position());
        }

        checkCycles(superType, goal);

        for (Type si : curr.interfaces()) {
            if (si == goal) {
                throw new SemanticException("Circular inheritance involving "
                        + goal, curr.position());
            }

            checkCycles(si.toReference(), goal);
        }
        if (curr.isClass()) {
            checkCycles(curr.toClass().outer(), goal);
        }
    }

    ////
    // Various one-type predicates.
    ////

    /**
     * Returns true iff the type t can be coerced to a String in the given
     * Context. If a type can be coerced to a String then it can be
     * concatenated with Strings, e.g. if o is of type T, then the code snippet
     *         "" + o
     * would be allowed.
     */
    @Override
    public boolean canCoerceToString(Type t, Context c) {
        // every Object can be coerced to a string, as can any primitive,
        // except void.
        return !t.isVoid();
    }

    /**
     * Returns true iff an object of type <type> may be thrown.
     **/
    @Override
    public boolean isThrowable(Type type) {
        assert_(type);
        return type.isThrowable();
    }

    /**
     * Returns a true iff the type or a supertype is in the list
     * returned by uncheckedExceptions().
     */
    @Override
    public boolean isUncheckedException(Type type) {
        assert_(type);
        return type.isUncheckedException();
    }

    /**
     * Returns a list of the Throwable types that need not be declared
     * in method and constructor signatures.
     */
    @Override
    public Collection<Type> uncheckedExceptions() {
        List<Type> l = new ArrayList<Type>(2);
        l.add(Error());
        l.add(RuntimeException());
        return l;
    }

    @Override
    public boolean isSubtype(Type t1, Type t2) {
        assert_(t1);
        assert_(t2);
        return t1.isSubtypeImpl(t2);
    }

    ////
    // Functions for type membership.
    ////

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public FieldInstance findField(ReferenceType container, String name,
            Context c) throws SemanticException {
        ClassType ct = null;
        if (c != null) ct = c.currentClass();
        return findField(container, name, ct);
    }

    /**
     * Returns the FieldInstance for the field <code>name</code> defined
     * in type <code>container</code> or a supertype, and visible from
     * <code>currClass</code>.  If no such field is found, a SemanticException
     * is thrown.  <code>currClass</code> may be null.
     **/
    @Override
    public FieldInstance findField(ReferenceType container, String name,
            ClassType currClass) throws SemanticException {
        Collection<FieldInstance> fields = findFields(container, name);

        if (fields.size() == 0) {
            throw new NoMemberException(NoMemberException.FIELD, "Field \""
                    + name + "\" not found in type \"" + container + "\".");
        }

        Iterator<FieldInstance> i = fields.iterator();
        FieldInstance fi = i.next();

        if (i.hasNext()) {
            FieldInstance fi2 = i.next();

            throw new SemanticException("Field \"" + name
                    + "\" is ambiguous; it is defined in both "
                    + fi.container() + " and " + fi2.container() + ".");
        }

        if (currClass != null && !isAccessible(fi, currClass)) {
            throw new SemanticException("Cannot access " + fi + ".");
        }

        return fi;
    }

    /**
     * Returns the FieldInstance for the field <code>name</code> defined
     * in type <code>container</code> or a supertype.  If no such field is
     * found, a SemanticException is thrown.
     */
    @Override
    public FieldInstance findField(ReferenceType container, String name)
            throws SemanticException {

        return findField(container, name, (ClassType) null);
    }

    /**
     * Returns a set of fields named <code>name</code> defined
     * in type <code>container</code> or a supertype.  The list
     * returned may be empty.
     */
    protected Set<FieldInstance> findFields(ReferenceType container, String name) {
        assert_(container);

        if (container == null) {
            throw new InternalCompilerError("Cannot access field \"" + name
                    + "\" within a null container type.");
        }

        FieldInstance fi = container.fieldNamed(name);

        if (fi != null) {
            return Collections.singleton(fi);
        }

        Set<FieldInstance> fields = new HashSet<FieldInstance>();

        if (container.superType() != null
                && container.superType().isReference()) {
            Set<FieldInstance> superFields =
                    findFields(container.superType().toReference(), name);
            fields.addAll(superFields);
        }

        if (container.isClass()) {
            // Need to check interfaces for static fields.
            ClassType ct = container.toClass();

            for (Type it : ct.interfaces()) {
                Set<FieldInstance> superFields =
                        findFields(it.toReference(), name);
                fields.addAll(superFields);
            }
        }

        return fields;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public ClassType findMemberClass(ClassType container, String name, Context c)
            throws SemanticException {
        return findMemberClass(container, name, c.currentClass());
    }

    @Override
    public ClassType findMemberClass(ClassType container, String name,
            ClassType currClass) throws SemanticException {
        assert_(container);

        Named n = classContextResolver(container, currClass).find(name);

        if (n instanceof ClassType) {
            return (ClassType) n;
        }

        throw new NoClassException(name, container);
    }

    @Override
    public ClassType findMemberClass(ClassType container, String name)
            throws SemanticException {

        return findMemberClass(container, name, (ClassType) null);
    }

    protected static String listToString(List<?> l) {
        StringBuffer sb = new StringBuffer();

        for (Iterator<?> i = l.iterator(); i.hasNext();) {
            Object o = i.next();
            sb.append(o.toString());

            if (i.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, Context c) throws SemanticException {
        return findMethod(container, name, argTypes, c.currentClass());
    }

    /**
     * Returns the list of methods with the given name defined or inherited
     * into container, checking if the methods are accessible from the
     * body of currClass
     */
    @Override
    public boolean hasMethodNamed(ReferenceType container, String name) {
        assert_(container);

        if (container == null) {
            throw new InternalCompilerError("Cannot access method \"" + name
                    + "\" within a null container type.");
        }

        if (!container.methodsNamed(name).isEmpty()) {
            return true;
        }

        if (container.superType() != null
                && container.superType().isReference()) {
            if (hasMethodNamed(container.superType().toReference(), name)) {
                return true;
            }
        }

        if (container.isClass()) {
            ClassType ct = container.toClass();

            for (Type it : ct.interfaces()) {
                if (hasMethodNamed(it.toReference(), name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasAccessibleMethodNamed(ReferenceType container,
            String name, ClassType currClass) {
        assert_(container);

        if (container == null) {
            throw new InternalCompilerError("Cannot access method \"" + name
                    + "\" within a null container type.");
        }

        Set<Type> visitedTypes = new HashSet<Type>();

        LinkedList<Type> typeQueue = new LinkedList<Type>();
        typeQueue.addLast(container);

        while (!typeQueue.isEmpty()) {
            Type type = typeQueue.removeFirst();

            if (visitedTypes.contains(type)) {
                continue;
            }

            visitedTypes.add(type);

            if (!type.isReference()) {
                continue;
            }

            for (MethodInstance mi : type.toReference().methodsNamed(name)) {
                if (isAccessible(mi, container, currClass)) {
                    return true;
                }
            }
            if (type.toReference().superType() != null) {
                typeQueue.addLast(type.toReference().superType());
            }

            typeQueue.addAll(type.toReference().interfaces());
        }
        return false;
    }

    /**
     * Requires: all type arguments are canonical.
     *
     * Returns the MethodInstance named 'name' defined on 'type' visible in
     * context.  If no such field may be found, returns a fieldmatch
     * with an error explaining why.  Access flags are considered.
     **/
    @Override
    public MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass)
            throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<? extends MethodInstance> acceptable =
                findAcceptableMethods(container, name, argTypes, currClass);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.METHOD,
                                        "No valid method call found for "
                                                + name + "("
                                                + listToString(argTypes) + ")"
                                                + " in " + container + ".");
        }

        Collection<? extends MethodInstance> maximal =
                findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<? extends MethodInstance> i = maximal.iterator(); i.hasNext();) {
                MethodInstance ma = i.next();
                sb.append(ma.returnType());
                sb.append(" ");
                sb.append(ma.container());
                sb.append(".");
                sb.append(ma.signature());
                if (i.hasNext()) {
                    if (maximal.size() == 2) {
                        sb.append(" and ");
                    }
                    else {
                        sb.append(", ");
                    }
                }
            }

            throw new SemanticException("Reference to " + name
                    + " is ambiguous, multiple methods match: " + sb.toString());
        }

        MethodInstance mi = maximal.iterator().next();
        return mi;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, Context c) throws SemanticException {
        return findConstructor(container, argTypes, c.currentClass());
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass)
            throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<? extends ConstructorInstance> acceptable =
                findAcceptableConstructors(container, argTypes, currClass);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                                        "No valid constructor found for "
                                                + container + "("
                                                + listToString(argTypes) + ").");
        }

        Collection<? extends ConstructorInstance> maximal =
                findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                                        "Reference to " + container
                                                + " is ambiguous, multiple "
                                                + "constructors match: "
                                                + maximal);
        }

        ConstructorInstance ci = maximal.iterator().next();
        return ci;
    }

    protected <I extends ProcedureInstance> I findProcedure(List<I> acceptable,
            ReferenceType container, List<Type> argTypes, ClassType currClass)
            throws SemanticException {
        Collection<I> maximal = findMostSpecificProcedures(acceptable);

        if (maximal.size() == 1) {
            return maximal.iterator().next();
        }
        return null;
    }

    /**
     * @throws SemanticException  
     */
    protected <Instance extends ProcedureInstance> Collection<Instance> findMostSpecificProcedures(
            List<Instance> acceptable) throws SemanticException {

        // now, use JLS 15.11.2.2
        // First sort from most- to least-specific.
        MostSpecificComparator<Instance> msc =
                new MostSpecificComparator<Instance>();
        acceptable = new ArrayList<Instance>(acceptable); // make into array list to sort
        Collections.sort(acceptable, msc);

        List<Instance> maximal = new ArrayList<Instance>(acceptable.size());

        Iterator<Instance> i = acceptable.iterator();

        Instance first = i.next();
        maximal.add(first);

        // Now check to make sure that we have a maximal most-specific method.
        while (i.hasNext()) {
            Instance p = i.next();

            if (msc.compare(first, p) >= 0) {
                maximal.add(p);
            }
        }

        if (maximal.size() > 1) {
            // If exactly one method is not abstract, it is the most specific.
            List<Instance> notAbstract =
                    new ArrayList<Instance>(maximal.size());
            for (Instance p : maximal) {
                if (!p.flags().isAbstract()) {
                    notAbstract.add(p);
                }
            }

            if (notAbstract.size() == 1) {
                maximal = notAbstract;
            }
            else if (notAbstract.size() == 0) {
                // all are abstract; if all signatures match, any will do.
                Iterator<Instance> j = maximal.iterator();
                first = j.next();
                while (j.hasNext()) {
                    Instance p = j.next();

                    // Use the declarations to compare formals.
                    ProcedureInstance firstDecl = first;
                    ProcedureInstance pDecl = p;
                    if (first instanceof Declaration) {
                        firstDecl =
                                (ProcedureInstance) ((Declaration) first).declaration();
                    }
                    if (p instanceof Declaration) {
                        pDecl =
                                (ProcedureInstance) ((Declaration) p).declaration();
                    }

                    if (!firstDecl.hasFormals(pDecl.formalTypes())) {
                        // not all signatures match; must be ambiguous
                        return maximal;
                    }
                }

                // all signatures match, just take the first
                maximal = Collections.singletonList(first);
            }
        }

        return maximal;
    }

    /**
     * Class to handle the comparisons; dispatches to moreSpecific method.
     */
    protected static class MostSpecificComparator<T extends ProcedureInstance>
            implements Comparator<T> {
        @Override
        public int compare(T p1, T p2) {
            if (p1.moreSpecific(p2)) return -1;
            if (p2.moreSpecific(p1)) return 1;
            return 0;
        }
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.11.2.1
     */
    protected List<? extends MethodInstance> findAcceptableMethods(
            ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass)
            throws SemanticException {

        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        // The list of acceptable methods. These methods are accessible from
        // currClass, the method call is valid, and they are not overridden
        // by an unacceptable method (which can occur with protected methods
        // only).
        List<MethodInstance> acceptable = new ArrayList<MethodInstance>();

        // A list of unacceptable methods, where the method call is valid, but
        // the method is not accessible. This list is needed to make sure that
        // the acceptable methods are not overridden by an unacceptable method.
        List<MethodInstance> unacceptable = new ArrayList<MethodInstance>();

        Set<Type> visitedTypes = new HashSet<Type>();

        LinkedList<Type> typeQueue = new LinkedList<Type>();
        typeQueue.addLast(container);

        while (!typeQueue.isEmpty()) {
            Type type = typeQueue.removeFirst();

            if (visitedTypes.contains(type)) {
                continue;
            }

            visitedTypes.add(type);

            if (Report.should_report(Report.types, 2))
                Report.report(2, "Searching type " + type + " for method "
                        + name + "(" + listToString(argTypes) + ")");

            if (!type.isReference()) {
                throw new SemanticException("Cannot call method in "
                        + " non-reference type " + type + ".");
            }

            for (MethodInstance mi : type.toReference().methods()) {
                if (Report.should_report(Report.types, 3))
                    Report.report(3, "Trying " + mi);

                if (!mi.name().equals(name)) {
                    continue;
                }

                if (methodCallValid(mi, name, argTypes)) {
                    if (isAccessible(mi, container, currClass)) {
                        if (Report.should_report(Report.types, 3)) {
                            Report.report(3, "->acceptable: " + mi + " in "
                                    + mi.container());
                        }

                        acceptable.add(mi);
                    }
                    else {
                        // method call is valid, but the method is
                        // unacceptable.
                        unacceptable.add(mi);
                        if (error == null) {
                            error =
                                    new NoMemberException(NoMemberException.METHOD,
                                                          "Method "
                                                                  + mi.signature()
                                                                  + " in "
                                                                  + container
                                                                  + " is inaccessible.");
                        }
                    }
                }
                else {
                    if (error == null) {
                        error =
                                new NoMemberException(NoMemberException.METHOD,
                                                      "Method "
                                                              + mi.signature()
                                                              + " in "
                                                              + container
                                                              + " cannot be called with arguments "
                                                              + "("
                                                              + listToString(argTypes)
                                                              + ").");
                    }
                }
            }
            if (type.toReference().superType() != null) {
                typeQueue.addLast(type.toReference().superType());
            }

            typeQueue.addAll(type.toReference().interfaces());
        }

        if (error == null) {
            error =
                    new NoMemberException(NoMemberException.METHOD,
                                          "No valid method call found for "
                                                  + name + "("
                                                  + listToString(argTypes)
                                                  + ")" + " in " + container
                                                  + ".");
        }

        if (acceptable.size() == 0) {
            throw error;
        }

        // remove any method in acceptable that are overridden by an
        // unacceptable
        // method.
        for (MethodInstance mi : unacceptable) {
            acceptable.removeAll(mi.overrides());
        }

        if (acceptable.size() == 0) {
            throw error;
        }

        return acceptable;
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.11.2.1
     */
    protected List<? extends ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            ClassType currClass) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        List<ConstructorInstance> acceptable =
                new ArrayList<ConstructorInstance>();

        if (Report.should_report(Report.types, 2))
            Report.report(2, "Searching type " + container
                    + " for constructor " + container + "("
                    + listToString(argTypes) + ")");

        for (ConstructorInstance ci : container.constructors()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "Trying " + ci);

            if (callValid(ci, argTypes)) {
                if (isAccessible(ci, currClass)) {
                    if (Report.should_report(Report.types, 3))
                        Report.report(3, "->acceptable: " + ci);
                    acceptable.add(ci);
                }
                else {
                    if (error == null) {
                        error =
                                new NoMemberException(NoMemberException.CONSTRUCTOR,
                                                      "Constructor "
                                                              + ci.signature()
                                                              + " is inaccessible.");
                    }
                }
            }
            else {
                if (error == null) {
                    error =
                            new NoMemberException(NoMemberException.CONSTRUCTOR,
                                                  "Constructor "
                                                          + ci.signature()
                                                          + " cannot be invoked with arguments "
                                                          + "("
                                                          + listToString(argTypes)
                                                          + ").");

                }
            }
        }

        if (acceptable.size() == 0) {
            if (error == null) {
                error =
                        new NoMemberException(NoMemberException.CONSTRUCTOR,
                                              "No valid constructor found for "
                                                      + container + "("
                                                      + listToString(argTypes)
                                                      + ").");
            }

            throw error;
        }

        return acceptable;
    }

    /**
     * Returns whether method 1 is <i>more specific</i> than method 2,
     * where <i>more specific</i> is defined as JLS 15.11.2.2
     */
    @Override
    public boolean moreSpecific(ProcedureInstance p1, ProcedureInstance p2) {
        return p1.moreSpecificImpl(p2);
    }

    /**
     * Returns the supertype of type, or null if type has no supertype.
     **/
    @Override
    public Type superType(ReferenceType type) {
        assert_(type);
        return type.superType();
    }

    /**
     * Returns an immutable list of all the interface types which type
     * implements.
     **/
    @Override
    public List<? extends Type> interfaces(ReferenceType type) {
        assert_(type);
        return type.interfaces();
    }

    /**
     * Requires: all type arguments are canonical.
     * Returns the least common ancestor of Type1 and Type2
     **/
    @Override
    public Type leastCommonAncestor(Type type1, Type type2)
            throws SemanticException {
        assert_(type1);
        assert_(type2);

        if (typeEquals(type1, type2)) return type1;

        if (type1.isNumeric() && type2.isNumeric()) {
            if (isImplicitCastValid(type1, type2)) {
                return type2;
            }

            if (isImplicitCastValid(type2, type1)) {
                return type1;
            }

            if (type1.isChar() && type2.isByte() || type1.isByte()
                    && type2.isChar()) {
                return Int();
            }

            if (type1.isChar() && type2.isShort() || type1.isShort()
                    && type2.isChar()) {
                return Int();
            }
        }

        if (type1.isArray() && type2.isArray()) {
            return arrayOf(leastCommonAncestor(type1.toArray().base(),
                                               type2.toArray().base()));
        }

        if (type1.isReference() && type2.isNull()) return type1;
        if (type2.isReference() && type1.isNull()) return type2;

        if (type1.isReference() && type2.isReference()) {
            // Don't consider interfaces.
            if (type1.isClass() && type1.toClass().flags().isInterface()) {
                return Object();
            }

            if (type2.isClass() && type2.toClass().flags().isInterface()) {
                return Object();
            }

            // Check against Object to ensure superType() is not null.
            if (typeEquals(type1, Object())) return type1;
            if (typeEquals(type2, Object())) return type2;

            if (isSubtype(type1, type2)) return type2;
            if (isSubtype(type2, type1)) return type1;

            // Walk up the hierarchy
            Type t1 =
                    leastCommonAncestor(type1.toReference().superType(), type2);
            Type t2 =
                    leastCommonAncestor(type2.toReference().superType(), type1);

            if (typeEquals(t1, t2)) return t1;

            return Object();
        }

        throw new SemanticException("No least common ancestor found for types \""
                + type1 + "\" and \"" + type2 + "\".");
    }

    ////
    // Functions for method testing.
    ////

    /**
     * Returns true iff <p1> throws fewer exceptions than <p2>.
     */
    @Override
    public boolean throwsSubset(ProcedureInstance p1, ProcedureInstance p2) {
        assert_(p1);
        assert_(p2);
        return p1.throwsSubsetImpl(p2);
    }

    /** Return true if t overrides mi */
    @Override
    public boolean hasFormals(ProcedureInstance pi,
            List<? extends Type> formalTypes) {
        assert_(pi);
        assert_(formalTypes);
        return pi.hasFormalsImpl(formalTypes);
    }

    /** Return true if t overrides mi */
    @Override
    public boolean hasMethod(ReferenceType t, MethodInstance mi) {
        assert_(t);
        assert_(mi);
        return t.hasMethodImpl(mi);
    }

    @Override
    public List<MethodInstance> overrides(MethodInstance mi) {
        return mi.overridesImpl();
    }

    @Override
    public List<MethodInstance> implemented(MethodInstance mi) {
        return mi.implementedImpl(mi.container());
    }

    @Override
    public boolean canOverride(MethodInstance mi, MethodInstance mj) {
        try {
            return mi.canOverrideImpl(mj, true);
        }
        catch (SemanticException e) {
            // this is the exception thrown by the canOverrideImpl check.
            // It should never be thrown if the quiet argument of
            // canOverrideImpl is true.
            throw new InternalCompilerError(e);
        }
    }

    @Override
    public void checkOverride(MethodInstance mi, MethodInstance mj)
            throws SemanticException {
        mi.canOverrideImpl(mj, false);
    }

    /**
     * Returns true iff <m1> is the same method as <m2>
     */
    @Override
    public boolean isSameMethod(MethodInstance m1, MethodInstance m2) {
        assert_(m1);
        assert_(m2);
        return m1.isSameMethodImpl(m2);
    }

    @Override
    public boolean methodCallValid(MethodInstance prototype, String name,
            List<? extends Type> argTypes) {
        assert_(prototype);
        assert_(argTypes);
        return prototype.methodCallValidImpl(name, argTypes);
    }

    @Override
    public boolean callValid(ProcedureInstance prototype,
            List<? extends Type> argTypes) {
        assert_(prototype);
        assert_(argTypes);
        return prototype.callValidImpl(argTypes);
    }

    ////
    // Functions which yield particular types.
    ////
    @Override
    public NullType Null() {
        return NULL_;
    }

    @Override
    public PrimitiveType Void() {
        return VOID_;
    }

    @Override
    public PrimitiveType Boolean() {
        return BOOLEAN_;
    }

    @Override
    public PrimitiveType Char() {
        return CHAR_;
    }

    @Override
    public PrimitiveType Byte() {
        return BYTE_;
    }

    @Override
    public PrimitiveType Short() {
        return SHORT_;
    }

    @Override
    public PrimitiveType Int() {
        return INT_;
    }

    @Override
    public PrimitiveType Long() {
        return LONG_;
    }

    @Override
    public PrimitiveType Float() {
        return FLOAT_;
    }

    @Override
    public PrimitiveType Double() {
        return DOUBLE_;
    }

    protected ClassType load(String name) {
        try {
            return (ClassType) typeForName(name);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Cannot find class \"" + name
                    + "\"; " + e.getMessage(), e);
        }
    }

    @Override
    public Named forName(String name) throws SemanticException {
        return forName(systemResolver, name);
    }

    protected Named forName(Resolver resolver, String name)
            throws SemanticException {
        try {
            return resolver.find(name);
        }
        catch (SemanticException e) {
            if (!StringUtil.isNameShort(name)) {
                String containerName = StringUtil.getPackageComponent(name);
                String shortName = StringUtil.getShortNameComponent(name);

                try {
                    Named container = forName(resolver, containerName);
                    if (container instanceof ClassType) {
                        return classContextResolver((ClassType) container).find(shortName);
                    }
                }
                catch (SemanticException e2) {
                }
            }

            // throw the original exception
            throw e;
        }
    }

    @Override
    public Type typeForName(String name) throws SemanticException {
        return (Type) forName(name);
    }

    protected ClassType OBJECT_;
    protected ClassType CLASS_;
    protected ClassType STRING_;
    protected ClassType THROWABLE_;

    @Override
    public ClassType Object() {
        if (OBJECT_ != null) return OBJECT_;
        return OBJECT_ = load("java.lang.Object");
    }

    @Override
    public ClassType Class() {
        if (CLASS_ != null) return CLASS_;
        return CLASS_ = load("java.lang.Class");
    }

    @Override
    public ClassType String() {
        if (STRING_ != null) return STRING_;
        return STRING_ = load("java.lang.String");
    }

    @Override
    public ClassType Throwable() {
        if (THROWABLE_ != null) return THROWABLE_;
        return THROWABLE_ = load("java.lang.Throwable");
    }

    @Override
    public ClassType Error() {
        return load("java.lang.Error");
    }

    @Override
    public ClassType Exception() {
        return load("java.lang.Exception");
    }

    @Override
    public ClassType RuntimeException() {
        return load("java.lang.RuntimeException");
    }

    @Override
    public ClassType Cloneable() {
        return load("java.lang.Cloneable");
    }

    @Override
    public ClassType Serializable() {
        return load("java.io.Serializable");
    }

    @Override
    public ClassType NullPointerException() {
        return load("java.lang.NullPointerException");
    }

    @Override
    public ClassType ClassCastException() {
        return load("java.lang.ClassCastException");
    }

    @Override
    public ClassType OutOfBoundsException() {
        return load("java.lang.ArrayIndexOutOfBoundsException");
    }

    @Override
    public ClassType ArrayStoreException() {
        return load("java.lang.ArrayStoreException");
    }

    @Override
    public ClassType ArithmeticException() {
        return load("java.lang.ArithmeticException");
    }

    protected NullType createNull() {
        return new NullType_c(this);
    }

    protected PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new PrimitiveType_c(this, kind);
    }

    protected final NullType NULL_ = createNull();
    protected final PrimitiveType VOID_ = createPrimitive(PrimitiveType.VOID);
    protected final PrimitiveType BOOLEAN_ =
            createPrimitive(PrimitiveType.BOOLEAN);
    protected final PrimitiveType CHAR_ = createPrimitive(PrimitiveType.CHAR);
    protected final PrimitiveType BYTE_ = createPrimitive(PrimitiveType.BYTE);
    protected final PrimitiveType SHORT_ = createPrimitive(PrimitiveType.SHORT);
    protected final PrimitiveType INT_ = createPrimitive(PrimitiveType.INT);
    protected final PrimitiveType LONG_ = createPrimitive(PrimitiveType.LONG);
    protected final PrimitiveType FLOAT_ = createPrimitive(PrimitiveType.FLOAT);
    protected final PrimitiveType DOUBLE_ =
            createPrimitive(PrimitiveType.DOUBLE);

    @Override
    public Object placeHolder(TypeObject o) {
        return placeHolder(o, Collections.<TypeObject> emptySet());
    }

    @Override
    public Object placeHolder(TypeObject o, Set<? extends TypeObject> roots) {
        assert_(o);

        if (o instanceof ParsedClassType) {
            ParsedClassType ct = (ParsedClassType) o;

            // This should never happen: anonymous and local types cannot
            // appear in signatures.
            if (ct.isLocal() || ct.isAnonymous()) {
                throw new InternalCompilerError("Cannot serialize " + o + ".");
            }

            // Use the transformed name so that member classes will
            // be sought in the correct class file.
            String name = getTransformedClassName(ct);
            return new PlaceHolder_c(name);
        }

        return o;
    }

    protected UnknownType unknownType = new UnknownType_c(this);
    protected UnknownPackage unknownPackage = new UnknownPackage_c(this);
    protected UnknownQualifier unknownQualifier = new UnknownQualifier_c(this);

    @Override
    public UnknownType unknownType(Position pos) {
        return unknownType;
    }

    @Override
    public UnknownPackage unknownPackage(Position pos) {
        return unknownPackage;
    }

    @Override
    public UnknownQualifier unknownQualifier(Position pos) {
        return unknownQualifier;
    }

    @Override
    public Package packageForName(Package prefix, String name)
            throws SemanticException {
        return createPackage(prefix, name);
    }

    @Override
    public Package packageForName(String name) throws SemanticException {
        if (name == null || name.equals("")) {
            return null;
        }

        String s = StringUtil.getShortNameComponent(name);
        String p = StringUtil.getPackageComponent(name);

        return packageForName(packageForName(p), s);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public Package createPackage(Package prefix, String name) {
        assert_(prefix);
        return new Package_c(this, prefix, name);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public Package createPackage(String name) {
        if (name == null || name.equals("")) {
            return null;
        }

        String s = StringUtil.getShortNameComponent(name);
        String p = StringUtil.getPackageComponent(name);

        return createPackage(createPackage(p), s);
    }

    /**
     * Returns a type identical to <type>, but with <dims> more array
     * dimensions.
     */
    @Override
    public ArrayType arrayOf(Type type) {
        assert_(type);
        return arrayOf(type.position(), type);
    }

    @Override
    public ArrayType arrayOf(Position pos, Type type) {
        assert_(type);
        return arrayType(pos, type);
    }

    Map<Type, ArrayType> arrayTypeCache = new HashMap<Type, ArrayType>();

    /**
     * Factory method for ArrayTypes.
     */
    protected ArrayType createArrayType(Position pos, Type type) {
        return new ArrayType_c(this, pos, type);
    }

    protected ArrayType arrayType(Position pos, Type type) {
        ArrayType t = arrayTypeCache.get(type);
        if (t == null) {
            t = createArrayType(pos, type);
            arrayTypeCache.put(type, t);
        }
        return t;
    }

    @Override
    public ArrayType arrayOf(Type type, int dims) {
        return arrayOf(null, type, dims);
    }

    @Override
    public ArrayType arrayOf(Position pos, Type type, int dims) {
        if (dims > 1) {
            return arrayOf(pos, arrayOf(pos, type, dims - 1));
        }
        else if (dims == 1) {
            return arrayOf(pos, type);
        }
        else {
            throw new InternalCompilerError("Must call arrayOf(type, dims) with dims > 0");
        }
    }

    /**
     * Returns a canonical type corresponding to the Java Class object
     * theClass.  Does not require that <theClass> have a JavaClass
     * registered in this typeSystem.  Does not register the type in
     * this TypeSystem.  For use only by JavaClass implementations.
     **/
    public Type typeForClass(Class<?> clazz) throws SemanticException {
        return typeForClass(systemResolver, clazz);
    }

    protected Type typeForClass(Resolver resolver, Class<?> clazz)
            throws SemanticException {
        if (clazz == Void.TYPE) return VOID_;
        if (clazz == Boolean.TYPE) return BOOLEAN_;
        if (clazz == Byte.TYPE) return BYTE_;
        if (clazz == Character.TYPE) return CHAR_;
        if (clazz == Short.TYPE) return SHORT_;
        if (clazz == Integer.TYPE) return INT_;
        if (clazz == Long.TYPE) return LONG_;
        if (clazz == Float.TYPE) return FLOAT_;
        if (clazz == Double.TYPE) return DOUBLE_;

        if (clazz.isArray()) {
            return arrayOf(typeForClass(clazz.getComponentType()));
        }

        return (Type) resolver.find(clazz.getName());
    }

    /**
     * Return the set of objects that should be serialized into the
     * type information for the given TypeObject.
     * Usually only the object itself should get encoded, and references
     * to other classes should just have their name written out.
     * If it makes sense for additional types to be fully encoded,
     * (i.e., they're necessary to correctly reconstruct the given clazz,
     * and the usual class resolvers can't otherwise find them) they
     * should be returned in the set in addition to clazz.
     */
    @Override
    public Set<TypeObject> getTypeEncoderRootSet(TypeObject t) {
        return Collections.singleton(t);
    }

    /**
     * Get the transformed class name of a class.
     * This utility method returns the "mangled" name of the given class,
     * whereby all periods ('.') following the toplevel class name
     * are replaced with dollar signs ('$'). If any of the containing
     * classes is not a member class or a top level class, then null is
     * returned.
     */
    @Override
    public String getTransformedClassName(ClassType ct) {
        StringBuffer sb = new StringBuffer(ct.fullName().length());
        if (!ct.isMember() && !ct.isTopLevel()) {
            return null;
        }
        while (ct.isMember()) {
            sb.insert(0, ct.name());
            sb.insert(0, '$');
            ct = ct.outer();
            if (!ct.isMember() && !ct.isTopLevel()) {
                return null;
            }
        }

        sb.insert(0, ct.fullName());
        return sb.toString();
    }

    @Override
    public String translatePackage(Resolver c, Package p) {
        return p.translate(c);
    }

    @Override
    public String translateArray(Resolver c, ArrayType t) {
        return t.translate(c);
    }

    @Override
    public String translateClass(Resolver c, ClassType t) {
        return t.translate(c);
    }

    @Override
    public String translatePrimitive(Resolver c, PrimitiveType t) {
        return t.translate(c);
    }

    @Override
    public PrimitiveType primitiveForName(String name) throws SemanticException {

        if (name.equals("void")) return Void();
        if (name.equals("boolean")) return Boolean();
        if (name.equals("char")) return Char();
        if (name.equals("byte")) return Byte();
        if (name.equals("short")) return Short();
        if (name.equals("int")) return Int();
        if (name.equals("long")) return Long();

        if (name.equals("float")) return Float();
        if (name.equals("double")) return Double();

        throw new SemanticException("Unrecognized primitive type \"" + name
                + "\".");
    }

    @Override
    public LazyClassInitializer defaultClassInitializer() {
        return new SchedulerClassInitializer(this);
    }

    /**
     * The lazy class initializer for deserialized classes.
     */
    @Override
    public LazyClassInitializer deserializedClassInitializer() {
        return new DeserializedClassInitializer(this);
    }

    @Override
    public final ParsedClassType createClassType() {
        return createClassType(defaultClassInitializer(), null);
    }

    @Override
    public final ParsedClassType createClassType(Source fromSource) {
        return createClassType(defaultClassInitializer(), fromSource);
    }

    @Override
    public final ParsedClassType createClassType(LazyClassInitializer init) {
        return createClassType(init, null);
    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init,
            Source fromSource) {
        return new ParsedClassType_c(this, init, fromSource);
    }

    @Override
    public List<String> defaultPackageImports() {
        List<String> l = new ArrayList<String>(1);
        l.add("java.lang");
        return l;
    }

    @Override
    public PrimitiveType promote(Type t1, Type t2) throws SemanticException {
        if (!t1.isNumeric()) {
            throw new SemanticException("Cannot promote non-numeric type " + t1);
        }

        if (!t2.isNumeric()) {
            throw new SemanticException("Cannot promote non-numeric type " + t2);
        }

        return promoteNumeric(t1.toPrimitive(), t2.toPrimitive());
    }

    protected PrimitiveType promoteNumeric(PrimitiveType t1, PrimitiveType t2) {
        if (t1.isDouble() || t2.isDouble()) {
            return Double();
        }

        if (t1.isFloat() || t2.isFloat()) {
            return Float();
        }

        if (t1.isLong() || t2.isLong()) {
            return Long();
        }

        return Int();
    }

    @Override
    public PrimitiveType promote(Type t) throws SemanticException {
        if (!t.isNumeric()) {
            throw new SemanticException("Cannot promote non-numeric type " + t);
        }

        return promoteNumeric(t.toPrimitive());
    }

    protected PrimitiveType promoteNumeric(PrimitiveType t) {
        if (t.isByte() || t.isShort() || t.isChar()) {
            return Int();
        }

        return t.toPrimitive();
    }

    /** All possible <i>access</i> flags. */
    @Override
    public Flags legalAccessFlags() {
        return Public().Protected().Private();
    }

    protected final Flags ACCESS_FLAGS = legalAccessFlags();

    /** All flags allowed for a local variable. */
    @Override
    public Flags legalLocalFlags() {
        return Final();
    }

    protected final Flags LOCAL_FLAGS = legalLocalFlags();

    /** All flags allowed for a field. */
    @Override
    public Flags legalFieldFlags() {
        return legalAccessFlags().Static().Final().Transient().Volatile();
    }

    protected final Flags FIELD_FLAGS = legalFieldFlags();

    /** All flags allowed for a constructor. */
    @Override
    public Flags legalConstructorFlags() {
        return legalAccessFlags().Synchronized().Native();
    }

    protected final Flags CONSTRUCTOR_FLAGS = legalConstructorFlags();

    /** All flags allowed for an initializer block. */
    @Override
    public Flags legalInitializerFlags() {
        return Static();
    }

    protected final Flags INITIALIZER_FLAGS = legalInitializerFlags();

    /** All flags allowed for a method. */
    @Override
    public Flags legalMethodFlags() {
        return legalAccessFlags().Abstract()
                                 .Static()
                                 .Final()
                                 .Native()
                                 .Synchronized()
                                 .StrictFP();
    }

    protected final Flags METHOD_FLAGS = legalMethodFlags();

    @Override
    public Flags legalAbstractMethodFlags() {
        return legalAccessFlags().clear(Private()).Abstract();
    }

    protected final Flags ABSTRACT_METHOD_FLAGS = legalAbstractMethodFlags();

    /** All flags allowed for a top-level class. */
    @Override
    public Flags legalTopLevelClassFlags() {
        return legalAccessFlags().clear(Private())
                                 .Abstract()
                                 .Final()
                                 .StrictFP()
                                 .Interface();
    }

    protected final Flags TOP_LEVEL_CLASS_FLAGS = legalTopLevelClassFlags();

    /** All flags allowed for an interface. */
    @Override
    public Flags legalInterfaceFlags() {
        return legalAccessFlags().Abstract().Interface().Static();
    }

    protected final Flags INTERFACE_FLAGS = legalInterfaceFlags();

    /** All flags allowed for a member class. */
    @Override
    public Flags legalMemberClassFlags() {
        return legalAccessFlags().Static()
                                 .Abstract()
                                 .Final()
                                 .StrictFP()
                                 .Interface();
    }

    protected final Flags MEMBER_CLASS_FLAGS = legalMemberClassFlags();

    /** All flags allowed for a local class. */
    @Override
    public Flags legalLocalClassFlags() {
        return Abstract().Final().StrictFP().Interface();
    }

    protected final Flags LOCAL_CLASS_FLAGS = legalLocalClassFlags();

    @Override
    public void checkMethodFlags(Flags f) throws SemanticException {
        if (!f.clear(METHOD_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare method with flags "
                    + f.clear(METHOD_FLAGS) + ".");
        }

        if (f.isAbstract()
                && !f.clear(ABSTRACT_METHOD_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare abstract method with flags "
                    + f.clear(ABSTRACT_METHOD_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkLocalFlags(Flags f) throws SemanticException {
        if (!f.clear(LOCAL_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare local variable with flags "
                    + f.clear(LOCAL_FLAGS) + ".");
        }
    }

    @Override
    public void checkFieldFlags(Flags f) throws SemanticException {
        if (!f.clear(FIELD_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare field with flags "
                    + f.clear(FIELD_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkConstructorFlags(Flags f) throws SemanticException {
        if (!f.clear(CONSTRUCTOR_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare constructor with flags "
                    + f.clear(CONSTRUCTOR_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkInitializerFlags(Flags f) throws SemanticException {
        if (!f.clear(INITIALIZER_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare initializer with flags "
                    + f.clear(INITIALIZER_FLAGS) + ".");
        }
    }

    @Override
    public void checkTopLevelClassFlags(Flags f) throws SemanticException {
        if (!f.clear(TOP_LEVEL_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a top-level class with flag(s) "
                    + f.clear(TOP_LEVEL_CLASS_FLAGS) + ".");
        }

        if (f.isInterface() && !f.clear(INTERFACE_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare interface with flags "
                    + f.clear(INTERFACE_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkMemberClassFlags(Flags f) throws SemanticException {
        if (!f.clear(MEMBER_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a member class with flag(s) "
                    + f.clear(MEMBER_CLASS_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkLocalClassFlags(Flags f) throws SemanticException {
        if (f.isInterface()) {
            throw new SemanticException("Cannot declare a local interface.");
        }

        if (!f.clear(LOCAL_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a local class with flag(s) "
                    + f.clear(LOCAL_CLASS_FLAGS) + ".");
        }

        checkAccessFlags(f);
    }

    @Override
    public void checkAccessFlags(Flags f) throws SemanticException {
        int count = 0;
        if (f.isPublic()) count++;
        if (f.isProtected()) count++;
        if (f.isPrivate()) count++;

        if (count > 1) {
            throw new SemanticException("Invalid access flags: "
                    + f.retain(ACCESS_FLAGS) + ".");
        }
    }

    /**
     * Utility method to gather all the superclasses and interfaces of
     * <code>ct</code> that may contain abstract methods that must be
     * implemented by <code>ct</code>. The list returned also contains
     * <code>rt</code>.
     */
    protected List<ReferenceType> abstractSuperInterfaces(ReferenceType rt) {
        List<ReferenceType> superInterfaces = new LinkedList<ReferenceType>();
        superInterfaces.add(rt);

        @SuppressWarnings("unchecked")
        List<ClassType> interfaces = (List<ClassType>) rt.interfaces();
        for (ClassType interf : interfaces) {
            superInterfaces.addAll(abstractSuperInterfaces(interf));
        }

        if (rt.superType() != null) {
            ClassType c = rt.superType().toClass();
            if (c.flags().isAbstract()) {
                // the superclass is abstract, so it may contain methods
                // that must be implemented.
                superInterfaces.addAll(abstractSuperInterfaces(c));
            }
            else {
                // the superclass is not abstract, so it must implement
                // all abstract methods of any interfaces it implements, and
                // any superclasses it may have.
            }
        }
        return superInterfaces;
    }

    /**
     * Assert that <code>ct</code> implements all abstract methods required;
     * that is, if it is a concrete class, then it must implement all
     * interfaces and abstract methods that it or it's superclasses declare, and if 
     * it is an abstract class then any methods that it overrides are overridden 
     * correctly.
     */
    @Override
    public void checkClassConformance(ClassType ct) throws SemanticException {
        if (ct.flags().isAbstract()) {
            // don't need to check interfaces or abstract classes           
            return;
        }

        // build up a list of superclasses and interfaces that ct 
        // extends/implements that may contain abstract methods that 
        // ct must define.
        List<ReferenceType> superInterfaces = abstractSuperInterfaces(ct);

        // check each abstract method of the classes and interfaces in
        // superInterfaces
        for (ReferenceType rt : superInterfaces) {
            for (MethodInstance mi : rt.methods()) {
                if (!mi.flags().isAbstract()) {
                    // the method isn't abstract, so ct doesn't have to
                    // implement it.
                    continue;
                }

                MethodInstance mj = findImplementingMethod(ct, mi);
                if (mj == null) {
                    if (!ct.flags().isAbstract()) {
                        throw new SemanticException(ct.fullName()
                                + " should be "
                                + "declared abstract; it does not define "
                                + mi.signature() + ", which is declared in "
                                + rt.toClass().fullName(), ct.position());
                    }
                    else {
                        // no implementation, but that's ok, the class is abstract.
                    }
                }
                else if (!equals(ct, mj.container())
                        && !equals(ct, mi.container())) {
                    try {
                        // check that mj can override mi, which
                        // includes access protection checks.
                        checkOverride(mj, mi);
                    }
                    catch (SemanticException e) {
                        // change the position of the semantic
                        // exception to be the class that we
                        // are checking.
                        throw new SemanticException(e.getMessage(),
                                                    ct.position());
                    }
                }
                else {
                    // the method implementation mj or mi was
                    // declared in ct. So other checks will take
                    // care of access issues
                }
            }
        }
    }

    @Override
    public MethodInstance findImplementingMethod(ClassType ct, MethodInstance mi) {
        ReferenceType curr = ct;
        while (curr != null) {
            List<? extends MethodInstance> possible =
                    curr.methods(mi.name(), mi.formalTypes());
            for (MethodInstance mj : possible) {
                if (!mj.flags().isAbstract()
                        && ((isAccessible(mi, ct) && isAccessible(mj, ct)) || isAccessible(mi,
                                                                                           mj.container()
                                                                                             .toClass()))) {
                    // The method mj may be a suitable implementation of mi.
                    // mj is not abstract, and either mj's container 
                    // can access mi (thus mj can really override mi), or
                    // mi and mj are both accessible from ct (e.g.,
                    // mi is declared in an interface that ct implements,
                    // and mj is defined in a superclass of ct).
                    return mj;
                }
            }
            if (curr == mi.container()) {
                // we've reached the definition of the abstract 
                // method. We don't want to look higher in the 
                // hierarchy; this is not an optimization, but is 
                // required for correctness. 
                break;
            }

            curr =
                    curr.superType() == null ? null : curr.superType()
                                                          .toReference();
        }
        return null;
    }

    /**
     * Returns t, modified as necessary to make it a legal
     * static target.
     */
    @Override
    public Type staticTarget(Type t) {
        // Nothing needs done in standard Java.
        return t;
    }

    protected void initFlags() {
        flagsForName = new HashMap<String, Flags>();
        flagsForName.put("public", Flags.PUBLIC);
        flagsForName.put("private", Flags.PRIVATE);
        flagsForName.put("protected", Flags.PROTECTED);
        flagsForName.put("static", Flags.STATIC);
        flagsForName.put("final", Flags.FINAL);
        flagsForName.put("synchronized", Flags.SYNCHRONIZED);
        flagsForName.put("transient", Flags.TRANSIENT);
        flagsForName.put("native", Flags.NATIVE);
        flagsForName.put("interface", Flags.INTERFACE);
        flagsForName.put("abstract", Flags.ABSTRACT);
        flagsForName.put("volatile", Flags.VOLATILE);
        flagsForName.put("strictfp", Flags.STRICTFP);
    }

    @Override
    public Flags createNewFlag(String name, Flags after) {
        Flags f = Flags.createFlag(name, after);
        flagsForName.put(name, f);
        return f;
    }

    @Override
    public Flags NoFlags() {
        return Flags.NONE;
    }

    @Override
    public Flags Public() {
        return Flags.PUBLIC;
    }

    @Override
    public Flags Private() {
        return Flags.PRIVATE;
    }

    @Override
    public Flags Protected() {
        return Flags.PROTECTED;
    }

    @Override
    public Flags Static() {
        return Flags.STATIC;
    }

    @Override
    public Flags Final() {
        return Flags.FINAL;
    }

    @Override
    public Flags Synchronized() {
        return Flags.SYNCHRONIZED;
    }

    @Override
    public Flags Transient() {
        return Flags.TRANSIENT;
    }

    @Override
    public Flags Native() {
        return Flags.NATIVE;
    }

    @Override
    public Flags Interface() {
        return Flags.INTERFACE;
    }

    @Override
    public Flags Abstract() {
        return Flags.ABSTRACT;
    }

    @Override
    public Flags Volatile() {
        return Flags.VOLATILE;
    }

    @Override
    public Flags StrictFP() {
        return Flags.STRICTFP;
    }

    @Override
    public Flags flagsForBits(int bits) {
        Flags f = Flags.NONE;

        if ((bits & Modifier.PUBLIC) != 0) f = f.Public();
        if ((bits & Modifier.PRIVATE) != 0) f = f.Private();
        if ((bits & Modifier.PROTECTED) != 0) f = f.Protected();
        if ((bits & Modifier.STATIC) != 0) f = f.Static();
        if ((bits & Modifier.FINAL) != 0) f = f.Final();
        if ((bits & Modifier.SYNCHRONIZED) != 0) f = f.Synchronized();
        if ((bits & Modifier.TRANSIENT) != 0) f = f.Transient();
        if ((bits & Modifier.NATIVE) != 0) f = f.Native();
        if ((bits & Modifier.INTERFACE) != 0) f = f.Interface();
        if ((bits & Modifier.ABSTRACT) != 0) f = f.Abstract();
        if ((bits & Modifier.VOLATILE) != 0) f = f.Volatile();
        if ((bits & Modifier.STRICT) != 0) f = f.StrictFP();

        return f;
    }

    public Flags flagsForName(String name) {
        Flags f = flagsForName.get(name);
        if (f == null) {
            throw new InternalCompilerError("No flag named \"" + name + "\".");
        }
        return f;
    }

    @Override
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }

}
