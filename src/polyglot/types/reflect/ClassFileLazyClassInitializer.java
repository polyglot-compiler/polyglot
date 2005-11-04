package polyglot.types.reflect;

import java.io.*;
import java.util.*;

import polyglot.main.Report;
import polyglot.types.*;
import polyglot.types.reflect.InnerClasses.Info;
import polyglot.util.*;

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
    ClassFile clazz;
    TypeSystem ts;
    ParsedClassType ct;

    boolean init;
    boolean constructorsInitialized;
    boolean fieldsInitialized;
    boolean interfacesInitialized;
    boolean memberClassesInitialized;
    boolean methodsInitialized;
    boolean superclassInitialized;

    static Collection verbose = ClassFileLoader.verbose;

    public ClassFileLazyClassInitializer(ClassFile file, TypeSystem ts) {
        this.clazz = file;
        this.ts = ts;
    }
    
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

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
    ParsedClassType createType() throws SemanticException {
        // The name is of the form "p.q.C$I$J".
        String name = clazz.classNameCP(clazz.getThisClass());

        if (Report.should_report(verbose, 2))
            Report.report(2, "creating ClassType for " + name);

        // Create the ClassType.
        ParsedClassType ct = ts.createClassType(this);
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
    Type arrayOf(Type t, int dims) {
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
    List typeListForString(String str) {
        List types = new ArrayList();

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
    Type typeForString(String str) {
        List l = typeListForString(str);

        if (l.size() == 1) {
            return (Type) l.get(0);
        }

        throw new InternalCompilerError("Bad type string: \"" + str + "\"");
    }

    /**
     * Looks up a class by name, assuming the class exists.
     * @param name Name of the class to find.
     * @return A ClassType with the given name.
     * @throws InternalCompilerError if the class does not exist.
     */
    ClassType quietTypeForName(String name) {
        if (Report.should_report(verbose, 2))
            Report.report(2, "resolving " + name);

        try {
            return (ClassType) ts.systemResolver().find(name);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("could not load " + name);
        }
    }

    /**
     * Looks up a class by name.
     * @param name Name of the class to find.
     * @return A ClassType with the given name.
     * @throws SemanticException if the class does not exist.
     */
    ClassType typeForName(String name) throws SemanticException {
        if (Report.should_report(verbose, 2))
            Report.report(2, "resolving " + name);
        return (ClassType) ts.systemResolver().find(name);
    }

    public void initTypeObject() {
        this.init = true;
    }

    public boolean isTypeObjectInitialized() {
        return this.init;
    }

    public void initSuperclass() {
        if (superclassInitialized) {
            return;
        }

        if (ts.equals(ct, ts.Object())) {
            ct.superType(null);
        }
        else {
            String superName = clazz.classNameCP(clazz.getSuperClass());

            if (superName != null) {
                ct.superType(quietTypeForName(superName));
            }
            else {
                ct.superType(ts.Object());
            }
        }

        superclassInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    public void initInterfaces() {
        if (interfacesInitialized) {
            return;
        }

        int[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            String name = clazz.classNameCP(interfaces[i]);
            ct.addInterface(quietTypeForName(name));
        }
        
        interfacesInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    public void initMemberClasses() {
        if (memberClassesInitialized) {
            return;
        }

        InnerClasses innerClasses = clazz.getInnerClasses();
        
        if (innerClasses != null) {
            for (int i = 0; i < innerClasses.getClasses().length; i++) {
                Info c = innerClasses.getClasses()[i];

                if (c.outerClassIndex == clazz.getThisClass() && c.classIndex != 0) {
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

    public void canonicalFields() {
        initFields();
    }
    
    public void canonicalMethods() {
        initMethods();
    }
    
    public void canonicalConstructors() {
        initConstructors();
    }
    
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

    public void initMethods() {
        if (methodsInitialized) {
            return;
        }
        
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].name().equals("<init>")
                    && !methods[i].name().equals("<clinit>")
                    && !methods[i].isSynthetic()) {
                MethodInstance mi = this.methodInstance(methods[i], ct);
                if (Report.should_report(verbose, 3))
                    Report.report(3, "adding " + mi + " to " + ct);
                ct.addMethod(mi);
            }
        }

        methodsInitialized = true;

        if (initialized()) {
            clazz = null;
        }
    }

    public void initConstructors() {
        if (constructorsInitialized) {
            return;
        }

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].name().equals("<init>")
                    && !methods[i].isSynthetic()) {
                ConstructorInstance ci = this.constructorInstance(methods[i],
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
    MethodInstance methodInstance(Method method, ClassType ct) {
        Constant[] constants = clazz.getConstants();
        String name = (String) constants[method.getName()].value();
        String type = (String) constants[method.getType()].value();
    
        if (type.charAt(0) != '(') {
            throw new ClassFormatError("Bad method type descriptor.");
        }
    
        int index = type.indexOf(')', 1);
        List argTypes = typeListForString(type.substring(1, index));
        Type returnType = typeForString(type.substring(index+1));
    
        List excTypes = new ArrayList();
    
        Exceptions exceptions = method.getExceptions();
        if (exceptions != null) {
            int[] throwTypes = exceptions.getThrowTypes();
            for (int i = 0; i < throwTypes.length; i++) {
                String s = clazz.classNameCP(throwTypes[i]);
                excTypes.add(quietTypeForName(s));
            }
        }
    
        return ts.methodInstance(ct.position(), ct,
                                 ts.flagsForBits(method.getModifiers()),
                                 returnType, name, argTypes, excTypes);
      }

    /**
     * Create a ConstructorInstance.
     * @param method The JVM Method data structure for the constructor.
     * @param ct The class containing the method.
     * @param fields The constructor's fields, needed to remove parameters
     * passed to initialize synthetic fields.
     */
    ConstructorInstance constructorInstance(Method method, ClassType ct, Field[] fields) {
        // Get a method instance for the <init> method.
        MethodInstance mi = methodInstance(method, ct);
    
        List formals = mi.formalTypes();
    
        if (ct.isInnerClass()) {
            // If an inner class, the first argument may be a reference to an
            // enclosing class used to initialize a synthetic field.
    
            // Count the number of synthetic fields.
            int numSynthetic = 0;
    
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isSynthetic()) {
                    numSynthetic++;
                }
            }
    
            // Ignore a number of parameters equal to the number of synthetic
            // fields.
            if (numSynthetic <= formals.size()) {
                formals = formals.subList(numSynthetic, formals.size());
            }
        }
        
        return ts.constructorInstance(mi.position(), ct, mi.flags(),
                                      formals, mi.throwTypes());
    }

    /**
     * Create a FieldInstance.
     * @param field The JVM Field data structure for the field.
     * @param ct The class containing the field.
     */
    FieldInstance fieldInstance(Field field, ClassType ct) {
      Constant[] constants = clazz.getConstants();
      String name = (String) constants[field.getName()].value();
      String type = (String) constants[field.getType()].value();
    
      FieldInstance fi = ts.fieldInstance(ct.position(), ct,
                                          ts.flagsForBits(field.getModifiers()),
                                          typeForString(type), name);
    
      if (field.isConstant()) {
        Constant c = field.constantValue();
    
        Object o = null;
    
        try {
          switch (c.tag()) {
            case Constant.STRING: o = field.getString(); break;
            case Constant.INTEGER: o = new Integer(field.getInt()); break;
            case Constant.LONG: o = new Long(field.getLong()); break;
            case Constant.FLOAT: o = new Float(field.getFloat()); break;
            case Constant.DOUBLE: o = new Double(field.getDouble()); break;
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
