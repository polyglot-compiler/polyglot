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
package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.JLang;
import polyglot.ast.NullLit;
import polyglot.ast.Term;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.ElementValueArrayInit;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.ext.jl5.types.inference.InferenceSolver;
import polyglot.ext.jl5.types.inference.InferenceSolver_c;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.jl5.types.inference.LubType_c;
import polyglot.ext.jl5.types.reflect.JL5ClassFileLazyClassInitializer;
import polyglot.ext.param.types.PClass;
import polyglot.ext.param.types.ParamTypeSystem_c;
import polyglot.ext.param.types.Subst;
import polyglot.frontend.Source;
import polyglot.main.Report;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.ImportTable;
import polyglot.types.LazyClassInitializer;
import polyglot.types.LocalInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.NoMemberException;
import polyglot.types.NullType;
import polyglot.types.Package;
import polyglot.types.ParsedClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JL5TypeSystem_c
        extends ParamTypeSystem_c<TypeVariable, ReferenceType>
        implements JL5TypeSystem {

    protected ClassType ENUM_;

    protected ClassType ANNOTATION_;
    protected ClassType OVERRIDE_ANNOTATION_;
    protected ClassType TARGET_ANNOTATION_;
    protected ClassType RETENTION_ANNOTATION_;
    protected ClassType ELEMENT_TYPE_;

    // this is for extended for
    protected ClassType ITERABLE_;

    protected ClassType ITERATOR_;

    @Override
    public ClassType Enum() {
        if (ENUM_ != null) {
            return ENUM_;
        }
        else {
            return ENUM_ = load("java.lang.Enum");
        }
    }

    @Override
    public ClassType Annotation() {
        if (ANNOTATION_ != null) {
            return ANNOTATION_;
        }
        else {
            return ANNOTATION_ = load("java.lang.annotation.Annotation");
        }
    }

    @Override
    public ClassType OverrideAnnotation() {
        if (OVERRIDE_ANNOTATION_ != null) {
            return OVERRIDE_ANNOTATION_;
        }
        else {
            return OVERRIDE_ANNOTATION_ = load("java.lang.Override");
        }
    }

    @Override
    public ClassType TargetAnnotation() {
        if (TARGET_ANNOTATION_ != null) {
            return TARGET_ANNOTATION_;
        }
        else {
            return TARGET_ANNOTATION_ = load("java.lang.annotation.Target");
        }
    }

    @Override
    public ClassType RetentionAnnotation() {
        if (RETENTION_ANNOTATION_ != null) {
            return RETENTION_ANNOTATION_;
        }
        else {
            return RETENTION_ANNOTATION_ =
                    load("java.lang.annotation.Retention");
        }
    }

    @Override
    public ClassType AnnotationElementType() {
        if (ELEMENT_TYPE_ != null) {
            return ELEMENT_TYPE_;
        }
        else {
            return ELEMENT_TYPE_ = load("java.lang.annotation.ElementType");
        }
    }

    @Override
    public ClassType Iterable() {
        if (ITERABLE_ != null) {
            return ITERABLE_;
        }
        else {
            return ITERABLE_ = load("java.lang.Iterable");
        }
    }

    @Override
    public ClassType Iterator() {
        if (ITERATOR_ != null) {
            return ITERATOR_;
        }
        else {
            return ITERATOR_ = load("java.util.Iterator");
        }
    }

    @Override
    public LazyClassInitializer defaultClassInitializer() {
        return new JL5SchedulerClassInitializer(this);
    }

    @Override
    public boolean accessibleFromPackage(Flags flags, Package pkg1,
            Package pkg2) {
        return super.accessibleFromPackage(flags, pkg1, pkg2);
    }

    @Override
    public ClassType wrapperClassOfPrimitive(PrimitiveType t) {
        try {
            return (ClassType) typeForName(t.wrapperTypeString(this));
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Couldn't find primitive wrapper "
                    + t.wrapperTypeString(this), e);
        }

    }

    @Override
    public PrimitiveType primitiveTypeOfWrapper(Type l) {
        try {
            if (l.equals(typeForName("java.lang.Boolean"))) return Boolean();
            if (l.equals(typeForName("java.lang.Character"))) return Char();
            if (l.equals(typeForName("java.lang.Byte"))) return Byte();
            if (l.equals(typeForName("java.lang.Short"))) return Short();
            if (l.equals(typeForName("java.lang.Integer"))) return Int();
            if (l.equals(typeForName("java.lang.Long"))) return Long();
            if (l.equals(typeForName("java.lang.Float"))) return Float();
            if (l.equals(typeForName("java.lang.Double"))) return Double();
            if (l.equals(typeForName("java.lang.Void"))) return Void();
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Couldn't find wrapper class");
        }
        return null;
    }

    @Override
    public boolean isPrimitiveWrapper(Type l) {
        if (primitiveTypeOfWrapper(l) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Flags legalTopLevelClassFlags() {
        return JL5Flags.setAnnotation(JL5Flags.setEnum(super.legalTopLevelClassFlags()));
    }

    @Override
    public Flags legalMemberClassFlags() {
        return JL5Flags.setAnnotation(JL5Flags.setEnum(super.legalMemberClassFlags()));
    }

    @Override
    protected void checkCycles(ReferenceType curr, ReferenceType goal)
            throws SemanticException {
        super.checkCycles(curr, goal);

        if (curr instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) curr;
            checkCycles(tv.upperBound(), goal);
        }
    }

    @Override
    public ConstructorInstance defaultConstructor(Position pos,
            ClassType container) {
        assert_(container);

        Flags access = Flags.NONE;

        if (container.flags().isPrivate()
                || JL5Flags.isEnum(container.flags())) {
            access = access.Private();
        }
        else if (container.flags().isProtected()) {
            access = access.Protected();
        }
        else if (container.flags().isPublic()) {
            access = access.Public();
        }
        return constructorInstance(pos,
                                   container,
                                   access,
                                   Collections.<Type> emptyList(),
                                   Collections.<Type> emptyList(),
                                   Collections.<TypeVariable> emptyList());

    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init,
            Source fromSource) {
        return new JL5ParsedClassType_c(this, init, fromSource);
    }

    @Override
    protected PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new JL5PrimitiveType_c(this, kind);
    }

    @Override
    protected NullType createNull() {
        return new JL5NullType_c(this);
    }

    @Override
    public EnumInstance findEnumConstant(ReferenceType container, String name,
            Context c) throws SemanticException {
        ClassType ct = null;
        if (c != null) ct = c.currentClass();
        return findEnumConstant(container, name, ct);
    }

    @Override
    public EnumInstance findEnumConstant(ReferenceType container, String name,
            ClassType currClass) throws SemanticException {
        Collection<EnumInstance> enumConstants =
                findEnumConstants(container, name);
        if (enumConstants.size() == 0) {
            throw new NoMemberException(JL5NoMemberException.ENUM_CONSTANT,
                                        "Enum Constant: \"" + name
                                                + "\" not found in type \""
                                                + container + "\".");
        }
        Iterator<EnumInstance> i = enumConstants.iterator();
        EnumInstance ei = i.next();

        if (i.hasNext()) {
            EnumInstance ei2 = i.next();

            throw new SemanticException("Enum Constant \"" + name
                    + "\" is ambiguous; it is defined in both " + ei.container()
                    + " and " + ei2.container() + ".");
        }

        if (currClass != null && !isAccessible(ei, currClass)
                && !isInherited(ei, currClass)) {
            throw new SemanticException("Cannot access " + ei + ".");
        }

        return ei;
    }

    @Override
    public EnumInstance findEnumConstant(ReferenceType container, String name)
            throws SemanticException {
        return findEnumConstant(container, name, (ClassType) null);
    }

    @Override
    public EnumInstance findEnumConstant(ReferenceType container, long ordinal)
            throws SemanticException {
        assert_(container);
        if (container == null) {
            throw new InternalCompilerError("Cannot access enum constant within a null container type.");
        }
        if (!container.isClass()) {
            throw new InternalCompilerError("Cannot access enum constant within a non-class container type.");
        }
        JL5ClassType ct = (JL5ClassType) container;
        for (EnumInstance ec : ct.enumConstants()) {
            if (ec.ordinal() == ordinal) {
                return ec;
            }
        }
        return null;
    }

    public Set<EnumInstance> findEnumConstants(ReferenceType container,
            String name) {
        assert_(container);
        if (container == null) {
            throw new InternalCompilerError("Cannot access enum constant \""
                    + name + "\" within a null container type.");
        }
        EnumInstance ei = null;

        if (container instanceof JL5ClassType) {
            ei = ((JL5ClassType) container).enumConstantNamed(name);
        }

        if (ei != null) {
            return Collections.singleton(ei);
        }

        return new HashSet<>();
    }

    @Override
    public EnumInstance enumInstance(Position pos, ClassType ct, Flags f,
            String name, long ordinal) {
        assert_(ct);
        return new EnumInstance_c(this, pos, ct, f, name, ordinal);
    }

    @Override
    public Context createContext() {
        return new JL5Context_c(J5Lang_c.instance, this);
    }

    @Override
    public FieldInstance findFieldOrEnum(ReferenceType container, String name,
            ClassType currClass) throws SemanticException {

        FieldInstance fi = null;

        try {
            fi = findField(container, name, currClass, true);
        }
        catch (NoMemberException e) {
            fi = findEnumConstant(container, name, currClass);
        }

        return fi;
    }

    @Override
    public MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes) {
        return methodInstance(pos,
                              container,
                              flags,
                              returnType,
                              name,
                              argTypes,
                              excTypes,
                              Collections.<TypeVariable> emptyList());
    }

    @Override
    public JL5MethodInstance methodInstance(Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes,
            List<TypeVariable> typeParams) {

        assert_(container);
        assert_(returnType);
        assert_(argTypes);
        assert_(excTypes);
        assert_(typeParams);
        return new JL5MethodInstance_c(this,
                                       pos,
                                       container,
                                       flags,
                                       returnType,
                                       name,
                                       argTypes,
                                       excTypes,
                                       typeParams);
    }

    @Override
    public ConstructorInstance constructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes) {
        return constructorInstance(pos,
                                   container,
                                   flags,
                                   argTypes,
                                   excTypes,
                                   Collections.<TypeVariable> emptyList());
    }

    @Override
    public JL5ConstructorInstance constructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes, List<TypeVariable> typeParams) {
        assert_(container);
        assert_(argTypes);
        assert_(excTypes);
        assert_(typeParams);
        return new JL5ConstructorInstance_c(this,
                                            pos,
                                            container,
                                            flags,
                                            argTypes,
                                            excTypes,
                                            typeParams);
    }

    @Override
    public LocalInstance localInstance(Position pos, Flags flags, Type type,
            String name) {
        return new JL5LocalInstance_c(this, pos, flags, type, name);
    }

    @Override
    public JL5FieldInstance fieldInstance(Position pos, ReferenceType container,
            Flags flags, Type type, String name) {
        assert_(container);
        assert_(type);
        return new JL5FieldInstance_c(this, pos, container, flags, type, name);
    }

    @Override
    public TypeVariable typeVariable(Position pos, String name,
            ReferenceType upperBound) {
//        System.err.println("JL5TS_c typevar created " + name + " " + bounds);
        return new TypeVariable_c(this, pos, name, upperBound);
    }

    @Override
    public UnknownTypeVariable unknownTypeVariable(Position position) {
        return new UnknownTypeVariable_c(this);
    }

    @Override
    public boolean isBaseCastValid(Type fromType, Type toType) {
        if (toType.isArray()) {
            Type base = ((ArrayType) toType).base();
            assert_(base);
            return isImplicitCastValid(fromType, base);
        }
        return false;
    }

    @Override
    public boolean numericConversionBaseValid(Type t, Object value) {
        if (t.isArray()) {
            return super.numericConversionValid(((ArrayType) t).base(), value);
        }
        return false;
    }

    @Override
    public Flags flagsForBits(int bits) {
        Flags f = super.flagsForBits(bits);
        if ((bits & JL5Flags.ENUM_MOD) != 0) {
            f = JL5Flags.setEnum(f);
        }
        if ((bits & JL5Flags.VARARGS_MOD) != 0) {
            f = JL5Flags.setVarArgs(f);
        }
        if ((bits & JL5Flags.ANNOTATION_MOD) != 0) {
            f = JL5Flags.setAnnotation(f);
        }
        return f;
    }

    @Override
    public ClassFileLazyClassInitializer classFileLazyClassInitializer(
            ClassFile clazz) {
        //return new ClassFileLazyClassInitializer(clazz, this);
        return new JL5ClassFileLazyClassInitializer(clazz, this);
    }

    @Override
    public ImportTable importTable(String sourceName,
            polyglot.types.Package pkg) {
        assert_(pkg);
        return new JL5ImportTable(this, pkg, sourceName);
    }

    @Override
    public ImportTable importTable(polyglot.types.Package pkg) {
        assert_(pkg);
        return new JL5ImportTable(this, pkg);
    }

    protected ArrayType createArrayType(Position pos, Type type,
            boolean isVarargs) {
        JL5ArrayType at = new JL5ArrayType_c(this, pos, type, isVarargs);
        return at;
    }

    @Override
    public ArrayType arrayOf(Position position, Type type, boolean isVarargs) {
        return arrayType(position, type, isVarargs);
    }

    /**
     * Factory method for ArrayTypes.
     */
    @Override
    protected ArrayType createArrayType(Position pos, Type type) {
        return new JL5ArrayType_c(this, pos, type, false);
    }

    Map<Type, ArrayType> varargsArrayTypeCache = new HashMap<>();

    protected ArrayType arrayType(Position pos, Type type, boolean isVarargs) {
        if (isVarargs) {
            ArrayType t = varargsArrayTypeCache.get(type);
            if (t == null) {
                t = createArrayType(pos, type, isVarargs);
                varargsArrayTypeCache.put(type, t);
            }
            return t;
        }
        else {
            return super.arrayType(pos, type);
        }
    }

    /*@Override
    protected Collection findMostSpecificProcedures(List acceptable) throws SemanticException {
        throw new InternalCompilerError("Unimplemented");
    }*/

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     */
    @Override
    protected List<? extends MethodInstance> findAcceptableMethods(
            ReferenceType container, String name, List<? extends Type> argTypes,
            ClassType currClass, boolean fromClient) throws SemanticException {
        return findAcceptableMethods(container,
                                     name,
                                     argTypes,
                                     Collections.<ReferenceType> emptyList(),
                                     currClass,
                                     fromClient);
    }

    protected List<? extends MethodInstance> findAcceptableMethods(
            ReferenceType container, String name, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            boolean fromClient) throws SemanticException {
        return findAcceptableMethods(container,
                                     name,
                                     argTypes,
                                     actualTypeArgs,
                                     currClass,
                                     null,
                                     fromClient);
    }

    protected List<? extends MethodInstance> findAcceptableMethods(
            ReferenceType container, String name, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            Type expectedReturnType, boolean fromClient)
                    throws SemanticException {
        assert_(container);
        assert_(argTypes);

        // apply capture conversion to container
        container =
                (ReferenceType) applyCaptureConversion(container,
                                                       container.position());

        SemanticException error = null;

        // List of methods accessible from curClass that have valid method
        // calls without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible method
        List<MethodInstance> phase1methods = new ArrayList<>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion
        List<MethodInstance> phase2methods = new ArrayList<>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion and variable arity
        List<MethodInstance> phase3methods = new ArrayList<>();

        // A list of unacceptable methods, where the method call is valid, but
        // the method is not accessible. This list is needed to make sure that
        // the acceptable methods are not overridden by an unacceptable method.
        List<MethodInstance> inaccessible = new ArrayList<>();

        // A set of all the methods that methods in phase[123]methods override.
        // Used to make sure we don't mistakenly add in overridden methods
        // (since overridden methods aren't inherited from superclasses).
        Set<MethodInstance> phase1overridden = new HashSet<>();
        Set<MethodInstance> phase2overridden = new HashSet<>();
        Set<MethodInstance> phase3overridden = new HashSet<>();

        Set<Type> visitedTypes = new HashSet<>();

        LinkedList<Type> typeQueue = new LinkedList<>();
        typeQueue.addLast(container);

//        System.err.println("JL5TS: findAcceptableMethods for " + name + " in " + container);
        while (!typeQueue.isEmpty()) {
            Type type = typeQueue.remove();

//            System.err.println("   looking at type " + type + " " + type.getClass());
            // Make sure each type is considered only once
            if (visitedTypes.contains(type)) continue;
            visitedTypes.add(type);

            if (Report.should_report(Report.types, 2)) {
                Report.report(2,
                              "Searching type " + type + " for method " + name
                                      + "(" + listToString(argTypes) + ")");
            }

            if (!type.isReference()) {
                throw new SemanticException("Cannot call method in "
                        + " non-reference type " + type + ".");
            }

            @SuppressWarnings("unchecked")
            List<JL5MethodInstance> methods =
                    (List<JL5MethodInstance>) type.toReference().methods();
            for (JL5MethodInstance mi : methods) {
                if (Report.should_report(Report.types, 3))
                    Report.report(3, "Trying " + mi);

                // Method name must match
                if (!mi.name().equals(name)) continue;
//                System.err.println("      checking " + mi);

                JL5MethodInstance substMi =
                        methodCallValid(mi,
                                        name,
                                        argTypes,
                                        actualTypeArgs,
                                        expectedReturnType);
                JL5MethodInstance origMi = mi;
                if (substMi != null) {
                    mi = substMi;
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
                        if (varArgsRequired(mi)) {
                            if (!phase3overridden.contains(mi)
                                    && !phase3overridden.contains(origMi)) {
                                phase3overridden.addAll(mi.implemented());
                                phase3overridden.addAll(origMi.implemented());
                                phase3methods.removeAll(mi.implemented());
                                phase3methods.removeAll(origMi.implemented());
                                phase3methods.add(mi);
                            }
                        }
                        else if (boxingRequired(mi, argTypes)) {
                            if (!phase2overridden.contains(mi)
                                    && !phase2overridden.contains(origMi)) {
                                phase2overridden.addAll(mi.implemented());
                                phase2overridden.addAll(origMi.implemented());
                                phase2methods.removeAll(mi.implemented());
                                phase2methods.removeAll(origMi.implemented());
                                phase2methods.add(mi);
                            }
                        }
                        else {
                            if (!phase1overridden.contains(mi)
                                    && !phase1overridden.contains(origMi)) {
                                phase1overridden.addAll(mi.implemented());
                                phase1overridden.addAll(origMi.implemented());
                                phase1methods.removeAll(mi.implemented());
                                phase1methods.removeAll(origMi.implemented());
                                phase1methods.add(mi);
                            }
                        }
                    }
                    else {
                        // method call is valid but the method is unaccessible
                        inaccessible.add(mi);
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

            if (type instanceof JL5ClassType) {
                for (Type superT : ((JL5ClassType) type).superclasses()) {
                    if (superT != null && superT.isReference()) {
                        typeQueue.addLast(superT.toReference());
                    }
                }
            }
            else {
                Type superT = type.toReference().superType();
                if (superT != null && superT.isReference()) {
                    typeQueue.addLast(superT.toReference());
                }

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

        // remove any methods that are overridden by an inaccessible method
        for (MethodInstance mi : inaccessible) {
            phase1methods.removeAll(mi.overrides());
            phase2methods.removeAll(mi.overrides());
            phase3methods.removeAll(mi.overrides());
        }

//        System.err.println("JL5ts_c: acceptable methods for " + name + argTypes
//                + " is " + phase1methods);
//        System.err.println("              " + phase2methods);
//        System.err.println("              " + phase3methods);
//        System.err.println("        phase1overridden is    " + phase1overridden);
//        System.err.println("        phase2overridden is    " + phase2overridden);
//        System.err.println("        phase3overridden is    " + phase3overridden);
        if (!phase1methods.isEmpty()) return phase1methods;
        if (!phase2methods.isEmpty()) return phase2methods;
        if (!phase3methods.isEmpty()) return phase3methods;

        // No acceptable accessible methods were found
        throw error;
    }

    @Override
    public boolean methodCallValid(MethodInstance mi, String name,
            List<? extends Type> argTypes) {
        return this.methodCallValid((JL5MethodInstance) mi,
                                    name,
                                    argTypes,
                                    null,
                                    null) != null;
    }

    @Override
    public JL5MethodInstance methodCallValid(JL5MethodInstance mi, String name,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs,
            Type expectedReturnType) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.emptyList();
        }

        // First check that the number of arguments is reasonable
        if (argTypes.size() != mi.formalTypes().size()) {
            // the actual args don't match the number of the formal args.
            if (!(mi.isVariableArity()
                    && argTypes.size() >= mi.formalTypes().size() - 1)) {
                // the last (variable) argument can consume 0 or more of the actual arguments.
                return null;
            }

        }
        JL5Subst subst = null;
        if (!mi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(mi, argTypes, expectedReturnType);
        }
        else if (!mi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            Map<TypeVariable, ReferenceType> m = new HashMap<>();
            Iterator<? extends ReferenceType> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : mi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m);
        }
        JL5MethodInstance mj = mi;
        if (!mi.typeParams().isEmpty() && subst != null) {
            // check that the substitution satisfies the bounds
            for (TypeVariable tv : subst.substitutions().keySet()) {
                Type a = subst.substitutions().get(tv);
                Type substUpperBound = subst.substType(tv.upperBound());
                if (!isSubtype(a, substUpperBound)) {
                    return null;
                }
            }
            //mj = (JL5MethodInstance) this.instantiate(mi.position(), mi, actualTypeArgs);
            mj = subst.substMethod(mi);
        }
//        System.err.println("JL5TS methocall valid to " + mi + " with argtypes "
//                + argTypes + " and actuals " + actualTypeArgs);
//        System.err.println("  subst is " + subst);
//        System.err.println("  Call to mi " + mi + " after inference is " + mj);
//        System.err.println("  super.methodCallValid ? "
//                + super.methodCallValid(mj, name, argTypes));
        if (super.methodCallValid(mj, name, argTypes)) {
            return mj;
        }
        return null;
    }

    @Override
    public boolean callValid(ProcedureInstance mi,
            List<? extends Type> argTypes) {
        return this.callValid((JL5ProcedureInstance) mi,
                              argTypes,
                              null) != null;
    }

    @Override
    public JL5ProcedureInstance callValid(JL5ProcedureInstance mi,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.emptyList();
        }
        JL5Subst subst = null;

        if (!mi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(mi, argTypes, null);
        }
        else if (!mi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            // Number of actual type parameters must be equal to number of
            // formal type parameters.
            if (mi.typeParams().size() != actualTypeArgs.size()) return null;
            Map<TypeVariable, ReferenceType> m = new HashMap<>();
            Iterator<? extends ReferenceType> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : mi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m);
        }

        JL5ProcedureInstance mj = mi;
        if (!mi.typeParams().isEmpty() && subst != null) {
            // check that the substitution satisfies the bounds

            for (TypeVariable tv : subst.substitutions().keySet()) {
                Type a = subst.substitutions().get(tv);
                if (!isSubtype(a, tv.upperBound())) {
                    return null;
                }
            }

            mj = subst.substProcedure(mi);
        }

        if (super.callValid(mj, argTypes)) {
            return mj;
        }

        return null;
    }

    /**
     * Infer type arguments for mi, when it is called with arguments of type argTypes
     * @param pi
     * @param argTypes
     * @return
     */
    protected JL5Subst inferTypeArgs(JL5ProcedureInstance pi,
            List<? extends Type> argTypes, Type expectedReturnType) {
        InferenceSolver s = inferenceSolver(pi, argTypes);
        Map<TypeVariable, ReferenceType> m = s.solve(expectedReturnType);
        if (m == null) return null;
        JL5Subst subst = (JL5Subst) this.subst(m);
        return subst;
    }

    protected InferenceSolver inferenceSolver(JL5ProcedureInstance pi,
            List<? extends Type> argTypes) {
        return new InferenceSolver_c(pi, argTypes, this);
    }

    @Override
    public ClassType instantiate(Position pos,
            PClass<TypeVariable, ReferenceType> base,
            List<? extends ReferenceType> actuals) throws SemanticException {
        JL5ParsedClassType clazz = (JL5ParsedClassType) base.clazz();
        return instantiate(pos, clazz, actuals);
    }

    @Override
    public ClassType instantiate(Position pos, JL5ParsedClassType clazz,
            ReferenceType... actuals) throws SemanticException {
        return this.instantiate(pos, clazz, Arrays.asList(actuals));
    }

    @Override
    public ClassType instantiate(Position pos, JL5ParsedClassType clazz,
            List<? extends ReferenceType> actuals) throws SemanticException {
        if (clazz.typeVariables().isEmpty() || actuals == null
                || actuals.isEmpty()) {
            return clazz;
        }
        boolean allNull = true;
        for (ReferenceType t : actuals) {
            if (t != null) {
                allNull = false;
                break;
            }
        }
        if (allNull) {
            return clazz;
        }
        return super.instantiate(pos, clazz.pclass(), actuals);
    }

    @Override
    public JL5ProcedureInstance instantiate(Position pos,
            JL5ProcedureInstance mi, List<? extends ReferenceType> actuals) {
        Map<TypeVariable, ReferenceType> m = new LinkedHashMap<>();
        Iterator<? extends ReferenceType> iter = actuals.iterator();
        for (TypeVariable tv : mi.typeParams()) {
            m.put(tv, iter.next());
        }
        JL5Subst subst = (JL5Subst) this.subst(m);
        JL5ProcedureInstance ret = subst.substProcedure(mi);
        ret.setContainer(mi.container());
        return ret;
    }

    protected boolean boxingRequired(JL5ProcedureInstance pi,
            List<? extends Type> paramTypes) {
        int numFormals = pi.formalTypes().size();
        for (int i = 0; i < numFormals - 1; i++) {
            Type formal = pi.formalTypes().get(i);
            Type actual = paramTypes.get(i);
            if (formal.isPrimitive() ^ actual.isPrimitive()) return true;
        }
        if (pi.isVariableArity()) {
            Type lastParams = ((JL5ArrayType) pi.formalTypes()
                                                .get(numFormals - 1)).base();
            for (int i = numFormals - 1; i < paramTypes.size() - 1; i++) {
                if (lastParams.isPrimitive() ^ paramTypes.get(i).isPrimitive())
                    return true;
            }
        }
        else if (numFormals > 0) {
            Type formal = pi.formalTypes().get(numFormals - 1);
            Type actual = paramTypes.get(numFormals - 1);
            if (formal.isPrimitive() ^ actual.isPrimitive()) return true;
        }
        return false;
    }

    protected boolean varArgsRequired(JL5ProcedureInstance pi) {
        return pi.isVariableArity();
    }

    @Override
    public List<ReferenceType> allAncestorsOf(ReferenceType rt) {
        Set<ReferenceType> ancestors = new LinkedHashSet<>();
        ancestors.add(rt);
        Set<? extends Type> superClasses;
        if (rt.isClass()) {
            superClasses = ((JL5ClassType) rt).superclasses();
        }
        else {
            superClasses = Collections.singleton(rt.superType());
        }
        for (Type superT : superClasses) {
            if (superT.isReference()) {
                ancestors.add((ReferenceType) superT);
                ancestors.addAll(allAncestorsOf((ReferenceType) superT));
            }
        }
        for (ReferenceType inter : rt.interfaces()) {
            ancestors.add(inter);
            ancestors.addAll(allAncestorsOf(inter));
        }
        return new ArrayList<>(ancestors);
    }

    public static String listToString(List<?> l) {
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

    @Override
    protected Subst<TypeVariable, ReferenceType> substImpl(
            Map<TypeVariable, ? extends ReferenceType> substMap) {
        return new JL5Subst_c(this, substMap);
    }

    @Override
    public boolean hasSameSignature(JL5ProcedureInstance mi,
            JL5ProcedureInstance mj) {
        return hasSameSignature(mi, mj, false);
    }

    protected boolean hasSameSignature(JL5ProcedureInstance mi,
            JL5ProcedureInstance mj, boolean eraseMj) {
        // JLS 3rd Ed. | 8.4.2
        // Two methods have the same signature if they have the same name
        // and argument types.
        // Two methods have the same argument types if all of the following hold:
        // - They have same number of formal parameters
        // - They have same number of type parameters
        // - After renaming type parameters to match, the bounds of type
        //   variables and argument types are the same.
        if (mi instanceof JL5MethodInstance
                && mj instanceof JL5MethodInstance) {
            if (!((JL5MethodInstance) mi).name()
                                         .equals(((JL5MethodInstance) mj).name())) {
                return false;
            }
        }
        if (mi.formalTypes().size() != mj.formalTypes().size()) {
            return false;
        }
        if (eraseMj && !mi.typeParams().isEmpty()) {
            // we are erasing mj, so it has no type parameters.
            // so mi better have no type parameters
            return false;

        }
        else if (!eraseMj && mi.typeParams().size() != mj.typeParams().size()) {
            // we are not erasing mj, so it and mi better
            // have the same number of type parameters.
            return false;
        }

        // replace the type variables of mj with the type variables of mi
        if (!eraseMj && !mi.typeParams().isEmpty()) {
            Map<TypeVariable, ReferenceType> substm = new LinkedHashMap<>();
            for (int i = 0; i < mi.typeParams().size(); i++) {
                substm.put(mj.typeParams().get(i), mi.typeParams().get(i));
            }
            Subst<TypeVariable, ReferenceType> subst = this.subst(substm);

            // Check that bounds of type variables match
            for (Iterator<? extends TypeVariable> typesi =
                    mi.typeParams().iterator(), typesj =
                            mj.typeParams().iterator(); typesi.hasNext();) {
                TypeVariable ti = typesi.next();
                TypeVariable tj = typesj.next();
                if (!ti.upperBound().equals(subst.substType(tj.upperBound())))
                    return false;
            }

            if (mj instanceof JL5MethodInstance)
                mj = subst.substMethod((JL5MethodInstance) mj);
            else mj = subst.substConstructor((JL5ConstructorInstance) mj);
        }

        // Check that the argument types match
        for (Iterator<? extends Type> typesi =
                mi.formalTypes().iterator(), typesj =
                        mj.formalTypes().iterator(); typesi.hasNext();) {
            Type ti = typesi.next();
            Type tj = typesj.next();
            if (eraseMj) {
                tj = this.erasureType(tj);
            }
            if (!ti.equals(tj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSubSignature(JL5ProcedureInstance m1,
            JL5ProcedureInstance m2) {
        if (hasSameSignature(m1, m2)) {
            return true;
        }
        // check if the signature of m1 is the same as the erasure of m2.
        return hasSameSignature(m1, m2, true);
    }

    @Override
    public boolean areOverrideEquivalent(JL5ProcedureInstance mi,
            JL5ProcedureInstance mj) {
        return isSubSignature(mi, mj) || isSubSignature(mj, mi);
    }

    @Override
    public boolean isUncheckedConversion(Type fromType, Type toType) {
        if (fromType instanceof JL5ClassType
                && toType instanceof JL5ClassType) {
            JL5ClassType from = (JL5ClassType) fromType;
            JL5ClassType to = (JL5ClassType) toType;
            if (from.isRawClass()) {
                if (!to.isRawClass() && to instanceof JL5SubstClassType) {
                    JL5SubstClassType tosct = (JL5SubstClassType) to;
                    return from.equals(tosct.base());
                }
            }
        }
        return false;
    }

    @Override
    public boolean areReturnTypeSubstitutable(Type ri, Type rj) {
        if (ri.isPrimitive()) {
            return ri.equals(rj);
        }
        else if (ri.isReference()) {
            return ri.isSubtype(rj) || isUncheckedConversion(ri, rj)
                    || ri.isSubtype(this.erasureType(rj));
        }
        else if (ri.isVoid()) {
            return rj.isVoid();
        }
        else {
            throw new InternalCompilerError("Unexpected return type: " + ri);
        }
    }

    @Override
    public MethodInstance findImplementingMethod(ClassType ct,
            MethodInstance mi) {
        // Obtain a list of declared methods in ct.
        List<? extends MethodInstance> declared = ct.methodsNamed(mi.name());
        for (MethodInstance mj : declared) {
            if (!areOverrideEquivalent((JL5MethodInstance) mi,
                                       (JL5MethodInstance) mj))
                continue;
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
    public Type erasureType(Type t) {
        return this.erasureType(t, new HashSet<TypeVariable>());
    }

    protected Type erasureType(Type t, Set<TypeVariable> visitedTypeVariables) {
        if (t.isArray()) {
            ArrayType at = t.toArray();
            return at.base(this.erasureType(at.base(), visitedTypeVariables));
        }
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            if (!visitedTypeVariables.add(tv)) {
                // tv was already in visitedTypeVariables
                // whoops, we're in some kind of recursive type
                return Object();
            }

            ReferenceType upperBound = tv.upperBound();
            if (upperBound instanceof IntersectionType) {
                // if the type variable has an intersection type bound,
                // then the erasure is the erasure of the left-most bound.
                // See JLS 3rd ed, Section 4.4
                IntersectionType it = (IntersectionType) upperBound;
                return this.erasureType(it.bounds().get(0),
                                        visitedTypeVariables);

            }
            else {
                return this.erasureType(upperBound, visitedTypeVariables);
            }
        }
        if (t instanceof IntersectionType) {
            IntersectionType it = (IntersectionType) t;
            ClassType ct = null; // most specific class type so far
            ClassType iface = null; // most specific interface type so far
            boolean subtypes = true; // are all the interfaces in a subtype relation?
            // Find the most specific class
            for (ReferenceType rt : it.bounds()) {
                ReferenceType origRt = rt;
                if (rt instanceof TypeVariable) {
                    rt = (ReferenceType) erasureType(rt, visitedTypeVariables);
                }
                if (!rt.isClass()) {
                    throw new InternalCompilerError("Don't know how to deal with erasure of intersection type "
                            + it + ", specifcally component " + origRt,
                                                    t.position());
                }
                ClassType next = (ClassType) rt;
                if (equals(Object(), next)) continue;
                if (!next.toClass().flags().isInterface()) {
                    // Is next more specific than ct?
                    if (ct == null || next.descendsFrom(ct)) {
                        ct = next;
                    }
                }
                else if (subtypes) {
                    // Is next a more specific subtype than iface?
                    if (iface == null || next.descendsFrom(iface)) {
                        iface = next;
                    }
                    // Is iface a subtype of next?
                    else if (!iface.descendsFrom(next)) {
                        subtypes = false;
                    }
                }
            }
            // Return the most-specific class, if there is one
            if (ct != null) {
                return erasureType(ct, visitedTypeVariables);
            }
            // Otherwise if the interfaces are all subtypes, return iface

            if (subtypes && iface != null) {
                return erasureType(iface, visitedTypeVariables);
            }
            return Object();

        }
        if (t instanceof WildCardType) {
            WildCardType tv = (WildCardType) t;
            if (tv.upperBound() == null) {
                return Object();
            }
            return this.erasureType(tv.upperBound(), visitedTypeVariables);
        }
        if (t instanceof JL5SubstType) {
            JL5SubstType jst = (JL5SubstType) t;
            return this.erasureType(jst.base(), visitedTypeVariables);
        }
        if (t instanceof JL5ParsedClassType) {
            return toRawType(t);
        }
        return t;
    }

    @Override
    public JL5Subst erasureSubst(JL5ProcedureInstance pi) {
        List<TypeVariable> typeParams = pi.typeParams();
        Map<TypeVariable, ReferenceType> m = new LinkedHashMap<>();
        for (TypeVariable tv : typeParams) {
            m.put(tv, tv.erasureType());
        }
        if (m.isEmpty()) {
            return null;
        }
        return new JL5Subst_c(this, m);
    }

    @Override
    public JL5Subst erasureSubst(JL5ParsedClassType base) {
        Map<TypeVariable, ReferenceType> m = new LinkedHashMap<>();
        JL5ParsedClassType t = base;
        while (t != null) {
            for (TypeVariable tv : t.typeVariables()) {
                m.put(tv, tv.erasureType());
            }
            if (!(t.outer() instanceof JL5ParsedClassType)) {
                // no more type variables that we care about!
                break;
            }
            t = (JL5ParsedClassType) t.outer();
        }
        if (m.isEmpty()) {
            return null;
        }
        return new JL5RawSubst_c(this, m, base);
    }

    @Override
    public boolean isContained(Type fromType, Type toType) {
        if (toType instanceof WildCardType) {
            WildCardType wTo = (WildCardType) toType;
            if (fromType instanceof WildCardType) {
                WildCardType wFrom = (WildCardType) fromType;
                // JLS 3rd ed 4.5.1.1
                if (wFrom.isExtendsConstraint() && wTo.isExtendsConstraint()) {
                    if (isSubtype(wFrom.upperBound(), wTo.upperBound())) {
                        return true;
                    }
                }
                if (wFrom.isSuperConstraint() && wTo.isSuperConstraint()) {
                    if (isSubtype(wTo.lowerBound(), wFrom.lowerBound())) {
                        return true;
                    }
                }

            }
            if (wTo.isSuperConstraint()) {
                if (isImplicitCastValid(wTo.lowerBound(), fromType)) {
                    return true;
                }
            }
            else if (wTo.isExtendsConstraint()) {
                if (isImplicitCastValid(fromType, wTo.upperBound())) {
                    return true;
                }
            }
            return false;
        }
        else {
            return typeEquals(fromType, toType);
        }

    }

    @Override
    public boolean descendsFrom(Type child, Type ancestor) {
//        System.err.println("jl5TS_C: descends from: " + child + " descended from " + ancestor);
        boolean b = super.descendsFrom(child, ancestor);
        if (b) return true;
//        System.err.println("   : descends from 0");
        if (ancestor instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) ancestor;
            // See JLS 3rd ed 4.10.2: type variable is a direct supertype of its lowerbound.
            if (tv.hasLowerBound()) {
//                System.err.println("   : descends from 1");
                return isSubtype(child, tv.lowerBound());
            }
        }
        if (ancestor instanceof WildCardType) {
            WildCardType w = (WildCardType) ancestor;
            // See JLS 3rd ed 4.10.2: type variable is a direct supertype of its lowerbound.
            if (w.hasLowerBound()) {
//                System.err.println("   : descends from 2");
                return isSubtype(child, w.lowerBound());
            }
        }
        if (ancestor instanceof LubType) {
            LubType lub = (LubType) ancestor;
            // LUB is a supertype of each of its elements
            for (ReferenceType rt : lub.lubElements()) {
                if (descendsFrom(child, rt)) return true;
            }
        }
//        System.err.println("   : descends from 3");
        return false;
    }

    @Override
    public boolean isSubtype(Type t1, Type t2) {
        if (super.isSubtype(t1, t2)) {
            return true;
        }
        if (t2 instanceof WildCardType) {
            WildCardType wct = (WildCardType) t2;
            if (wct.hasLowerBound() && isSubtype(t1, wct.lowerBound())) {
                return true;
            }
        }
        if (t2 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t2;
            if (tv.hasLowerBound() && isSubtype(t1, tv.lowerBound())) {
                return true;
            }
        }
        if (t2 instanceof IntersectionType) {
            IntersectionType it = (IntersectionType) t2;
            // t1 is a substype of u1&u2&...&un if there is some ui such
            // that t1 is a subtype of ui.
            for (Type t : it.bounds()) {
                if (isSubtype(t1, t)) {
                    return true;
                }
            }
        }
        if (t2 instanceof LubType) {
            LubType lub = (LubType) t2;
            // t2 is an upper bound of several types. If t1 is a subtype of any of them,
            // then t2 is a subtype of lub.
            for (ReferenceType upperBound : lub.lubElements()) {
                if (isSubtype(t1, upperBound)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isImplicitCastValid(Type fromType, Type toType) {
        LinkedList<Type> chain = isImplicitCastValidChain(fromType, toType);
        // Try an unchecked conversion, if toType is a parameterized type.
        if (chain == null && toType instanceof JL5SubstClassType) {
            JL5SubstClassType toSubstCT = (JL5SubstClassType) toType;
            chain = isImplicitCastValidChain(fromType,
                                             this.rawClass(toSubstCT.base(),
                                                           toSubstCT.base()
                                                                    .position()));
            if (chain != null) {
                // success!
                chain.addLast(toType);
            }
        }

        if (chain == null) {
            return false;
        }
        // check whether "the chain of conversions contains two parameterized types that are not not in the subtype relation."
        // See JLS 3rd ed 5.2 and 5.3.
        for (int i = 0; i < chain.size(); i++) {
            Type t = chain.get(i);
            if (t instanceof JL5SubstClassType) {
                for (int j = i + 1; j < chain.size(); j++) {
                    Type u = chain.get(j);
                    if (u instanceof JL5SubstClassType) {
                        if (!t.isSubtype(u)) {
                            return false;
                        }
                    }
                }

            }
        }
        return true;
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChain(Type fromType,
            Type toType) {
        assert_(fromType);
        assert_(toType);
        if (fromType == null || toType == null) {
            throw new IllegalArgumentException("isImplicitCastValidChain: "
                    + fromType + " " + toType);
        }

        LinkedList<Type> chain = null;
        if (fromType instanceof JL5ClassType) {
            chain = ((JL5ClassType) fromType).isImplicitCastValidChainImpl(toType);
        }
        else if (fromType.isImplicitCastValidImpl(toType)) {
            chain = new LinkedList<>();
            chain.add(fromType);
            chain.add(toType);
        }

        return chain;
    }

    @Override
    public boolean numericConversionValid(Type type, Object value) {
        // Optional support for allowing a boxing conversion when using a literal
        // in an initializer for compatibility with with "javac -source 1.5"
        JL5Options opts = (JL5Options) extInfo.getOptions();
        if (opts.morePermissiveCasts && isPrimitiveWrapper(type)) {
            return super.numericConversionValid(primitiveTypeOfWrapper(type),
                                                value);
        }
        else {
            return super.numericConversionValid(type, value);
        }
    }

    @Override
    public boolean isCastValid(Type fromType, Type toType) {
        if (super.isCastValid(fromType, toType)) {
            return true;
        }

        // Optional support for widening conversion after unboxing for compatibility
        // with "javac -source 1.5"
        JL5Options opts = (JL5Options) extensionInfo().getOptions();
        if (opts.morePermissiveCasts) {
            if (isPrimitiveWrapper(fromType) && toType.isPrimitive()) {
                if (isImplicitCastValid(unboxingConversion(fromType), toType)) {
                    return true;
                }
            }
        }

        // JLS 3rd ed. Section 5.5
        if (fromType.isClass()) {
            if (!fromType.toClass().flags().isInterface()) {
                // fromType is class type
                return isCastValidFromClass(fromType.toClass(), toType);
            }
            else {
                return isCastValidFromInterface(fromType.toClass(), toType);
                // fromType is an interface
            }
        }
        else if (fromType instanceof TypeVariable) {
            return isCastValid(((TypeVariable) fromType).upperBound(), toType);
        }
        else if (fromType.isArray()) {
            return isCastValidFromArray(fromType.toArray(), toType);
        }
        return false;
    }

    protected boolean isCastValidFromClass(ClassType fromType, Type toType) {
        if (toType instanceof TypeVariable) {
            return isCastValid(fromType, ((TypeVariable) toType).upperBound());
        }
        if (toType.isClass()) {
            if (!toType.toClass().flags().isInterface()) {
                // toType is a class type
                Type erasedFrom = erasureType(fromType);
                Type erasedTo = erasureType(toType);
                return (erasedFrom != fromType || erasedTo != toType)
                        && (erasedFrom.isSubtype(erasedTo)
                                || erasedTo.isSubtype(erasedFrom));
                // TODO: need to check whether there is a supertype X
                // of this and Y of toType that have the same erasure
                // and are provably distinct.
            }
            else {
                // toType is an interface
                // TODO: need to check whether there is a supertype X
                // of this and Y of toType that have the same erasure
                // and are provably distinct.
            }
        }
        return false;
    }

    protected boolean isCastValidFromInterface(ClassType fromType,
            Type toType) {
        // If T is an array type, then T must implement S, or a compile-time error occurs
        // This is handled in the super class.

        if (toType.isClass() && toType.toClass().flags().isFinal()) {
            // toType is final.
            if (fromType instanceof RawClass
                    || fromType instanceof JL5SubstClassType) {
                // S is either a parameterized type that is an invocation of some generic type declaration G, or a raw type corresponding to a generic type declaration G.
                // Then there must exist a supertype X of T, such that X is an invocation of G, or a compile-time error occurs.
                JL5ParsedClassType baseClass;
                if (fromType instanceof RawClass) {
                    baseClass = ((RawClass) fromType).base();
                }
                else {
                    baseClass = ((JL5SubstClassType) fromType).base();
                }
                JL5SubstClassType x =
                        findGenericSupertype(baseClass, toType.toReference());
                if (x == null) {
                    return false;
                }

                // Furthermore, if S and X are provably distinct parameterized types then a compile-time error occurs.
                if (fromType instanceof JL5SubstClassType
                        && areProvablyDistinct((JL5SubstClassType) fromType,
                                               x)) {
                    return false;
                }
            }
            else {
                // S is not a parameterized type or a raw type, and T is final
                // Then T must implement S, and the cast is statically known to be correct, or a compile-time error occurs.
                if (!isSubtype(toType, fromType)) {
                    // XXX this takes care that T must implement S. Not sure why there is a requirement for the cast to statically known to be correct. That would seem to imply that fromType is a subtype of toType?!
                    return false;
                }

            }
            return true;

        }
        else {
            // T is a type that is not final (�8.1.1), and S is an interface
            // if there exists a supertype X of T, and a supertype Y of S, such that both X and Y are provably distinct parameterized types,
            // and that the erasures of X and Y are the same, a compile-time error occurs.
            // Go through the supertypes of each.
            List<ReferenceType> allY = allAncestorsOf(fromType.toReference());
            List<ReferenceType> allX = allAncestorsOf(toType.toReference());
            for (ReferenceType y : allY) {
                for (ReferenceType x : allX) {
                    if (x instanceof JL5SubstClassType
                            && y instanceof JL5SubstClassType
                            && areProvablyDistinct((JL5SubstClassType) x,
                                                   (JL5SubstClassType) y)
                            && erasureType(x).equals(erasureType(y))) {
                        return false;
                    }
                }
            }

            // Otherwise, the cast is always legal at compile time (because even if T does not implement S, a subclass of T might).
            return true;
        }
    }

    protected boolean isCastValidFromArray(ArrayType arrayType, Type toType) {
        if (toType.equals(Object()) || toType.equals(Serializable())
                || toType.equals(Cloneable())) {
            return true;
        }
        if (toType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) toType;
            Type upperBound = tv.upperBound();
            if (upperBound.equals(Object()) || upperBound.equals(Serializable())
                    || upperBound.equals(Cloneable())) {
                return true;
            }
            if (upperBound.isArray()) {
                return isCastValidFromArray(arrayType, upperBound);
            }
            if (upperBound instanceof TypeVariable) {
                // should we do a recursive call? Check whether there are cycles in the type variables...
                Set<TypeVariable> visited = new HashSet<>();
                visited.add(tv);
                while (upperBound instanceof TypeVariable) {
                    if (!visited.add((TypeVariable) upperBound)) {
                        break;
                    }
                    upperBound = ((TypeVariable) upperBound).upperBound();
                }
                if (!(upperBound instanceof TypeVariable)) {
                    // no cycle in the upper bounds of type variables!
                    return isCastValidFromArray(arrayType, upperBound);
                }

            }

            return false;
        }
        if (toType.isArray()) {
            ArrayType toArrayType = toType.toArray();
            if (arrayType.base().isPrimitive()
                    && arrayType.base().equals(toArrayType.base())) {
                return true;
            }
            if (arrayType.base().isReference()
                    && toArrayType.base().isReference()) {
                return isCastValid(arrayType.base(), toArrayType.base());
            }
        }
        return false;
    }

    private static boolean areProvablyDistinct(JL5SubstClassType s,
            JL5SubstClassType t) {
        // See JLS 3rd ed 4.5
        // Distinct if either (1) They are invocations of distinct generic type declarations.
        // or (2) Any of their type arguments are provably distinct
        JL5SubstClassType x = s;
        JL5SubstClassType y = t;
        if (!x.base().equals(y.base())) {
            return true;
        }
        List<ReferenceType> xActuals = x.actuals();
        List<ReferenceType> yActuals = y.actuals();
        if (xActuals.size() != yActuals.size()) {
            return true;
        }
        for (int i = 0; i < xActuals.size(); i++) {
            if (areTypArgsProvablyDistinct(xActuals.get(i), xActuals.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean areTypArgsProvablyDistinct(ReferenceType s,
            ReferenceType t) {
        // JLS 3rd ed 4.5. "Two type arguments are provably distinct if
        // neither of the arguments is a type variable or wildcard, and
        // the two arguments are not the same type."
        return !(s instanceof TypeVariable) && !(t instanceof TypeVariable)
                && !(s instanceof WildCardType) && !(t instanceof WildCardType)
                && !s.equals(t);
    }

    @Override
    protected List<ReferenceType> abstractSuperInterfaces(ReferenceType rt) {
        List<ReferenceType> superInterfaces = new LinkedList<>();
        superInterfaces.add(rt);

        @SuppressWarnings("unchecked")
        List<JL5ClassType> interfaces = (List<JL5ClassType>) rt.interfaces();
        for (JL5ClassType interf : interfaces) {
            if (interf.isRawClass()) {
                // it's a raw class, so use the erased version of it
                interf = (JL5ClassType) this.erasureType(interf);
            }
            superInterfaces.addAll(abstractSuperInterfaces(interf));
        }

        if (rt.superType() != null) {
            JL5ClassType c = (JL5ClassType) rt.superType().toClass();
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
    public MethodInstance findMethod(ReferenceType container, String name,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException {
        return findMethod(container,
                          name,
                          argTypes,
                          null,
                          currClass,
                          null,
                          fromClient);
    }

    @Override
    public MethodInstance findMethod(ReferenceType container,
            java.lang.String name, List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            Type expectedReturnType, boolean fromClient)
                    throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<? extends MethodInstance> acceptable =
                findAcceptableMethods(container,
                                      name,
                                      argTypes,
                                      typeArgs,
                                      currClass,
                                      expectedReturnType,
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

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes, ClassType currClass,
            boolean fromClient) throws SemanticException {
        return findConstructor(container,
                               argTypes,
                               Collections.<ReferenceType> emptyList(),
                               currClass,
                               fromClient);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            boolean fromClient) throws SemanticException {

        assert_(container);
        assert_(argTypes);

        List<ConstructorInstance> acceptable =
                findAcceptableConstructors(container,
                                           argTypes,
                                           typeArgs,
                                           currClass,
                                           fromClient);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                                        "No valid constructor found for "
                                                + container + "("
                                                + listToString(argTypes)
                                                + ").");
        }

        Collection<ConstructorInstance> maximal =
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

    @Override
    protected List<? extends ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            ClassType currClass, boolean fromClient) throws SemanticException {
        return this.findAcceptableConstructors(container,
                                               argTypes,
                                               Collections.<ReferenceType> emptyList(),
                                               currClass,
                                               fromClient);
    }

    /**
     * Populates the list acceptable with those MethodInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     * @throws SemanticException
     */
    protected List<ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            boolean fromClient) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        SemanticException error = null;

        // List of methods accessible from curClass that have valid method
        // calls without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible method
        List<ConstructorInstance> phase1methods = new ArrayList<>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion
        List<ConstructorInstance> phase2methods = new ArrayList<>();
        // List of methods accessible from curClass that have a valid method
        // call relying on boxing/unboxing conversion and variable arity
        List<ConstructorInstance> phase3methods = new ArrayList<>();

        if (Report.should_report(Report.types, 2))
            Report.report(2,
                          "Searching type " + container + " for constructor "
                                  + container + "(" + listToString(argTypes)
                                  + ")");
        @SuppressWarnings("unchecked")
        List<JL5ConstructorInstance> constructors =
                (List<JL5ConstructorInstance>) container.constructors();
        for (JL5ConstructorInstance ci : constructors) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "Trying " + ci);

            JL5ConstructorInstance substCi =
                    (JL5ConstructorInstance) callValid(ci,
                                                       argTypes,
                                                       actualTypeArgs);
            if (substCi != null) {
                ci = substCi;
                if (isAccessible(ci, currClass)) {
                    if (Report.should_report(Report.types, 3))
                        Report.report(3, "->acceptable: " + ci);
                    if (varArgsRequired(ci))
                        phase3methods.add(ci);
                    else if (boxingRequired(ci, argTypes))
                        phase2methods.add(ci);
                    else phase1methods.add(ci);
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
                                                          + " cannot be invoked with "
                                                          + (!actualTypeArgs.isEmpty()
                                                                  ? "type arguments <"
                                                                          + listToString(actualTypeArgs)
                                                                          + "> and "
                                                                  : "")
                                                          + "arguments " + "("
                                                          + listToString(argTypes)
                                                          + ").");
                }
            }
        }

        if (!phase1methods.isEmpty()) return phase1methods;
        if (!phase2methods.isEmpty()) return phase2methods;
        if (!phase3methods.isEmpty()) return phase3methods;

        if (error == null) {
            error = new NoMemberException(NoMemberException.CONSTRUCTOR,
                                          "No valid constructor found for "
                                                  + container + "("
                                                  + listToString(argTypes)
                                                  + ").");
        }

        throw error;
    }

    @Override
    public boolean isMember(MemberInstance mi, ReferenceType type) {
        if (super.isMember(mi, type)) return true;
        if (mi.flags().isStatic()) {
            if (type instanceof JL5SubstClassType) {
                type = ((JL5SubstClassType) type).base();
            }
            else if (type instanceof RawClass) {
                type = ((RawClass) type).base();
            }
            return typeEquals(mi.container(), type);
        }
        return false;
    }

    @Override
    public boolean isAccessible(MemberInstance mi, ReferenceType container,
            ReferenceType contextType, boolean fromClient) {
        assert_(mi);

        Flags flags = mi.flags();

        if (container instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) container;
            return !flags.isPrivate()
                    && isAccessible(mi,
                                    tv.upperBound(),
                                    contextType,
                                    fromClient);
        }

        if (super.isAccessible(mi, container, contextType, fromClient))
            return true;

        if (flags.isProtected()) {
            // XXX Is there a more graceful way to deal with raw types?
            // Let C be the class in which a protected member is declared.
            // Access is permitted only within the body of a subclass S of C.
            Type targetClass = erasureType(mi.container().toClass());
            ReferenceType rt = contextType;
            if (contextType.isClass()) {
                ClassType ct = contextType.toClass();
                while (!isSubtype(ct, targetClass) && !ct.isTopLevel())
                    ct = ct.outer();
                rt = ct;
            }
            if (isSubtype(rt, targetClass)) {
                // Class and static members are accessible.
                if (mi instanceof ClassType || flags.isStatic()) return true;
                // In addition, for expressions of the form E.Id or E.Id(...),
                // access is permitted iff the type of E is S or a subclass of S.
                return !fromClient || isSubtype(container, rt);
            }
        }

        return false;
    }

    @Override
    public boolean isEnclosed(ClassType inner, ClassType outer) {
        if (inner instanceof JL5ClassType) {
            inner = (ClassType) inner.declaration();
        }
        if (outer instanceof JL5ClassType) {
            outer = (ClassType) outer.declaration();
        }
        return inner.isEnclosedImpl(outer);
    }

    @Override
    public boolean hasEnclosingInstance(ClassType inner, ClassType encl) {
        if (inner instanceof JL5ClassType) {
            inner = (ClassType) inner.declaration();
        }
        if (encl instanceof JL5ClassType) {
            encl = (ClassType) encl.declaration();
        }
        return inner.hasEnclosingInstanceImpl(encl);
    }

    @Override
    public WildCardType wildCardType(Position position) {
        return wildCardType(position, null, null);
    }

    @Override
    public WildCardType wildCardType(Position position,
            ReferenceType upperBound, ReferenceType lowerBound) {
        if (upperBound == null) {
            upperBound = Object();
        }
        return new WildCardType_c(this, position, upperBound, lowerBound);
    }

    public CaptureConvertedWildCardType captureConvertedWildCardType(
            Position pos) {
        return new CaptureConvertedWildCardType_c(this, pos);
    }

    @Override
    public Type applyCaptureConversion(Type t, Position pos)
            throws SemanticException {
        if (!(t instanceof JL5SubstClassType_c)) {
            return t;
        }
        JL5SubstClassType_c ct = (JL5SubstClassType_c) t;
        JL5ParsedClassType g = ct.base();

        Map<TypeVariable, ReferenceType> substmap = new LinkedHashMap<>();
        // If g is an inner class, need to examine the outer class.
        // first, set up a subst from the formals to the captured variables.
        for (JL5ParsedClassType cur = g; cur != null; cur =
                (JL5ParsedClassType) cur.outer()) {
            for (TypeVariable a : cur.typeVariables()) {
                ReferenceType ti = (ReferenceType) ct.subst().substType(a);
                ReferenceType si = ti;
                if (ti instanceof WildCardType) {
                    CaptureConvertedWildCardType tv =
                            captureConvertedWildCardType(ti.position());
                    tv.setSyntheticOrigin();
                    si = tv;
                }
                substmap.put(a, si);
            }
            if (!cur.isInnerClass()) break;
        }
        JL5Subst subst = (JL5Subst) this.subst(substmap);

        // now go through and substitute the bounds if needed.
        for (JL5ParsedClassType cur = g; cur != null; cur =
                (JL5ParsedClassType) cur.outer()) {
            for (TypeVariable a : cur.typeVariables()) {
                Type ti = ct.subst().substType(a);
                Type si = subst.substType(a);
                if (ti instanceof WildCardType) {
                    WildCardType wti = (WildCardType) ti;
                    TypeVariable vsi = (TypeVariable) si;
                    if (wti.isExtendsConstraint()) {
                        ReferenceType wub = wti.upperBound();
                        ReferenceType substUpperBoundOfA =
                                (ReferenceType) subst.substType(a.upperBound());
                        ReferenceType glb;
                        if (typeEquals(wub, substUpperBoundOfA))
                            glb = wub;
                        else glb = this.glb(wub, substUpperBoundOfA, false);
                        vsi.setUpperBound(glb);
                        if (wub.isClass()
                                && !wub.toClass().flags().isInterface()
                                && substUpperBoundOfA.isClass()
                                && !substUpperBoundOfA.toClass()
                                                      .flags()
                                                      .isInterface()) {
                            // check that wub is a subtype of substUpperBoundOfA, or vice versa
                            // JLS 3rd ed 5.1.10
                            if (!isSubtype(wub, substUpperBoundOfA)
                                    && !isSubtype(substUpperBoundOfA, wub)) {
                                throw new SemanticException("Cannot capture convert "
                                        + t, pos);
                            }
                        }
                    }
                    else {
                        // wti is a super wildcard.
                        vsi.setUpperBound((ReferenceType) subst.substType(a.upperBound()));
                        vsi.setLowerBound(wti.lowerBound());
                    }

                }
            }
        }

        return subst.substType(g);
    }

    @Override
    public Flags legalLocalFlags() {
        return JL5Flags.setVarArgs(super.legalLocalFlags());
    }

    @Override
    public Flags legalConstructorFlags() {
        return JL5Flags.setVarArgs(super.legalConstructorFlags());
    }

    @Override
    public Flags legalMethodFlags() {
        return JL5Flags.setVarArgs(super.legalMethodFlags());
    }

    @Override
    public Flags legalAbstractMethodFlags() {
        return JL5Flags.setVarArgs(super.legalAbstractMethodFlags());
    }

    @Override
    public JL5SubstClassType findGenericSupertype(JL5ParsedClassType base,
            ReferenceType sub) {
        List<ReferenceType> ancestors = allAncestorsOf(sub);
        for (ReferenceType a : ancestors) {
            if (!(a instanceof JL5SubstClassType)) {
                continue;
            }
            JL5SubstClassType instantiatedType = (JL5SubstClassType) a;
            JL5ParsedClassType instBase = instantiatedType.base();

            if (typeEquals(base, instBase)) {
                return instantiatedType;
            }
        }
        return null;
    }

    @Override
    public ReferenceType intersectionType(Position pos,
            List<ReferenceType> types) {
        if (types.size() == 1) {
            return types.get(0);
        }
        if (types.isEmpty()) {
            return Object();
        }

        return new IntersectionType_c(this, pos, types);
    }

    @Override
    public boolean checkIntersectionBounds(List<? extends Type> bounds,
            boolean quiet) throws SemanticException {
        /*        if ((bounds == null) || (bounds.size() == 0)) {
        if (!quiet)
        throw new SemanticException("Intersection type can't be empty");
        return false;
        }*/
        List<Type> concreteBounds = concreteBounds(bounds);
        if (concreteBounds.size() == 0) {
            if (!quiet)
                throw new SemanticException("Invalid bounds in intersection type.");
            else return false;
        }
        for (int i = 0; i < concreteBounds.size(); i++)
            for (int j = i + 1; j < concreteBounds.size(); j++) {
                Type t1 = concreteBounds.get(i);
                Type t2 = concreteBounds.get(j);
                // for now, no checks if at least one is an array type
                if (!t1.isClass() || !t2.isClass()) {
                    return true;
                }
                if (!t1.toClass().flags().isInterface()
                        && !t2.toClass().flags().isInterface()) {
                    if (!isSubtype(t1, t2) && !isSubtype(t2, t1)) {
                        if (!quiet)
                            throw new SemanticException("Error in intersection type. Types "
                                    + t1 + " and " + t2
                                    + " are not in subtype relation.");
                        else return false;
                    }
                }
                if (t1.toClass().flags().isInterface()
                        && t2.toClass().flags().isInterface()
                        && t1 instanceof JL5SubstClassType
                        && t2 instanceof JL5SubstClassType) {
                    JL5SubstClassType j5t1 = (JL5SubstClassType) t1;
                    JL5SubstClassType j5t2 = (JL5SubstClassType) t2;
                    if (j5t1.base().equals(j5t2.base()) && !j5t1.equals(j5t2)) {
                        if (!quiet) {
                            throw new SemanticException("Error in intersection type. Interfaces "
                                    + j5t1 + " and " + j5t2
                                    + "are instantiations of the same generic interface but with different type arguments");
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
        return true;
    }

    //@Override
    public List<Type> concreteBounds(List<? extends Type> bounds) {

        Set<Type> included = new LinkedHashSet<>();
        Set<Type> visited = new LinkedHashSet<>();
        List<Type> queue = new ArrayList<>(bounds);
        while (!queue.isEmpty()) {
            Type t = queue.remove(0);
            if (visited.contains(t)) continue;
            visited.add(t);
            if (t instanceof TypeVariable) {
                TypeVariable tv = (TypeVariable) t;
                queue.add(tv.upperBound());
            }
            else if (t instanceof IntersectionType) {
                IntersectionType it = (IntersectionType) t;
                queue.addAll(it.bounds());
            }
            else {
                included.add(t);
            }
        }
        return new ArrayList<>(included);
    }

    @Override
    public ReferenceType glb(ReferenceType t1, ReferenceType t2) {
        return this.glb(t1, t2, true);
    }

    protected ReferenceType glb(ReferenceType t1, ReferenceType t2,
            boolean performIntersectionCheck) {
        List<ReferenceType> l = new ArrayList<>();
        l.add(t1);
        l.add(t2);
        return glb(Position.compilerGenerated(), l, performIntersectionCheck);
    }

    @Override
    public ReferenceType glb(Position pos, List<ReferenceType> bounds) {
        return this.glb(pos, bounds, true);
    }

    protected ReferenceType glb(Position pos, List<ReferenceType> bounds,
            boolean performIntersectionCheck) {
        if (bounds == null || bounds.isEmpty()) {
            return Object();
        }
        try {
            // XXX also need to check that does not have two classes that are not in a subclass relation?
            if (performIntersectionCheck
                    && !checkIntersectionBounds(bounds, true)) {
                return Object();
            }
            else {
                return intersectionType(pos, bounds);
            }
        }
        catch (SemanticException e) {
            return Object();
        }
    }

    @Override
    public UnknownReferenceType unknownReferenceType(Position position) {
        return unknownReferenceType;
    }

    protected UnknownReferenceType unknownReferenceType =
            new UnknownReferenceType_c(this);

    @Override
    public RawClass rawClass(JL5ParsedClassType base) {
        return this.rawClass(base, base.position());
    }

    @Override
    public RawClass rawClass(JL5ParsedClassType base, Position pos) {
        if (!canBeRaw(base)) {
            throw new InternalCompilerError("Can only create a raw class with a parameterized class");
        }
        return new RawClass_c(base, pos);
    }

    @Override
    public boolean canBeRaw(Type type) {
        if (type instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType) type;
            if (!pct.typeVariables().isEmpty()) return true;
            ClassType outer = pct.outer();
            if (outer != null) return canBeRaw(outer);
        }
        return false;
    }

    @Override
    public Type toRawType(Type t) {
        if (!t.isReference()) {
            return t;
        }
        if (t instanceof RawClass) {
            return t;
        }
        if (t instanceof JL5ParsedClassType) {
            JL5ParsedClassType ct = (JL5ParsedClassType) t;
            if (!classAndEnclosingTypeVariables(ct).isEmpty()) {
                return this.rawClass(ct, ct.position());
            }
            else {
                // neither t nor its containers has type variables
                return t;
            }
        }
        if (t instanceof ArrayType) {
            ArrayType at = t.toArray();
            Type b = toRawType(at.base());
            return at.base(b);
        }
        return t;
    }

    /**
     * Does pct, or a containing class of pct, have type variables?
     */
    @Override
    public List<TypeVariable> classAndEnclosingTypeVariables(
            JL5ParsedClassType ct) {
        List<TypeVariable> l = new ArrayList<>();
        classAndEnclosingTypeVariables(ct, l);
        return l;
    }

    protected void classAndEnclosingTypeVariables(JL5ParsedClassType ct,
            List<TypeVariable> l) {

        if (!ct.typeVariables().isEmpty()) {
            l.addAll(ct.typeVariables());
        }
        if (!ct.isTopLevel() && ct.isNested() && ct.isInnerClass()) {
            // either we are an inner class, or we are a nested class, and we want to include the
            // container
            if (ct.outer() instanceof JL5ParsedClassType) {
                classAndEnclosingTypeVariables((JL5ParsedClassType) ct.outer(),
                                               l);
            }
        }
    }

    @Override
    public PrimitiveType promote(Type t1, Type t2) throws SemanticException {
        return super.promote(unboxingConversion(t1), unboxingConversion(t2));
    }

    @Override
    public Type boxingConversion(Type t) {
        if (t.isPrimitive()) {
            return wrapperClassOfPrimitive(t.toPrimitive());
        }
        return t;
    }

    @Override
    public Type unboxingConversion(Type t) {
        Type s = primitiveTypeOfWrapper(t);
        if (s != null) {
            return s;
        }
        return t;
    }

    @Override
    public LubType lub(Position pos, List<ReferenceType> us) {
        return new LubType_c(this, pos, us);
    }

    @Override
    public boolean isValidAnnotationValueType(Type t) {
        // must be one of primitive, String, Class, enum, annotation or
        // array of one of these
        if (t.isPrimitive()) return true;
        if (t.isClass()) {
            if (JL5Flags.isEnum(t.toClass().flags())
                    || JL5Flags.isAnnotation(t.toClass().flags())
                    || String().equals(t) || Class().equals(t)) {
                return true;
            }
        }
        // XXX More elegant way to check that t is a parameterized invocation of Class?
        // See JLS 3rd ed. 9.6, clarified in JLS SE7 ed. 9.6.1
        if (erasureType(Class()).equals(erasureType(t))) {
            return true;
        }
        if (t.isArray()) {
            return isValidAnnotationValueType(t.toArray().base());
        }
        return false;
    }

    @Override
    public void checkAnnotationValueConstant(Term value)
            throws SemanticException {
        if (value instanceof ElementValueArrayInit) {
            // check elements
            for (Term next : ((ElementValueArrayInit) value).elements()) {
                if (!isAnnotationValueConstant(next)) {
                    throw new SemanticException("Annotation attribute value must be constant",
                                                next.position());
                }
            }
        }
        else if (value instanceof AnnotationElem) {
            return;
        }
        else if (!isAnnotationValueConstant(value)) {
            throw new SemanticException("Annotation attribute value must be constant: "
                    + value, value.position());
        }
    }

    protected boolean isAnnotationValueConstant(Term value) {
        if (value == null || value instanceof NullLit
                || value instanceof ClassLit) {
            // for purposes of annotation elems class lits are constants
            // we're ok, try the next one.
            return true;
        }
        if (value instanceof Expr) {
            JLang lang = J5Lang_c.instance;
            Expr ev = (Expr) value;
            if (lang.constantValueSet(ev, lang) && lang.isConstant(ev, lang)) {
                // value is a constant
                return true;
            }
            if (ev instanceof EnumConstant) {
                // Enum constants are constants for our purposes.
                return true;
            }
            if (!lang.constantValueSet(ev, lang)) {
                // the constant value hasn't been set yet...
                return true; // TODO: should this throw a missing dependency exception?
            }
        }
        return false;

    }

    @Override
    public void checkDuplicateAnnotations(List<AnnotationElem> annotations)
            throws SemanticException {
        // check no duplicate annotations used
        ArrayList<AnnotationElem> l = new ArrayList<>(annotations);
        for (int i = 0; i < l.size(); i++) {
            AnnotationElem ai = l.get(i);
            for (int j = i + 1; j < l.size(); j++) {
                AnnotationElem aj = l.get(j);
                if (ai.typeName().type() == aj.typeName().type()) {
                    throw new SemanticException("Duplicate annotation use: "
                            + aj.typeName(), aj.position());
                }
            }
        }
    }

    @Override
    public AnnotationTypeElemInstance annotationElemInstance(Position pos,
            ClassType ct, Flags f, Type type, java.lang.String name,
            boolean hasDefault) {
        assert_(ct);
        assert_(type);
        return new AnnotationTypeElemInstance_c(this,
                                                pos,
                                                ct,
                                                f,
                                                type,
                                                name,
                                                hasDefault);
    }

    @Override
    public AnnotationTypeElemInstance findAnnotation(ReferenceType container,
            String name, ClassType currClass) throws SemanticException {
        Set<AnnotationTypeElemInstance> annotations =
                findAnnotations(container, name);
        if (annotations.size() == 0) {
            throw new NoMemberException(JL5NoMemberException.ANNOTATION,
                                        "Annotation: \"" + name
                                                + "\" not found in type \""
                                                + container + "\".");
        }
        Iterator<AnnotationTypeElemInstance> i = annotations.iterator();
        AnnotationTypeElemInstance ai = i.next();

        if (i.hasNext()) {
            AnnotationTypeElemInstance ai2 = i.next();

            throw new SemanticException("Annotation \"" + name
                    + "\" is ambiguous; it is defined in both " + ai.container()
                    + " and " + ai2.container() + ".");
        }

        if (currClass != null && !isAccessible(ai, currClass)
                && !isInherited(ai, currClass)) {
            throw new SemanticException("Cannot access " + ai + ".");
        }
        return ai;
    }

    public Set<AnnotationTypeElemInstance> findAnnotations(
            ReferenceType container, String name) {
        assert_(container);
        if (container == null) {
            throw new InternalCompilerError("Cannot access annotation \"" + name
                    + "\" within a null container type.");
        }
        AnnotationTypeElemInstance ai =
                ((JL5ParsedClassType) container).annotationElemNamed(name);

        if (ai != null) {
            return Collections.singleton(ai);
        }

        return new HashSet<>();
    }

    @Override
    public void checkMethodNameClash(JL5MethodInstance mi, ClassType ct)
            throws SemanticException {
        checkMethodNameClash(mi, ct, ct);
    }

    public void checkMethodNameClash(JL5MethodInstance mi, ClassType type,
            ReferenceType declaringClass) throws SemanticException {
        for (MethodInstance mj_ : declaringClass.methods()) {
            JL5MethodInstance mj = (JL5MethodInstance) mj_;
            if (!mi.name().equals(mj.name())) continue;
            if (!isAccessible(mj, type)) continue;
            if (isSubSignature(mi, mj)) continue;

            for (MethodInstance imi : implemented(mi))
                for (MethodInstance imj : implemented(mj))
                    if (hasSameErasure((JL5MethodInstance) imi,
                                       (JL5MethodInstance) imj)) {
                        throw new SemanticException("Name clash: The method "
                                + imi.signature() + " of type "
                                + imi.container() + " has the same erasure as "
                                + imj.signature() + " of type "
                                + imj.container()
                                + " but does not override it");
                    }
        }

        Type superType = declaringClass.superType();
        if (superType != null)
            checkMethodNameClash(mi, type, superType.toReference());
        for (ReferenceType superInterface : declaringClass.interfaces())
            checkMethodNameClash(mi, type, superInterface);
    }

    protected boolean hasSameErasure(JL5MethodInstance mi,
            JL5MethodInstance mj) {
        if (!mi.name().equals(mj.name())) return false;
        if (mi.formalTypes().size() != mj.formalTypes().size()) return false;
        // now check that the types match
        mi = (JL5MethodInstance) mi.declaration();
        mj = (JL5MethodInstance) mj.declaration();
        Iterator<? extends Type> typesi = mi.formalTypes().iterator();
        Iterator<? extends Type> typesj = mj.formalTypes().iterator();
        while (typesi.hasNext()) {
            Type ti = erasureType(typesi.next());
            Type tj = erasureType(typesj.next());
            if (!ti.equals(tj)) return false;
        }
        return true;
    }

    @Override
    protected boolean returnTypesConsistent(MethodInstance mi,
            MethodInstance mj) {
        // See JLS 3rd Ed. | 8.4.8.4.
        // One method must be return-type-substitutable for the other.
        Type miRet = mi.returnType();
        Type mjRet = mj.returnType();
        return areReturnTypeSubstitutable(miRet, mjRet)
                || areReturnTypeSubstitutable(mjRet, miRet);
    }

    @Override
    public Type Class(Position pos, ReferenceType type) {
        try {
            return this.instantiate(pos,
                                    (JL5ParsedClassType) this.Class(),
                                    Collections.singletonList(type));
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Couldn't create class java.lang.Class<"
                    + type + ">", e);
        }
    }

    @Override
    public boolean isReifiable(Type t) {
        if (t.isPrimitive() || t.isNull()) {
            return true;
        }
        if (t instanceof RawClass) {
            return isContainerReifiable(t.toClass());
        }
        if (t instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType) t;
            return pct.typeVariables().isEmpty()
                    && isContainerReifiable(t.toClass());
        }
        if (t instanceof ArrayType) {
            return isReifiable(((ArrayType) t).base());
        }
        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType ct = (JL5SubstClassType) t;
            for (ReferenceType a : ct.actuals()) {
                if (a instanceof WildCardType) {
                    WildCardType wc = (WildCardType) a;
                    if (!wc.hasLowerBound()
                            && wc.upperBound().equals(Object())) {
                        // the actual is an unbounded wildcard, and so is reifiable
                        continue;
                    }
                }
                // actual a is not an unbounded wildcard. This type is not reifiable.
                return false;
            }
            return isContainerReifiable(t.toClass());
        }
        return false;
    }

    /**
     * If ct is an inner class, then check that the container of ct is reifiable.
     * @param ct
     * @return
     */
    private boolean isContainerReifiable(ClassType ct) {
        if (!ct.isInnerClass()) {
            // we don't care if the container is reifiable
            return true;
        }
        return isReifiable(ct.container());
    }

    @Override
    public ClassType instantiateInnerClassFromContext(Context c, ClassType ct) {
        ReferenceType outer = ct.outer();
        // Find the container in the context
        while (c != null) {
            ClassType fromCtx = c.currentClass();
            while (fromCtx != null) {
                if (fromCtx instanceof JL5SubstClassType) {
                    JL5SubstClassType sct = (JL5SubstClassType) fromCtx;
                    ClassType rawCT = sct.base();
                    if (outer.equals(rawCT)) {
                        return (ClassType) sct.subst().substType(ct);
                    }
                }
                else if (fromCtx instanceof JL5ParsedClassType) {
                    if (outer.equals(fromCtx)) {
                        // nothing to substitute
                        return ct;
                    }
                }
                fromCtx = (ClassType) fromCtx.superType();
            }
            c = c.pop();
        }
        // XXX: Couldn't find container, why are we here?
        return ct;
//        throw new InternalCompilerError(
//                "Could not find container of inner class "
//                        + ct + " in current context");
    }

    @Override
    public Annotations createAnnotations(
            Map<Type, Map<java.lang.String, AnnotationElementValue>> annotationElems,
            Position pos) {
        return new Annotations_c(annotationElems, this, pos);
    }

    /**
     * Given an annotation of type annotationType, should the annotation
     * be retained in the binary? See JLS 3rd ed, 9.6.1.2
     */
    @Override
    public boolean isRetainedAnnotation(Type annotationType) {
        if (annotationType.isClass()
                && annotationType.toClass().isSubtype(Annotation())) {
            // well, it's an annotation type at least.
            // check if there is a retention policy on it.
            JL5ClassType ct = (JL5ClassType) annotationType.toClass();
            Annotations ra = ct.annotations();
            if (ra == null) {
                // by default, use RetentionPolicy.CLASS
                return true;
            }
            AnnotationElementValue v = ra.singleElement(RetentionAnnotation());
            if (v == null) {
                // by default, use RetentionPolicy.CLASS
                return true;
            }
            if (v instanceof AnnotationElementValueConstant) {
                AnnotationElementValueConstant c =
                        (AnnotationElementValueConstant) v;
                EnumInstance ei = (EnumInstance) c.constantValue();
                if (ei.name().equalsIgnoreCase("CLASS")
                        || ei.name().equalsIgnoreCase("RUNTIME")) {
                    return true;
                }
                else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Annotations NoAnnotations() {
        return new Annotations_c(this, Position.compilerGenerated(1));
    }

    @Override
    public AnnotationElementValueArray AnnotationElementValueArray(Position pos,
            List<AnnotationElementValue> vals) {
        return new AnnotationElementValueArray_c(this, pos, vals);
    }

    @Override
    public AnnotationElementValueAnnotation AnnotationElementValueAnnotation(
            Position pos, Type annotationType,
            Map<String, AnnotationElementValue> annotationElementValues) {
        return new AnnotationElementValueAnnotation_c(this,
                                                      pos,
                                                      annotationType,
                                                      annotationElementValues);
    }

    @Override
    public AnnotationElementValueConstant AnnotationElementValueConstant(
            Position pos, Type type, Object constVal) {
        return new AnnotationElementValueConstant_c(this, pos, type, constVal);
    }

    @Override
    public Type leastCommonAncestor(Type type1, Type type2)
            throws SemanticException {
        if (type1.isPrimitive() && (type2.isReference() || type2.isNull())) {
            // box type1, i.e. promote to an object
            return leastCommonAncestor(boxingConversion(type1), type2);
        }
        if (type2.isPrimitive() && (type1.isReference() || type1.isNull())) {
            // box type2, i.e. promote to an object
            return leastCommonAncestor(type1, boxingConversion(type2));
        }
        return super.leastCommonAncestor(type1, type2);
    }
}
