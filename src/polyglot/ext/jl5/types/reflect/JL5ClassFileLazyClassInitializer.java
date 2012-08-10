package polyglot.ext.jl5.types.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
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
import polyglot.util.Position;
import polyglot.util.StringUtil;

/**
 * XXX TODO
 * Enums
 * default annotation vals
 */
public class JL5ClassFileLazyClassInitializer extends
        ClassFileLazyClassInitializer implements JL5LazyClassInitializer {

    protected boolean annotationsInitialized;

    public JL5ClassFileLazyClassInitializer(ClassFile file, TypeSystem ts) {
        super(file, ts);
    }

    @Override
    protected boolean initialized() {
        return super.initialized() & annotationsInitialized;
    }

    /**
     * Create the type for this class file.
     */
    @Override
    protected ParsedClassType createType() throws SemanticException {
        // The name is of the form "p.q.C$I$J".
        String name = clazz.classNameCP(clazz.getThisClass());

        if (Report.should_report(verbose, 2))
            Report.report(2, "creating ClassType for " + name);

        // Create the ClassType.
        JL5ParsedClassType ct = (JL5ParsedClassType) ts.createClassType(this);
        ct.flags(ts.flagsForBits(clazz.getModifiers()));
        ct.position(position());

        // This is the "p.q" part.
        String packageName = StringUtil.getPackageComponent(name);

        // Set the ClassType's package.
        if (!packageName.equals("")) {
            ct.package_(ts.packageForName(packageName));
        }

        // This is the "C$I$J" part.
        String className = StringUtil.getShortNameComponent(name);

        String outerName; // This will be "p.q.C$I"
        String innerName; // This will be "J"

        outerName = name;
        innerName = null;

        while (true) {
            int dollar = outerName.lastIndexOf('$');

            if (dollar >= 0) {
                outerName = name.substring(0, dollar);
                innerName = name.substring(dollar + 1);
            }
            else {
                outerName = name;
                innerName = null;
                break;
            }

            // Try loading the outer class.
            // This will recursively load its outer class, if any.
            try {
                if (Report.should_report(verbose, 2))
                    Report.report(2, "resolving " + outerName + " for " + name);
                ct.outer(this.typeForName(outerName));
                break;
            }
            catch (SemanticException e) {
                // Failed. The class probably has a '$' in its name.
                if (Report.should_report(verbose, 3))
                    Report.report(2, "error resolving " + outerName);
            }
        }

        ClassType.Kind kind = ClassType.TOP_LEVEL;

        if (innerName != null) {
            // A nested class. Parse the class name to determine what kind.
            StringTokenizer st = new StringTokenizer(className, "$");

            while (st.hasMoreTokens()) {
                String s = st.nextToken();

                if (Character.isDigit(s.charAt(0))) {
                    // Example: C$1
                    kind = ClassType.ANONYMOUS;
                }
                else if (kind == ClassType.ANONYMOUS) {
                    // Example: C$1$D
                    kind = ClassType.LOCAL;
                }
                else {
                    // Example: C$D
                    kind = ClassType.MEMBER;
                }
            }
        }

        if (Report.should_report(verbose, 3))
            Report.report(3, name + " is " + kind);

        ct.kind(kind);

        if (ct.isTopLevel()) {
            ct.name(className);
        }
        else if (ct.isMember() || ct.isLocal()) {
            ct.name(innerName);
        }

        // Add unresolved class into the cache to avoid circular resolving.
        ts.systemResolver().addNamed(name, ct);
        ts.systemResolver().addNamed(ct.fullName(), ct);

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
            pc.formals(new ArrayList<TypeVariable>(ct.typeVariables()));

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

        List<ReferenceType> excTypes = new ArrayList<ReferenceType>();

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
            return ((JL5TypeSystem) ts).methodInstance(ct.position(),
                                                       ct,
                                                       ts.flagsForBits(method.getModifiers()),
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

            return ((JL5TypeSystem) ts).methodInstance(ct.position(),
                                                       ct,
                                                       ts.flagsForBits(method.getModifiers()),
                                                       returnType,
                                                       name,
                                                       argTypes,
                                                       excTypes);
        }
    }

    @Override
    protected ClassType quietTypeForName(String name) {
        JL5ParsedClassType pct;
        ClassType ct = super.quietTypeForName(name);
        // If ct is a parameterized type, since we got here from a raw
        // name we need to create a raw type
        if (ct instanceof JL5ParsedClassType)
            pct = (JL5ParsedClassType) ct;
        else
        // Only worried about ParsedClassTypes
        return ct;

        if (!pct.typeVariables().isEmpty()) {
            return ((JL5TypeSystem) ts).rawClass(pct,
                                                 Position.compilerGenerated());
        }
        return ct;
    }

    @Override
    protected ConstructorInstance constructorInstance(Method method,
            ClassType ct, Field[] fields) {
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

        return ((JL5TypeSystem) ts).constructorInstance(mi.position(),
                                                        ct,
                                                        mi.flags(),
                                                        formals,
                                                        mi.throwTypes(),
                                                        mi.typeParams());
    }

    @Override
    protected FieldInstance fieldInstance(Field field_, ClassType ct) {
        JL5Field field = (JL5Field) field_;
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[field.getName()].value();
        String type = (String) constants[field.getType()].value();

        FieldInstance fi = null;
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
            fi =
                    ((JL5TypeSystem) ts).enumInstance(ct.position(),
                                                      ct,
                                                      flags,
                                                      name,
                                                      (ParsedClassType) fieldType,
                                                      0);
        }
        else {
            fi = ts.fieldInstance(ct.position(), ct, flags, fieldType, name);
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

        return fi;
    }

    @Override
    public void initAnnotationElems() {
        if (annotationsInitialized) {
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

        annotationsInitialized = true;

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
