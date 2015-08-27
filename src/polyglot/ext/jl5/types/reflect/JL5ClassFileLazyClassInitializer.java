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
package polyglot.ext.jl5.types.reflect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5FieldInstance;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.param.types.MuPClass;
import polyglot.main.Report;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.types.reflect.Constant;
import polyglot.types.reflect.Exceptions;
import polyglot.types.reflect.Field;
import polyglot.types.reflect.Method;

/**
 * XXX TODO
 * Enums
 * default annotation vals
 */
public class JL5ClassFileLazyClassInitializer extends
        ClassFileLazyClassInitializer implements JL5LazyClassInitializer {

    /**
     * Have the annotation elems (i.e., the method-like accessors for
     * values of annotations) been initialized?
     */
    protected boolean annotationElemsInitialized;

    /**
     * Have the annotation for the class been initialized?
     */
    protected boolean annotationsInitialized;
    /**
     * Have the enum constants for the class been initialized?
     */
    protected boolean enumConstantsInitialized;

    public JL5ClassFileLazyClassInitializer(ClassFile file, TypeSystem ts) {
        super(file, ts);
    }

    @Override
    protected boolean initialized() {
        return super.initialized() && annotationElemsInitialized
                && annotationsInitialized && enumConstantsInitialized;
    }

    /**
     * Create the type for this class file.
     */
    @Override
    protected ParsedClassType createType() throws SemanticException {
        // Create the ClassType.
        JL5ParsedClassType ct = (JL5ParsedClassType) super.createType();

        // System.err.println("Added " + name + " " + ct +
        // " to the system resolver.");

        JL5Signature signature = ((JL5ClassFile) clazz).getSignature();
        // Load the class signature
        // System.err.println("    signature == null? " + (signature == null));
        if (signature != null) {
            MuPClass<TypeVariable, ReferenceType> pc =
                    ((JL5TypeSystem) ts).mutablePClass(ct.position());
            ct.setPClass(pc);
            pc.clazz(ct);
            List<TypeVariable> typeVars =
                    signature.parseClassTypeVariables(ts, position());
            ct.setTypeVariables(typeVars);
            pc.formals(new ArrayList<>(ct.typeVariables()));

            signature.parseClassSignature(ts, position());

            // Set then read then set to force initialization of the classes
            // and interfaces so that they are initialized before we unify any
            // type variables
            ct.superType(signature.classSignature.superType());
            ct.setInterfaces(signature.classSignature.interfaces());

            ct.superType();
            ct.interfaces();

            ct.superType(signature.classSignature.superType());
            ct.setInterfaces(signature.classSignature.interfaces());

            /*
             * System.err.println("Class signature type for " + name);
             * System.err.println("    interfaces " +
             * signature.classSignature.interfaces());
             * System.err.println("    supertype " +
             * signature.classSignature.superType());
             * System.err.println("           " +
             * signature.classSignature.superType().getClass());
             * System.err.println("    typevars " +
             * signature.classSignature.typeVars());
             * System.err.println("  type vars " +
             * signature.classSignature.typeVars() + " for class " + name);
             * 
             * System.err.println("Class signature type for " + name);
             * System.err.println("          " + ct.getClass());
             * System.err.println("    interfaces " + ct.interfaces());
             * System.err.println("    supertype " + ct.superType());
             * System.err.println("           " + ct.superType().getClass());
             * System.err.println("    typevars " + ct.typeVariables());
             * System.err.println("  type vars " + ct.typeVariables() +
             * " for class " + name);
             */
        }
        else {
            // System.err.println("Class signature type for " + name +
            // ": null signature");
        }

        return ct;
    }

    @Override
    protected MethodInstance methodInstance(Method method_, ClassType ct) {
        JL5Method method = (JL5Method) method_;
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[method.getName()].value();
        String type = (String) constants[method.getType()].value();
        JL5Signature signature = method.getSignature();

        List<ReferenceType> excTypes = new ArrayList<>();

        // JL5 method signature does not contain the throw types
        // so parse that first, so we can use it in both cases.
        Exceptions exceptions = method.getExceptions();
        if (exceptions != null) {
            int[] throwTypes = exceptions.getThrowTypes();
            for (int throwType : throwTypes) {
                String s = clazz.classNameCP(throwType);
                excTypes.add(quietTypeForName(s));
            }
        }

        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        JL5MethodInstance mi;
        if (signature != null) {
            signature.parseMethodSignature(ts, position(), ct);

            List<ReferenceType> tt = signature.methodSignature.throwTypes();
            if (tt != null && !tt.isEmpty()) {
                // be robust in case for some reason the signature did include
                // throw types info
                excTypes = tt;
            }

            /*
             * System.err.println("Method signature type for " + name);
             * System.err.println("    returnType " +jl5RetType);
             * System.err.println("    formalTypes "
             * +signature.methodSignature.formalTypes);
             * System.err.println("    typevars "
             * +signature.methodSignature.typeVars);
             * System.err.println("    throwTypes " +excTypes);
             */
            mi =
                    ts.methodInstance(ct.position(), ct,
                    // VarArg flag (0x0080) is also transient flag,
                    // which should be cleared.
                                      ts.flagsForBits(method.getModifiers())
                                        .clearTransient(),
                                      signature.methodSignature.returnType(),
                                      name,
                                      signature.methodSignature.formalTypes(),
                                      excTypes,
                                      signature.methodSignature.typeVars());
        }
        else {
            // System.err.println("Method signature type for " + name +
            // " returnType: null signature");

            if (type.charAt(0) != '(') {
                throw new ClassFormatError("Bad method type descriptor.");
            }

            int index = type.indexOf(')', 1);
            List<Type> argTypes = typeListForString(type.substring(1, index));
            Type returnType = typeForString(type.substring(index + 1));

            mi =
                    (JL5MethodInstance) ts.methodInstance(ct.position(),
                                                          ct,
                                                          ts.flagsForBits(method.getModifiers()),
                                                          returnType,
                                                          name,
                                                          argTypes,
                                                          excTypes);
        }

        Map<Type, Map<String, AnnotationElementValue>> annotationElems =
                new LinkedHashMap<>();

        if (method.getRuntimeVisibleAnnotations() != null) {
            annotationElems.putAll(method.getRuntimeVisibleAnnotations()
                                         .toAnnotationElems(this, ts));
        }
        if (method.getRuntimeInvisibleAnnotations() != null) {
            annotationElems.putAll(method.getRuntimeInvisibleAnnotations()
                                         .toAnnotationElems(this, ts));
        }
        Annotations ann = ts.createAnnotations(annotationElems, ct.position());
        mi.setAnnotations(ann);
        return mi;
    }

//    @Override
//    protected ClassType quietTypeForName(String name) {
//        JL5ParsedClassType pct;
//        ClassType ct = super.quietTypeForName(name);
//        // If ct is a parameterized type, since we got here from a raw
//        // name we need to create a raw type
//        if (ct instanceof JL5ParsedClassType)
//            pct = (JL5ParsedClassType) ct;
//        else
//        // Only worried about ParsedClassTypes
//        return ct;
//
//        if (!pct.typeVariables().isEmpty()) {
//            return ((JL5TypeSystem) ts).rawClass(pct,
//                                                 Position.compilerGenerated());
//        }
//        return ct;
//    }

    @Override
    protected ConstructorInstance constructorInstance(Method method_,
            ClassType ct, Field[] fields) {
        JL5Method method = (JL5Method) method_;
        // Get a method instance for the <init> method.
        JL5MethodInstance mi = (JL5MethodInstance) methodInstance(method, ct);

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

        JL5ConstructorInstance ci =
                ((JL5TypeSystem) ts).constructorInstance(mi.position(),
                                                         ct,
                                                         mi.flags(),
                                                         formals,
                                                         mi.throwTypes(),
                                                         mi.typeParams());

        ci.setAnnotations(mi.annotations());

        return ci;
    }

    @Override
    protected FieldInstance fieldInstance(Field field_, ClassType ct) {
        JL5Field field = (JL5Field) field_;
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[field.getName()].value();
        String type = (String) constants[field.getType()].value();

        JL5TypeSystem ts = ((JL5TypeSystem) this.ts);

        JL5FieldInstance fi = null;
        JL5Signature signature = field.getSignature();
        Flags flags = ts.flagsForBits(field.getModifiers());
        Type fieldType;
        if (signature != null) {
            signature.parseFieldSignature(ts, position(), ct);
            fieldType = signature.fieldSignature.type;
        }
        else {
            fieldType = typeForString(type);
        }
        if (JL5Flags.isEnum(flags)) {
            fi = ts.enumInstance(ct.position(), ct, flags, name, 0);
        }
        else {
            fi =
                    (JL5FieldInstance) ts.fieldInstance(ct.position(),
                                                        ct,
                                                        flags,
                                                        fieldType,
                                                        name);
        }

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

        Map<Type, Map<String, AnnotationElementValue>> annotationElems =
                new LinkedHashMap<>();
        if (field.getRuntimeVisibleAnnotations() != null) {
            annotationElems.putAll(field.getRuntimeVisibleAnnotations()
                                        .toAnnotationElems(this, ts));
        }
        if (field.getRuntimeInvisibleAnnotations() != null) {
            annotationElems.putAll(field.getRuntimeInvisibleAnnotations()
                                        .toAnnotationElems(this, ts));
        }
        Annotations ann = ts.createAnnotations(annotationElems, ct.position());
        fi.setAnnotations(ann);

        return fi;
    }

    @Override
    public void initEnumConstants() {
        if (enumConstantsInitialized) {
            return;
        }
        // initialize fields first
        initFields();
        List<EnumInstance> enumInstances = new ArrayList<>();
        for (FieldInstance fi : this.ct.fields()) {
            if (JL5Flags.isEnum(fi.flags())) {
                EnumInstance ei = (EnumInstance) fi;
                enumInstances.add(ei);
                ((JL5ParsedClassType) ct).addEnumConstant(ei);
            }
        }

        // if we added enums, we need to set their ordinals.
        // TODO: XXX This is currently a hack. There is probably a better way to get the
        // ordinals for the enum instances.
        long ordinal = 0;
        for (EnumInstance ei : enumInstances) {
            ei.setOrdinal(ordinal);
            ordinal++;
        }

        enumConstantsInitialized = true;
        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initAnnotations() {
        if (annotationsInitialized) {
            return;
        }
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        Map<Type, Map<String, AnnotationElementValue>> annotationElems =
                new LinkedHashMap<>();
        JL5ClassFile cls = (JL5ClassFile) clazz;
        if (cls.getRuntimeVisibleAnnotations() != null) {
            annotationElems.putAll(cls.getRuntimeVisibleAnnotations()
                                      .toAnnotationElems(this, ts));
        }
        if (cls.getRuntimeInvisibleAnnotations() != null) {
            annotationElems.putAll(cls.getRuntimeInvisibleAnnotations()
                                      .toAnnotationElems(this, ts));
        }

        Annotations retAnn =
                ts.createAnnotations(annotationElems, ct.position());
        ((JL5ParsedClassType) ct).setAnnotations(retAnn);

        annotationsInitialized = true;
        if (initialized()) {
            clazz = null;
        }
    }

    @Override
    public void initAnnotationElems() {
        if (annotationElemsInitialized) {
            return;
        }

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].name().equals("<init>")
                    && !methods[i].name().equals("<clinit>")
                    && !methods[i].isSynthetic()) {
                AnnotationTypeElemInstance mi =
                        this.annotationElemInstance((JL5Method) methods[i],
                                                    ct,
                                                    ((JL5Method) methods[i]).hasDefaultVal());
                if (Report.should_report(verbose, 3))
                    Report.report(3, "adding " + mi + " to " + ct);
                ((JL5ParsedClassType) ct).addAnnotationElem(mi);
            }
        }

        annotationElemsInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    private AnnotationTypeElemInstance annotationElemInstance(JL5Method annot,
            ParsedClassType ct, boolean hasDefault) {
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[annot.getName()].value();
        String type = (String) constants[annot.getType()].value();
        if (type.charAt(0) != '(') {
            throw new ClassFormatError("Bad annotation type descriptor.");
        }

        int index = type.indexOf(')', 1);
        Type returnType = typeForString(type.substring(index + 1));
        return ((JL5TypeSystem) ts).annotationElemInstance(ct.position(),
                                                           ct,
                                                           ts.flagsForBits(annot.getModifiers()),
                                                           returnType,
                                                           name,
                                                           hasDefault);
    }
}
