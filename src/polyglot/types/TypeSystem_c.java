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

import polyglot.ast.JLang_c;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.util.SubtypeSet;

/**
 * The {@code TypeSystem} defines the types of the language and
 * how they are related.
 *
 * Overview:
 *    A {@code TypeSystem_c} is a universe of types, including all Java types.
 */
public class TypeSystem_c implements TypeSystem {
    protected SystemResolver systemResolver;
    protected TopLevelResolver loadedResolver;
    protected Map<String, Flags> flagsForName;
    protected ExtensionInfo extInfo;

    public TypeSystem_c() {
    }

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
        systemResolver = new SystemResolver(loadedResolver, extInfo);

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
        SystemResolver r = systemResolver;
        systemResolver = r.copy();
        return r;
    }

    @Override
    public void restoreSystemResolver(SystemResolver r) {
        if (r != systemResolver.previous()) {
            throw new InternalCompilerError("Inconsistent systemResolver.previous");
        }
        systemResolver = r;
    }

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
        return new Context_c(JLang_c.instance, this);
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

    @Override
    public boolean descendsFrom(Type child, Type ancestor) {
        assert_(child);
        assert_(ancestor);
        return child.descendsFromImpl(ancestor);
    }

    @Override
    public boolean isCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isCastValidImpl(toType);
    }

    @Override
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        assert_(fromType);
        assert_(toType);
        return fromType.isImplicitCastValidImpl(toType);
    }

    @Override
    public boolean equals(TypeObject type1, TypeObject type2) {
        assert_(type1);
        assert_(type2);
        if (type1 == type2) return true;
        if (type1 == null || type2 == null) return false;
        return type1.equalsImpl(type2);
    }

    @Override
    public boolean typeEquals(Type type1, Type type2) {
        assert_(type1);
        assert_(type2);
        return type1.typeEqualsImpl(type2);
    }

    @Override
    public boolean packageEquals(Package type1, Package type2) {
        assert_(type1);
        assert_(type2);
        return type1.packageEqualsImpl(type2);
    }

    @Override
    public boolean numericConversionValid(Type t, Object value) {
        assert_(t);
        return t.numericConversionValidImpl(value);
    }

    @Deprecated
    @Override
    public boolean numericConversionValid(Type t, long value) {
        return numericConversionValid(t, new Long(value));
    }

    ////
    // Functions for one-type checking and resolution.
    ////

    @Override
    public boolean isCanonical(Type type) {
        assert_(type);
        return type.isCanonical();
    }

    @Override
    public boolean isAccessible(MemberInstance mi, Context context) {
        return isAccessible(mi, context.currentClass());
    }

    @Override
    public boolean isAccessible(MemberInstance mi, ClassType contextClass) {
        return isAccessible(mi, contextClass, false);
    }

    @Override
    public boolean isAccessible(MemberInstance mi, ClassType contextClass,
            boolean fromClient) {
        assert_(mi);

        ReferenceType target = mi.container();
        return isAccessible(mi, target, contextClass, fromClient);
    }

    @Override
    public boolean isAccessible(MemberInstance mi, ReferenceType container,
            ClassType contextClass) {
        return isAccessible(mi, container, contextClass, true);
    }

    @Override
    public boolean isAccessible(MemberInstance mi, ReferenceType container,
            ReferenceType contextType, boolean fromClient) {
        Flags flags = mi.flags();

        // See JLS 2nd Ed. | 6.6.1.

        // A member (class, interface, field, or method) of a reference (class,
        // interface, or array) type or a constructor of a class type is
        // accessible only if the type is accessible and the member or
        // constructor is declared to permit access:
        ReferenceType target = mi.container();
        if (!target.isClass()) {
            // public members of non-classes are accessible;
            // non-public members of non-classes are inaccessible
            return flags.isPublic();
        }
        ClassType targetClass = (ClassType) target.toClass().declaration();
        if (container.isClass() && contextType.isClass()
                && !classAccessible(container.toClass(), contextType.toClass()))
            return false;

        // If the member or constructor is declared public, then access is
        // permitted.
        if (flags.isPublic()) return true;

        // Otherwise, if the member or constructor is declared private, then
        // access is permitted iff it occurs within the body of the top level
        // class that encloses the declaration of the member.
        if (flags.isPrivate()) {
            if (contextType.isClass()) {
                ClassType ct = contextType.toClass();
                while (!ct.isTopLevel())
                    ct = ct.outer();
                return typeEquals(targetClass, ct)
                        || isEnclosed(targetClass, ct);
            }
            else return typeEquals(targetClass, contextType);
        }

        boolean isAccessibleFromPackage = contextType.isClass()
                && accessibleFromPackage(flags,
                                         targetClass.package_(),
                                         contextType.toClass().package_());

        // Otherwise, if the member or constructor is declared protected, then
        // access is permitted only when one of the following is true:
        // - Access to the member or constructor occurs from within the package
        //   containing the class in which the protected member or constructor
        //   is declared.  (Deferred to default case.)
        // - Access is correct as described in | 6.6.2.
        if (flags.isProtected()) {
            if (isAccessibleFromPackage) return true;
            if (mi instanceof ConstructorInstance) {
                // See JLS 2nd Ed. | 6.6.2.2

                // Let C be the class in which a protected constructor is declared
                // and let S be the innermost class in whose declaration the use of
                // the protected constructor occurs.  Then:
                // - If the access is one of super(...), E.super(...),
                //   new C(...){...}, or E.new C(...){...}, then access is permitted.
                // - Otherwise, if the access is new C(...) or E.new C(...), then
                //   the access is not permitted.
                return !fromClient;
            }
            else {
                // See JLS 2nd Ed. | 6.6.2.1

                // Let C be the class in which a protected member is declared.
                // Access is permitted only within the body of a subclass S of C.
                ReferenceType rt = contextType;
                if (contextType.isClass()) {
                    ClassType ct = contextType.toClass();
                    while (!isSubtype(ct, targetClass) && !ct.isTopLevel())
                        ct = ct.outer();
                    rt = ct;
                }
                if (isSubtype(rt, targetClass)) {
                    // Class and static members are accessible.
                    if (mi instanceof ClassType || flags.isStatic())
                        return true;
                    // In addition, for expressions of the form E.Id or E.Id(...),
                    // access is permitted iff the type of E is S or a subclass of S.
                    return !fromClient || isSubtype(container, rt);
                }
            }
        }

        // Otherwise, we say there is default access, which is permitted only
        // when the access occurs from within the package in which the type is
        // declared.
        return isAccessibleFromPackage;
    }

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

    @Override
    public boolean classAccessible(ClassType targetClass,
            ClassType contextClass) {
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

    @Override
    public boolean classAccessibleFromPackage(ClassType targetClass,
            Package pkg) {
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
     * top-level class with access flags {@code flags}
     * in package {@code pkg1} is accessible from package
     * {@code pkg2}.
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

    @Override
    public boolean canCoerceToString(Type t, Context c) {
        // every Object can be coerced to a string, as can any primitive,
        // except void.
        return !t.isVoid();
    }

    @Override
    public boolean isThrowable(Type type) {
        assert_(type);
        return type.isThrowable();
    }

    @Override
    public boolean isUncheckedException(Type type) {
        assert_(type);
        return type.isUncheckedException();
    }

    @Override
    public Collection<Type> uncheckedExceptions() {
        List<Type> l = new ArrayList<>(2);
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

    @Deprecated
    @Override
    public FieldInstance findField(ReferenceType container, String name,
            ClassType currClass) throws SemanticException {
        return this.findField(container, name, currClass, true);
    }

    @Override
    public FieldInstance findField(ReferenceType container, String name,
            ClassType currClass, boolean fromClient) throws SemanticException {
        Collection<FieldInstance> fields = findFields(container, name);

        FieldInstance result = null;
        SemanticException error = null;
        for (FieldInstance fi : fields) {
            ReferenceType fc = fi.container();
            if (currClass != null) {
                Type fiType = fi.type();
                if (fiType.isArray()) fiType = fiType.toArray().ultimateBase();
                if (fiType.isClass()
                        && !classAccessible(fiType.toClass(), currClass)) {
                    if (error == null)
                        error = new SemanticException("The field \"" + fi.name()
                                + "\" has type " + fi.type()
                                + ", which is inaccessible from class "
                                + currClass);
                }
                else if (!isMember(fi, container.toReference())) {
                    if (error == null) error =
                            new NoMemberException(NoMemberException.FIELD,
                                                  "The " + fi
                                                          + " is not visible in class "
                                                          + container + ".");
                }
                else if (!isAccessible(fi, container, currClass, fromClient)) {
                    if (error == null) error =
                            new SemanticException("Cannot access " + fi + ".");
                }
                else if (result != null) {
                    throw new SemanticException("Field \"" + name
                            + "\" is ambiguous; it is defined in both "
                            + result.container() + " and " + fc + ".");
                }
                else result = fi;
            }
            else {
                if (!fi.flags().isPublic()) {
                    if (error == null) error =
                            new SemanticException("Cannot access " + fi + ".");
                }
                else if (result != null) {
                    throw new SemanticException("Field \"" + name
                            + "\" is ambiguous; it is defined in both "
                            + result.container() + " and " + fc + ".");
                }
                else result = fi;
            }
        }
        if (result == null) {
            throw error == null
                    ? new NoMemberException(NoMemberException.FIELD,
                                            "Field \"" + name
                                                    + "\" not found in type \""
                                                    + container + "\".")
                    : error;
        }
        return result;
    }

    @Override
    public boolean isMember(MemberInstance mi, ReferenceType type) {
        return typeEquals(mi.container(), type) || isInherited(mi, type);
    }

    @Override
    public boolean isInherited(MemberInstance mi, ReferenceType type) {
        if (mi.flags().isPrivate()) {
            // private members are never inherited.
            return false;
        }
        if (mi instanceof MethodInstance) {
            MethodInstance mi_ = (MethodInstance) mi;
            if (!type.methods(mi_.name(), mi_.formalTypes()).isEmpty())
                return false;
        }
        ReferenceType container = mi.container();
        boolean isInheritedInSuperType = false;
        Type superType = type.superType();
        if (superType != null) {
            if (isMember(mi, superType.toReference())) {
                isInheritedInSuperType = true;
            }
        }
        if (!isInheritedInSuperType) {
            for (ReferenceType rt : type.interfaces()) {
                if (isMember(mi, rt.toReference())) {
                    isInheritedInSuperType = true;
                    break;
                }
            }
        }
        if (isInheritedInSuperType) {
            if (mi.flags().isProtected() || mi.flags().isPublic()) return true;
            Package typePackage = null;
            if (type.isClass()) typePackage = type.toClass().package_();
            Package containerPackage = null;
            if (container.isClass())
                containerPackage = container.toClass().package_();
            // whether type and the container are in the same package.
            return typePackage == null
                    ? typePackage == containerPackage
                    : packageEquals(typePackage, containerPackage);
        }
        return false;
    }

    @Deprecated
    @Override
    public FieldInstance findField(ReferenceType container, String name)
            throws SemanticException {
        return findField(container, name, container.toClass());
    }

    /**
     * Returns a set of fields named {@code name} defined
     * in type {@code container} or a supertype.  The list
     * returned may be empty.
     */
    protected Set<FieldInstance> findFields(ReferenceType container,
            String name) {
        assert_(container);

        if (container == null) {
            throw new InternalCompilerError("Cannot access field \"" + name
                    + "\" within a null container type.");
        }

        FieldInstance fi = container.fieldNamed(name);

        if (fi != null) {
            return Collections.singleton(fi);
        }

        Set<FieldInstance> fields = new HashSet<>();

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
    public ClassType findMemberClass(ClassType container, String name,
            Context c) throws SemanticException {
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

        Set<Type> visitedTypes = new HashSet<>();

        LinkedList<Type> typeQueue = new LinkedList<>();
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
                if (isMember(mi, container.toReference())
                        && isAccessible(mi, container, currClass)) {
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

    @Deprecated
    @Override
    public MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass)
                    throws SemanticException {
        return findMethod(container, name, argTypes, currClass, true);
    }

    @Override
    public MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<? extends MethodInstance> acceptable =
                findAcceptableMethods(container,
                                      name,
                                      argTypes,
                                      currClass,
                                      fromClient);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.METHOD,
                                        "No valid method call found for " + name
                                                + "(" + listToString(argTypes)
                                                + ")" + " in " + container
                                                + ".");
        }
        Collection<? extends MethodInstance> maximal =
                findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<? extends MethodInstance> i =
                    maximal.iterator(); i.hasNext();) {
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
                    + " is ambiguous, multiple methods match: "
                    + sb.toString());
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

    @Deprecated
    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass)
                    throws SemanticException {
        return findConstructor(container, argTypes, currClass, true);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        List<? extends ConstructorInstance> acceptable =
                findAcceptableConstructors(container,
                                           argTypes,
                                           currClass,
                                           fromClient);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                                        "No valid constructor found for "
                                                + container + "("
                                                + listToString(argTypes)
                                                + ").");
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
            ReferenceType container, List<Type> argTypes, ClassType currClass) {
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
            List<Instance> acceptable) {
        MostSpecificComparator<Instance> msc = mostSpecificComparator();
        // now, use JLS 15.12.2.2
        // First sort from most- to least-specific.
        acceptable = new ArrayList<>(acceptable); // make into array list to sort
        Collections.sort(acceptable, msc);

        List<Instance> maximal = new ArrayList<>(acceptable.size());

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
            // First, check that all the maximally specific methods have the
            // same signature.
            // Use the declarations to compare formals.
            Iterator<Instance> j = maximal.iterator();
            first = j.next();
            // XXX
//            ProcedureInstance firstDecl = first;
//            if (first instanceof Declaration) {
//                firstDecl =
//                        (ProcedureInstance) ((Declaration) first).declaration();
//            }
            while (j.hasNext()) {
                Instance p = j.next();

//                ProcedureInstance pDecl = p;
//                if (p instanceof Declaration) {
//                    pDecl = (ProcedureInstance) ((Declaration) p).declaration();
//                }

                if (!first.hasFormals(p.formalTypes())) {
//                if (!firstDecl.hasFormals(pDecl.formalTypes())) {
                    // not all signatures match; must be ambiguous
                    return maximal;
                }
            }

            // If exactly one method is not abstract, it is the most specific.
            List<Instance> notAbstract = new ArrayList<>(maximal.size());
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
                j = maximal.iterator();
                first = j.next();
                SubtypeSet throwsSubsetType =
                        new SubtypeSet(this, first.throwTypes());
                while (j.hasNext()) {
                    Instance p = j.next();
                    throwsSubsetType.retainAll(p.throwTypes());
                }

                // all signatures match, just take the first
                // However, the most specific method is considered to throw a
                // checked exception iff that exception is declared in the
                // throws clauses of each of the maximally specific methods.
                List<Type> throwTypes = new LinkedList<>(throwsSubsetType);
                if (!first.throwTypes().equals(throwTypes)) {
                    first = Copy.Util.copy(first);
                    first.setThrowTypes(throwTypes);
                }
                maximal = Collections.singletonList(first);
            }
        }

        return maximal;
    }

    @Override
    public <T extends ProcedureInstance> MostSpecificComparator<T> mostSpecificComparator() {
        return new MostSpecificComparator<>();
    }

    /**
     * Class to handle the comparisons; dispatches to moreSpecific method.
     */
    protected static class MostSpecificComparator<T extends ProcedureInstance>
            implements Comparator<T> {
        @Override
        public int compare(T p1, T p2) {
            // Implement the "strictly more specific" relation
            // The JLS 2nd ed used only the "more specific" relation,
            // but that is likely a shortcoming in the spec, as then the
            // relation isn't anti-symmetric.
            // The JLS 3rd edition defines the "strictly more specific"
            // relation, which we will use here.
            boolean p1beatsp2 = p1.moreSpecific(p2);
            boolean p2beatsp1 = p2.moreSpecific(p1);
            if (p1beatsp2 && !p2beatsp1) return -1;
            if (p2beatsp1 && !p1beatsp2) return 1;
            return 0;
        }
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2.1
     */
    protected List<? extends MethodInstance> findAcceptableMethods(
            ReferenceType container, String name, List<? extends Type> argTypes,
            ClassType currClass, boolean fromClient) throws SemanticException {

        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        // The list of acceptable methods. These methods are accessible from
        // currClass, the method call is valid, and they are not overridden
        // by an unacceptable method (which can occur with protected methods
        // only). They include methods that are inherited from super classes
        // and interfaces but not overridden.
        List<MethodInstance> acceptable = new ArrayList<>();

        // A list of unacceptable methods, where the method call is valid, but
        // the method is not accessible. This list is needed to make sure that
        // the acceptable methods are not overridden by an unacceptable method.
        List<MethodInstance> unacceptable = new ArrayList<>();

        // A set of all the methods that methods in acceptable override.
        // Used to make sure we don't mistakenly add in overridden methods
        // (since overridden methods aren't inherited from superclasses).
        Set<MethodInstance> overridden = new HashSet<>();

        Set<Type> visitedTypes = new HashSet<>();

        LinkedList<Type> typeQueue = new LinkedList<>();
        typeQueue.addLast(container);

        while (!typeQueue.isEmpty()) {
            Type type = typeQueue.removeFirst();

            if (visitedTypes.contains(type)) {
                continue;
            }

            visitedTypes.add(type);

            if (Report.should_report(Report.types, 2))
                Report.report(2,
                              "Searching type " + type + " for method " + name
                                      + "(" + listToString(argTypes) + ")");

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
                    if (isMember(mi, container.toReference())
                            && isAccessible(mi,
                                            container,
                                            currClass,
                                            fromClient)) {
                        if (Report.should_report(Report.types, 3)) {
                            Report.report(3,
                                          "->acceptable: " + mi + " in "
                                                  + mi.container());
                        }

                        // Check that mi isn't overridden by something
                        // already accepted
                        if (!overridden.contains(mi)) {
                            // mi isn't overridden by something already in acceptable
                            // so add mi to acceptable, and add all the methods it
                            // overrides to the set overridden.
                            List<? extends MethodInstance> implemented =
                                    mi.implemented();
                            overridden.addAll(implemented);
                            acceptable.removeAll(implemented); // remove everything that mi overrides
                            acceptable.add(mi);
                        }
                    }
                    else {
                        // method call is valid, but the method is
                        // unacceptable.
                        unacceptable.add(mi);
                        if (error == null) {
                            error = new NoMemberException(NoMemberException.METHOD,
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
                        error = new NoMemberException(NoMemberException.METHOD,
                                                      "Method " + mi.signature()
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
            error = new NoMemberException(NoMemberException.METHOD,
                                          "No valid method call found for "
                                                  + name + "("
                                                  + listToString(argTypes) + ")"
                                                  + " in " + container + ".");
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
            ClassType currClass, boolean fromClient) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        List<ConstructorInstance> acceptable = new ArrayList<>();

        if (Report.should_report(Report.types, 2))
            Report.report(2,
                          "Searching type " + container + " for constructor "
                                  + container + "(" + listToString(argTypes)
                                  + ")");

        for (ConstructorInstance ci : container.constructors()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "Trying " + ci);

            if (callValid(ci, argTypes)) {
                if (isAccessible(ci, currClass, fromClient)) {
                    if (Report.should_report(Report.types, 3))
                        Report.report(3, "->acceptable: " + ci);
                    acceptable.add(ci);
                }
                else {
                    if (error == null) {
                        error = new NoMemberException(NoMemberException.CONSTRUCTOR,
                                                      "Constructor "
                                                              + ci.signature()
                                                              + " is inaccessible.");
                    }
                }
            }
            else {
                if (error == null) {
                    error = new NoMemberException(NoMemberException.CONSTRUCTOR,
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
                error = new NoMemberException(NoMemberException.CONSTRUCTOR,
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
     * Returns true iff {@code m1} is <i>more specific</i> than {@code m2},
     * where <i>more specific</i> is defined as JLS 15.11.2.2
     */
    @Override
    public boolean moreSpecific(ProcedureInstance p1, ProcedureInstance p2) {
        return p1.moreSpecificImpl(p2);
    }

    @Override
    public Type superType(ReferenceType type) {
        assert_(type);
        return type.superType();
    }

    @Override
    public List<? extends Type> interfaces(ReferenceType type) {
        assert_(type);
        return type.interfaces();
    }

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

            if (type1.isChar() && type2.isByte()
                    || type1.isByte() && type2.isChar()) {
                return Int();
            }

            if (type1.isChar() && type2.isShort()
                    || type1.isShort() && type2.isChar()) {
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

    @Override
    public boolean throwsSubset(ProcedureInstance p1, ProcedureInstance p2) {
        assert_(p1);
        assert_(p2);
        return p1.throwsSubsetImpl(p2);
    }

    @Override
    public boolean hasFormals(ProcedureInstance pi,
            List<? extends Type> formalTypes) {
        assert_(pi);
        assert_(formalTypes);
        return pi.hasFormalsImpl(formalTypes);
    }

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

    @Override
    public boolean isSameMethod(MethodInstance m1, MethodInstance m2) {
        assert_(m1);
        assert_(m2);
        return m1.isSameMethodImpl(m2);
    }

    @Override
    public boolean isSameConstructor(ConstructorInstance c1,
            ConstructorInstance c2) {
        assert_(c1);
        assert_(c2);
        return c1.isSameConstructorImpl(c2);
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
    public ClassType AssertionError() {
        return load("java.lang.AssertionError");
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

    Map<Type, ArrayType> arrayTypeCache = new HashMap<>();

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

    @Override
    public Set<TypeObject> getTypeEncoderRootSet(TypeObject t) {
        return Collections.singleton(t);
    }

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
    public PrimitiveType primitiveForName(String name)
            throws SemanticException {

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
        List<String> l = new ArrayList<>(1);
        l.add("java.lang");
        return l;
    }

    @Override
    public PrimitiveType promote(Type t1, Type t2) throws SemanticException {
        if (!t1.isNumeric()) {
            throw new SemanticException("Cannot promote non-numeric type "
                    + t1);
        }

        if (!t2.isNumeric()) {
            throw new SemanticException("Cannot promote non-numeric type "
                    + t2);
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

    @Override
    public Flags legalAccessFlags() {
        return Public().Protected().Private();
    }

    protected final Flags ACCESS_FLAGS = legalAccessFlags();

    @Override
    public Flags legalLocalFlags() {
        return Final();
    }

    protected final Flags LOCAL_FLAGS = legalLocalFlags();

    @Override
    public Flags legalFieldFlags() {
        return legalAccessFlags().Static().Final().Transient().Volatile();
    }

    protected final Flags FIELD_FLAGS = legalFieldFlags();

    @Override
    public Flags legalConstructorFlags() {
        // A constructor cannot be abstract, static, final, native, strictfp,
        // or synchronized.  See JLS 2nd Ed. | 8.8.3.
        return legalAccessFlags();
    }

    protected final Flags CONSTRUCTOR_FLAGS = legalConstructorFlags();

    @Override
    public Flags legalInitializerFlags() {
        return Static();
    }

    protected final Flags INITIALIZER_FLAGS = legalInitializerFlags();

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

    @Override
    public Flags legalTopLevelClassFlags() {
        return Public().Abstract().Final().StrictFP().Interface();
    }

    protected final Flags TOP_LEVEL_CLASS_FLAGS = legalTopLevelClassFlags();

    @Override
    public Flags legalMemberClassFlags() {
        return legalAccessFlags().Static()
                                 .Abstract()
                                 .Final()
                                 .StrictFP()
                                 .Interface();
    }

    protected final Flags MEMBER_CLASS_FLAGS = legalMemberClassFlags();

    @Override
    public Flags legalLocalClassFlags() {
        return Abstract().Final().StrictFP().Interface();
    }

    protected final Flags LOCAL_CLASS_FLAGS = legalLocalClassFlags();

    @Override
    public Flags legalInterfaceFieldFlags() {
        return Public().Static().Final();
    }

    protected final Flags INTERFACE_FIELD_FLAGS = legalInterfaceFieldFlags();

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

        // A compile-time error occurs if a method declaration that contains the
        // keyword native also contains strictfp.  See JLS 2nd Ed. | 8.4.3.
        if (f.isNative() && f.isStrictFP()) {
            throw new SemanticException("Method cannot be both native and strictfp.");
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

        // A compile-time error occurs if a final variable is also declared
        // volatile.
        // See JLS 2nd Ed. | 8.3.1.4.
        if (f.isFinal() && f.isVolatile()) {
            throw new SemanticException("Field cannot be both final and volatile.");
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
        Flags remainingFlags = f.clear(TOP_LEVEL_CLASS_FLAGS);
        if (!remainingFlags.equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a top-level "
                    + (f.isInterface() ? "interface" : "class")
                    + " with flag(s) " + remainingFlags + ".");
        }

        checkClassFlagsConflict(f);
        checkAccessFlags(f);
    }

    @Override
    public void checkMemberClassFlags(Flags f) throws SemanticException {
        if (!f.clear(MEMBER_CLASS_FLAGS).equals(Flags.NONE)) {
            throw new SemanticException("Cannot declare a member class with flag(s) "
                    + f.clear(MEMBER_CLASS_FLAGS) + ".");
        }

        checkClassFlagsConflict(f);
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

        checkClassFlagsConflict(f);
        checkAccessFlags(f);
    }

    protected void checkClassFlagsConflict(Flags f) throws SemanticException {
        // A compile-time error occurs if a class is declared both final and abstract.
        // See JLS 2nd Ed. | 8.1.1.2.
        if (f.isAbstract() && f.isFinal()) {
            throw new SemanticException("Class cannot be both abstract and final");
        }
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
     * {@code ct} that may contain abstract methods that must be
     * implemented by {@code ct}. The list returned also contains
     * {@code rt}.
     */
    protected List<ReferenceType> abstractSuperInterfaces(ReferenceType rt) {
        List<ReferenceType> superInterfaces = new LinkedList<>();
        superInterfaces.add(rt);

        List<? extends ReferenceType> interfaces = rt.interfaces();
        for (ReferenceType interf : interfaces) {
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

    @Override
    public void checkClassConformance(ClassType ct) throws SemanticException {
        // build up a list of superclasses and interfaces that ct
        // extends/implements that may contain abstract methods that
        // ct inherits or must define.
        List<ReferenceType> superInterfaces = abstractSuperInterfaces(ct);

        // map used to determine incompatible return types of inherited methods
        // of same signature
        Map<String, MethodInstance> signatureMap = new HashMap<>();

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
                    else if (isInherited(mi, ct)) {
                        String signature = mi.signature();
                        if (signatureMap.containsKey(signature)) {
                            // no implementation, so ct inherits mi.
                            // Check that the return type of inherited methods
                            // are consistent.  See JLS 2nd Ed. | 8.4.6.4.
                            mj = signatureMap.get(signature);
                            if (!returnTypesConsistent(mi, mj)) {
                                throw new SemanticException("Types "
                                        + mj.container() + " and "
                                        + mi.container()
                                        + " are incompatible; both define "
                                        + signature
                                        + ", but with unrelated return types.");
                            }
                        }
                        else signatureMap.put(signature, mi);
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

    protected boolean returnTypesConsistent(MethodInstance mi,
            MethodInstance mj) {
        return typeEquals(mi.returnType(), mj.returnType());
    }

    @Override
    public void checkInterfaceFieldFlags(ClassType ct)
            throws SemanticException {
        // For interface declaration, check the flags of field declarations
        // which may only be any of: public, static, final.
        // See JLS 2nd Ed. | 9.3.
        if (!ct.flags().isInterface()) return;

        for (FieldInstance fi : ct.fields()) {
            Flags f = fi.flags();
            if (!f.clear(INTERFACE_FIELD_FLAGS).equals(Flags.NONE)) {
                throw new SemanticException("Cannot declare an interface constant with flag(s) "
                        + f.clear(INTERFACE_FIELD_FLAGS) + ".", fi.position());
            }
        }
    }

    @Override
    public MethodInstance findImplementingMethod(ClassType ct,
            MethodInstance mi) {
        // Obtain a list of declared methods in ct.
        List<? extends MethodInstance> declared =
                ct.methods(mi.name(), mi.formalTypes());
        for (MethodInstance mj : declared) {
            if (mj.flags().isAbstract()) {
                // We found a method that is declared abstract, so no
                // implementation of mi can be found for ct.
                return null;
            }
            if (mi.flags().isPublic() || mi.flags().isProtected()
                    || isAccessible(mi, ct)) {
                // If this method is implemented in ct and can override the
                // desired method, we found an implementation.
                return mj;
            }
        }

        // No method is declared and implemented in ct, so we must find an
        // implementation of the method that is inherited from ct's superclass.
        ClassType superClass =
                ct.superType() == null ? null : ct.superType().toClass();
        if (superClass == null) return null;

        MethodInstance mj = findImplementingMethod(superClass, mi);
        return mj;
    }

    @Override
    public Type staticTarget(Type t) {
        // Nothing needs done in standard Java.
        return t;
    }

    protected void initFlags() {
        flagsForName = new HashMap<>();
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
