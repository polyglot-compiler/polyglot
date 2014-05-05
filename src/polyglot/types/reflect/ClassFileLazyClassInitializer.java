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

package polyglot.types.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import polyglot.main.Report;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.InnerClasses.Info;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StringUtil;

/**
 * ClassFile basically represents a Java classfile as it is found on disk. The
 * classfile is modeled according to the Java Virtual Machine Specification.
 * Methods are provided to edit the classfile at a very low level.
 * 
 * @see polyglot.types.reflect Attribute
 * @see polyglot.types.reflect Constant
 * @see polyglot.types.reflect Field
 * @see polyglot.types.reflect Method
 * 
 * @author Nate Nystrom
 */
public class ClassFileLazyClassInitializer implements LazyClassInitializer {
    protected ClassFile clazz;
    protected TypeSystem ts;
    protected ParsedClassType ct;

    protected boolean init;
    protected boolean constructorsInitialized;
    protected boolean fieldsInitialized;
    protected boolean interfacesInitialized;
    protected boolean memberClassesInitialized;
    protected boolean methodsInitialized;
    protected boolean superclassInitialized;

    protected static Collection<String> verbose;
    static {
        verbose = new HashSet<>();
        verbose.add("loader");
    }

    public ClassFileLazyClassInitializer(ClassFile file, TypeSystem ts) {
        this.clazz = file;
        this.ts = ts;
    }

    @Override
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

    @Override
    public boolean fromClassFile() {
        return true;
    }

    /**
     * Create a position for the class file.
     */
    public Position position() {
        return new Position(null, clazz.name() + ".class");
    }

    /**
     * Create the type for this class file.
     */
    protected ParsedClassType createType() throws SemanticException {
        // The name is of the form "p.q.C$I$J".
        String name = clazz.classNameCP(clazz.getThisClass());

        if (Report.should_report(verbose, 2))
            Report.report(2, "creating ClassType for " + name);

        // Create the ClassType.
        ParsedClassType ct = ts.createClassType(this);
        // 0x0020 is rather ACC_SUPER class property flag, for backward
        // compatibility.  This bit should be ignored.
        ct.flags(ts.flagsForBits(clazz.getModifiers()).clearSynchronized());
        ct.position(position());

        // This is the "p.q" part.
        String packageName = StringUtil.getPackageComponent(name);

        // Set the ClassType's package.
        if (!packageName.equals("")) {
            ct.package_(ts.packageForName(packageName));
        }

        // This is the "C$I$J" part.
        String className = StringUtil.getShortNameComponent(name);

        ClassType.Kind kind = ClassType.TOP_LEVEL;

        // For member classes, set the proper access flags, which are the
        // modifier bits found in the InnerClass attribute.
        InnerClasses innerClasses = clazz.getInnerClasses();
        if (innerClasses != null && className.lastIndexOf('$') >= 0) {
            for (Info c : innerClasses.getClasses()) {
                if (c.classIndex == clazz.getThisClass() && c.classIndex != 0) {
                    ct.flags(ts.flagsForBits(c.modifiers));

                    if (c.nameIndex == 0) {
                        // anonymous class
                        kind = ClassType.ANONYMOUS;
                    }
                    else {
                        // This will be "p.q.C$I"
                        String outerName = clazz.classNameCP(c.outerClassIndex);
                        // This will be "J"
                        className =
                                (String) clazz.getConstants()[c.nameIndex].value();

                        // Load the outer class.
                        // This will recursively load its outer class, if any.
                        if (Report.should_report(verbose, 2))
                            Report.report(2, "resolving " + outerName + " for "
                                    + name);
                        ClassType outer = this.typeForName(outerName);
                        ClassType.Kind outerKind = ct.kind();
                        if (outerKind == ClassType.ANONYMOUS)
                            kind = ClassType.LOCAL;
                        else kind = ClassType.MEMBER;
                        ct.outer(outer);
                    }

                    break;
                }
            }
        }

        if (Report.should_report(verbose, 3))
            Report.report(3, name + " is " + kind);

        ct.name(className);
        ct.kind(kind);

        // Add unresolved class into the cache to avoid circular resolving.
        ts.systemResolver().addNamed(name, ct);
        ts.systemResolver().addNamed(ct.fullName(), ct);

        return ct;
    }

    /**
     * Create the type for this class file.
     */
    public ParsedClassType type() throws SemanticException {
        ParsedClassType ct = createType();
        return ct;
    }

    /**
     * Return an array type.
     * @param t The array base type.
     * @param dims The number of dimensions of the array.
     * @return An array type.
     */
    protected Type arrayOf(Type t, int dims) {
        if (dims == 0) {
            return t;
        }
        else {
            return ts.arrayOf(t, dims);
        }
    }

    /**
     * Return a list of types based on a JVM type descriptor string.
     * @param str The type descriptor.
     * @return The corresponding list of types.
     */
    protected List<Type> typeListForString(String str) {
        List<Type> types = new ArrayList<>();

        for (int i = 0; i < str.length(); i++) {
            int dims = 0;

            while (str.charAt(i) == '[') {
                dims++;
                i++;
            }

            switch (str.charAt(i)) {
            case 'Z':
                types.add(arrayOf(ts.Boolean(), dims));
                break;
            case 'B':
                types.add(arrayOf(ts.Byte(), dims));
                break;
            case 'S':
                types.add(arrayOf(ts.Short(), dims));
                break;
            case 'C':
                types.add(arrayOf(ts.Char(), dims));
                break;
            case 'I':
                types.add(arrayOf(ts.Int(), dims));
                break;
            case 'J':
                types.add(arrayOf(ts.Long(), dims));
                break;
            case 'F':
                types.add(arrayOf(ts.Float(), dims));
                break;
            case 'D':
                types.add(arrayOf(ts.Double(), dims));
                break;
            case 'V':
                types.add(arrayOf(ts.Void(), dims));
                break;
            case 'L': {
                int start = ++i;
                while (i < str.length()) {
                    if (str.charAt(i) == ';') {
                        String s = str.substring(start, i);
                        s = s.replace('/', '.');
                        types.add(arrayOf(this.quietTypeForName(s), dims));
                        break;
                    }

                    i++;
                }
            }
            }
        }

        if (Report.should_report(verbose, 4))
            Report.report(4, "parsed \"" + str + "\" -> " + types);

        return types;
    }

    /**
     * Return a type based on a JVM type descriptor string. 
     * @param str The type descriptor.
     * @return The corresponding type.
     */
    public Type typeForString(String str) {
        List<Type> l = typeListForString(str);

        if (l.size() == 1) {
            return l.get(0);
        }

        throw new InternalCompilerError("Bad type string: \"" + str + "\"");
    }

    /**
     * Looks up a class by name, assuming the class exists.
     * @param name Name of the class to find.
     * @return A ClassType with the given name.
     * @throws InternalCompilerError if the class does not exist.
     */
    protected ClassType quietTypeForName(String name) {
        if (Report.should_report(verbose, 2))
            Report.report(2, "resolving " + name);

        try {
            return (ClassType) ts.systemResolver().find(name);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("could not load " + name, e);
        }
    }

    /**
     * Looks up a class by name.
     * @param name Name of the class to find.
     * @return A ClassType with the given name.
     * @throws SemanticException if the class does not exist.
     */
    protected ClassType typeForName(String name) throws SemanticException {
        if (Report.should_report(verbose, 2))
            Report.report(2, "resolving " + name);
        return (ClassType) ts.systemResolver().find(name);
    }

    @Override
    public void initTypeObject() {
        this.init = true;
    }

    @Override
    public boolean isTypeObjectInitialized() {
        return this.init;
    }

    @Override
    public void initSuperclass() {
        if (superclassInitialized) {
            return;
        }

        if (ts.equals(ct, ts.Object())) {
            ct.superType(null);
        }
        else {
            String superName = clazz.classNameCP(clazz.getSuperClass());

            ClassType superType =
                    superName == null
                            ? ts.Object() : quietTypeForName(superName);
            // For an interface, the value of the super_class item must always
            // be a valid index into the constant_pool table. The constant_pool
            // entry at that index must be a CONSTANT_Class_info structure
            // representing the class Object.
            // See JVMS 2nd Ed. | 4.1.
            if (ct.flags().isInterface()) {
                if (!ts.typeEquals(superType, ts.Object()))
                    throw new ClassFormatError("The superclass of an interface is not Object.");
            }
            else ct.superType(superType);
        }

        superclassInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initInterfaces() {
        if (interfacesInitialized) {
            return;
        }

        int[] interfaces = clazz.getInterfaces();
        for (int interface1 : interfaces) {
            String name = clazz.classNameCP(interface1);
            ct.addInterface(quietTypeForName(name));
        }

        interfacesInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initMemberClasses() {
        if (memberClassesInitialized) {
            return;
        }

        InnerClasses innerClasses = clazz.getInnerClasses();

        if (innerClasses != null) {
            for (int i = 0; i < innerClasses.getClasses().length; i++) {
                Info c = innerClasses.getClasses()[i];

                if (c.outerClassIndex == clazz.getThisClass()
                        && c.classIndex != 0) {
                    String name = clazz.classNameCP(c.classIndex);

                    int index = name.lastIndexOf('$');

                    // Skip local and anonymous classes.
                    if (index >= 0 && Character.isDigit(name.charAt(index + 1))) {
                        continue;
                    }

                    // A member class of this class
                    ClassType t = quietTypeForName(name);

                    if (t.isMember()) {
                        if (Report.should_report(verbose, 3))
                            Report.report(3, "adding member " + t + " to " + ct);

                        ct.addMemberClass(t);

                        // Set the access flags of the member class
                        // using the modifier bits of the InnerClass attribute.
                        // The flags in the class file for the member class are
                        // not correct! Stupid Java.
                        if (t instanceof ParsedClassType) {
                            ParsedClassType pt = (ParsedClassType) t;
                            pt.flags(ts.flagsForBits(c.modifiers));
                        }
                    }
                    else {
                        throw new InternalCompilerError(name
                                + " should be a member class.");
                    }
                }
            }
        }

        memberClassesInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void canonicalFields() {
        initFields();
    }

    @Override
    public void canonicalMethods() {
        initMethods();
    }

    @Override
    public void canonicalConstructors() {
        initConstructors();
    }

    @Override
    public void initFields() {
        if (fieldsInitialized) {
            return;
        }

        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].name().startsWith("jlc$")
                    && !fields[i].isSynthetic()) {
                FieldInstance fi = this.fieldInstance(fields[i], ct);
                if (Report.should_report(verbose, 3))
                    Report.report(3, "adding " + fi + " to " + ct);
                ct.addField(fi);
            }
        }

        fieldsInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initMethods() {
        if (methodsInitialized) {
            return;
        }

        Method[] methods = clazz.getMethods();
        List<MethodInstance> declaredMethods = new ArrayList<>(methods.length);
        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].name().equals("<init>")
                    && !methods[i].name().equals("<clinit>")
                    && !methods[i].isSynthetic() //  && !methods[i].isBridge()
            ) {
                MethodInstance mi = this.methodInstance(methods[i], ct);
                if (Report.should_report(verbose, 3))
                    Report.report(3, "adding " + mi + " to " + ct);
                declaredMethods.add(mi);
                ct.addMethod(mi);
            }
        }

        if (ct.flags().isInterface() && ct.interfaces().isEmpty()) {
            // See JLS 2nd Ed. | 9.2.
            // If an interface has no direct superinterfaces, then the interface
            // implicitly declares a public abstract member method corresponding
            // to each public instance method declared in Object, unless a
            // method with the same signature, same return type, and a
            // compatible throws clause is explicitly declared by the interface.
            List<? extends MethodInstance> objectMethods =
                    ts.Object().methods();
            List<MethodInstance> implicitlyDeclaredMethods =
                    new ArrayList<>(objectMethods.size());
            for (MethodInstance mi : objectMethods) {
                Flags flags = mi.flags();
                if (!flags.isPublic()) continue;
                boolean methodNeeded = true;
                for (MethodInstance mj : declaredMethods) {
                    if (!mi.name().equals(mj.name())) continue;
                    if (!mi.formalTypes().equals(mj.formalTypes())) continue;
                    methodNeeded = false;
                    break;
                }
                if (methodNeeded)
                    implicitlyDeclaredMethods.add(mi.container(ct)
                                                    .flags(flags.Abstract()
                                                                .clearFinal()));
            }
            for (MethodInstance mi : implicitlyDeclaredMethods)
                ct.addMethod(mi);
        }

        methodsInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initConstructors() {
        if (constructorsInitialized) {
            return;
        }

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].name().equals("<init>") && !methods[i].isSynthetic()) {
                ConstructorInstance ci =
                        this.constructorInstance(methods[i],
                                                 ct,
                                                 clazz.getFields());
                if (Report.should_report(verbose, 3))
                    Report.report(3, "adding " + ci + " to " + ct);
                ct.addConstructor(ci);
            }
        }

        constructorsInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    protected boolean initialized() {
        return superclassInitialized && interfacesInitialized
                && memberClassesInitialized && methodsInitialized
                && fieldsInitialized && constructorsInitialized;
    }

    /**
     * Create a MethodInstance.
     * @param method The JVM Method data structure.
     * @param ct The class containing the method.
     */
    protected MethodInstance methodInstance(Method method, ClassType ct) {
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[method.getName()].value();
        String type = (String) constants[method.getType()].value();

        if (type.charAt(0) != '(') {
            throw new ClassFormatError("Bad method type descriptor.");
        }

        int index = type.indexOf(')', 1);
        List<Type> argTypes = typeListForString(type.substring(1, index));
        Type returnType = typeForString(type.substring(index + 1));

        List<Type> excTypes = new ArrayList<>();

        Exceptions exceptions = method.getExceptions();
        if (exceptions != null) {
            int[] throwTypes = exceptions.getThrowTypes();
            for (int throwType : throwTypes) {
                String s = clazz.classNameCP(throwType);
                excTypes.add(quietTypeForName(s));
            }
        }

        return ts.methodInstance(ct.position(),
                                 ct,
                                 ts.flagsForBits(method.getModifiers()),
                                 returnType,
                                 name,
                                 argTypes,
                                 excTypes);
    }

    /**
     * Create a ConstructorInstance.
     * @param method The JVM Method data structure for the constructor.
     * @param ct The class containing the method.
     * @param fields The constructor's fields, needed to remove parameters
     * passed to initialize synthetic fields.
     */
    protected ConstructorInstance constructorInstance(Method method,
            ClassType ct, Field[] fields) {
        // Get a method instance for the <init> method.
        MethodInstance mi = methodInstance(method, ct);

        List<? extends Type> formals = mi.formalTypes();

        if (ct.isInnerClass()) {
            // If an inner class, the first argument may be a reference to an
            // enclosing class used to initialize a synthetic field.

            // Count the number of synthetic fields.
            int numSynthetic = 0;

            for (Field field : fields) {
                if (field.isSynthetic()) {
                    numSynthetic++;
                }
            }

            // Ignore a number of parameters equal to the number of synthetic
            // fields.
            if (numSynthetic <= formals.size()) {
                formals = formals.subList(numSynthetic, formals.size());
            }
        }

        return ts.constructorInstance(mi.position(),
                                      ct,
                                      mi.flags(),
                                      formals,
                                      mi.throwTypes());
    }

    /**
     * Create a FieldInstance.
     * @param field The JVM Field data structure for the field.
     * @param ct The class containing the field.
     */
    protected FieldInstance fieldInstance(Field field, ClassType ct) {
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[field.getName()].value();
        String type = (String) constants[field.getType()].value();

        FieldInstance fi =
                ts.fieldInstance(ct.position(),
                                 ct,
                                 ts.flagsForBits(field.getModifiers()),
                                 typeForString(type),
                                 name);

        if (field.isConstant()) {
            Constant c = field.constantValue();

            Object o = null;

            try {
                switch (c.tag()) {
                case Constant.STRING:
                    o = field.getString();
                    break;
                case Constant.INTEGER:
                    o = new Integer(field.getInt());
                    break;
                case Constant.LONG:
                    o = new Long(field.getLong());
                    break;
                case Constant.FLOAT:
                    o = new Float(field.getFloat());
                    break;
                case Constant.DOUBLE:
                    o = new Double(field.getDouble());
                    break;
                }
            }
            catch (SemanticException e) {
                throw new ClassFormatError("Unexpected constant pool entry.");
            }

            fi.setConstantValue(o);
            return fi;
        }
        else {
            fi.setNotConstant();
        }

        return fi;
    }

}
