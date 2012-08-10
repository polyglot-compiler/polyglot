/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 1997-2001 Purdue Research Foundation
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

package polyglot.types.reflect;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.tools.FileObject;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.SemanticException;

/**
 * ClassFile represents a Java classfile as it is found on disk. The classfile
 * is modelled according to the Java Virtual Machine Specification. Methods are
 * provided to access the classfile at a very low level.
 * 
 * @see polyglot.types.reflect Attribute
 * @see polyglot.types.reflect ConstantValue
 * @see polyglot.types.reflect Field
 * @see polyglot.types.reflect Method
 * 
 * @author Nate Nystrom
 */
public class ClassFile_c implements ClassFile {
    protected Constant[] constants; // The constant pool
    protected int modifiers; // This class's modifer bit field
    protected int thisClass;
    protected int superClass;
    protected int[] interfaces;
    protected Field[] fields;
    protected Method[] methods;
    protected Attribute[] attrs;
    protected InnerClasses innerClasses;
    protected FileObject classFileSource;
    protected ExtensionInfo extensionInfo;

    protected Map<String, JLCInfo> jlcInfoCache =
            new HashMap<String, JLCInfo>();

    protected static Collection<String> verbose;
    static {
        verbose = new HashSet<String>();
        verbose.add("loader");
    }

    /**
     * Constructor. This constructor parses the class file from the byte array
     * 
     * @param code
     *            A byte array containing the class data
     * @throws IOException
     */
    public ClassFile_c(FileObject classFileSource, byte[] code,
            ExtensionInfo ext) throws IOException {
        this.classFileSource = classFileSource;
        this.extensionInfo = ext;

        ByteArrayInputStream bin = new ByteArrayInputStream(code);
        DataInputStream in = new DataInputStream(bin);
        read(in);
        in.close();
        bin.close();
    }

    @Deprecated
    public String getClassFileLocation() {
        return getClassFileURI().getPath();
    }

    @Override
    public URI getClassFileURI() {
        return classFileSource.toUri();
    }

    JLCInfo getJLCInfo(String typeSystemKey) {
        // Check if already set.
        JLCInfo jlc = jlcInfoCache.get(typeSystemKey);

        if (jlc != null) {
            return jlc;
        }

        jlc = new JLCInfo();
        jlcInfoCache.put(typeSystemKey, jlc);

        try {
            int mask = 0;

            for (int i = 0; i < fields.length; i++) {
                if (fields[i].name().equals("jlc$SourceLastModified$"
                        + typeSystemKey)) {
                    jlc.sourceLastModified = fields[i].getLong();
                    mask |= 1;
                }
                else if (fields[i].name().equals("jlc$CompilerVersion$"
                        + typeSystemKey)) {
                    jlc.compilerVersion = fields[i].getString();
                    mask |= 2;
                }
                else if (fields[i].name().equals("jlc$ClassType$"
                        + typeSystemKey)) {
                    // there is encoded class type information.
                    StringBuffer encodedClassTypeInfo =
                            new StringBuffer(fields[i].getString());
                    // check to see if there are more fields.
                    int seeking = 1;
                    boolean found;
                    do {
                        found = false;
                        String suffix = ("$" + seeking);
                        String seekingFieldName =
                                "jlc$ClassType$" + typeSystemKey + suffix;
                        for (int j = 0; j < fields.length; j++) {
                            if (fields[j].name().equals(seekingFieldName)) {
                                encodedClassTypeInfo.append(fields[j].getString());
                                found = true;
                                seeking++;
                                break;
                            }
                        }
                    } while (found);
                    jlc.encodedClassType = encodedClassTypeInfo.toString();
                    mask |= 4;
                }
            }

            if (mask != 7) {
                // Not all the information is there. Reset to default.
                jlc.sourceLastModified = 0;
                jlc.compilerVersion = null;
                jlc.encodedClassType = null;
            }
        }
        catch (SemanticException e) {
            jlc.sourceLastModified = 0;
            jlc.compilerVersion = null;
            jlc.encodedClassType = null;
        }

        return jlc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * polyglot.types.reflect.ClassFileI#sourceLastModified(java.lang.String)
     */
    @Override
    public long sourceLastModified(String ts) {
        JLCInfo jlc = getJLCInfo(ts);
        return jlc.sourceLastModified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#compilerVersion(java.lang.String)
     */
    @Override
    public String compilerVersion(String ts) {
        JLCInfo jlc = getJLCInfo(ts);
        return jlc.compilerVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#encodedClassType(java.lang.String)
     */
    @Override
    public String encodedClassType(String typeSystemKey) {
        JLCInfo jlc = getJLCInfo(typeSystemKey);
        return jlc.encodedClassType;
    }

    /**
     * Read the class file.
     */
    void read(DataInputStream in) throws IOException {
        // Read in file contents from stream
        readHeader(in);
        readConstantPool(in);
        readAccessFlags(in);
        readClassInfo(in);
        readFields(in);
        readMethods(in);
        readAttributes(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#classNameCP(int)
     */
    @Override
    public String classNameCP(int index) {
        Constant c = constants[index];

        if (c != null && c.tag() == Constant.CLASS) {
            Integer nameIndex = (Integer) c.value();
            if (nameIndex != null) {
                c = constants[nameIndex.intValue()];
                if (c.tag() == Constant.UTF8) {
                    String s = (String) c.value();
                    return s.replace('/', '.');
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#name()
     */
    @Override
    public String name() {
        Constant c = constants[thisClass];
        if (c.tag() == Constant.CLASS) {
            Integer nameIndex = (Integer) c.value();
            if (nameIndex != null) {
                c = constants[nameIndex.intValue()];
                if (c.tag() == Constant.UTF8) {
                    return (String) c.value();
                }
            }
        }

        throw new ClassFormatError("Couldn't find class name in file");
    }

    /**
     * Read a constant from the constant pool.
     * 
     * @param in
     *            The stream from which to read.
     * @return The constant.
     * @exception IOException
     *                If an error occurs while reading.
     */
    Constant readConstant(DataInputStream in) throws IOException {
        int tag = in.readUnsignedByte();
        Object value;

        switch (tag) {
        case Constant.CLASS:
        case Constant.STRING:
            value = new Integer(in.readUnsignedShort());
            break;
        case Constant.FIELD_REF:
        case Constant.METHOD_REF:
        case Constant.INTERFACE_METHOD_REF:
        case Constant.NAME_AND_TYPE:
            value = new int[2];

            ((int[]) value)[0] = in.readUnsignedShort();
            ((int[]) value)[1] = in.readUnsignedShort();
            break;
        case Constant.INTEGER:
            value = new Integer(in.readInt());
            break;
        case Constant.FLOAT:
            value = new Float(in.readFloat());
            break;
        case Constant.LONG:
            // Longs take up 2 constant pool entries.
            value = new Long(in.readLong());
            break;
        case Constant.DOUBLE:
            // Doubles take up 2 constant pool entries.
            value = new Double(in.readDouble());
            break;
        case Constant.UTF8:
            value = in.readUTF();
            break;
        default:
            throw new ClassFormatError("Invalid constant tag: " + tag);
        }

        return new Constant(tag, value);
    }

    /**
     * Read the class file header.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readHeader(DataInputStream in) throws IOException {
        int magic = in.readInt();

        if (magic != 0xCAFEBABE) {
            throw new ClassFormatError("Bad magic number.");
        }

        @SuppressWarnings("unused")
        int major = in.readUnsignedShort();

        @SuppressWarnings("unused")
        int minor = in.readUnsignedShort();
    }

    /**
     * Read the class's constant pool. Constants in the constant pool are
     * modeled by an array of <tt>reflect.Constant</tt>/
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     * 
     * @see Constant
     * @see #constants
     */
    void readConstantPool(DataInputStream in) throws IOException {
        int count = in.readUnsignedShort();

        constants = new Constant[count];

        // The first constant is reserved for internal use by the JVM.
        constants[0] = null;

        // Read the constants.
        for (int i = 1; i < count; i++) {
            constants[i] = readConstant(in);

            switch (constants[i].tag()) {
            case Constant.LONG:
            case Constant.DOUBLE:
                // Longs and doubles take up 2 constant pool entries.
                constants[++i] = null;
                break;
            }
        }
    }

    /**
     * Read the class's access flags.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readAccessFlags(DataInputStream in) throws IOException {
        modifiers = in.readUnsignedShort();
    }

    /**
     * Read the class's name, superclass, and interfaces.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readClassInfo(DataInputStream in) throws IOException {
        thisClass = in.readUnsignedShort();
        superClass = in.readUnsignedShort();

        int numInterfaces = in.readUnsignedShort();

        interfaces = new int[numInterfaces];

        for (int i = 0; i < numInterfaces; i++) {
            interfaces[i] = in.readUnsignedShort();
        }
    }

    /**
     * Read the class's fields.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readFields(DataInputStream in) throws IOException {
        int numFields = in.readUnsignedShort();

        fields = new Field[numFields];

        for (int i = 0; i < numFields; i++) {
            fields[i] = createField(in);
        }
    }

    /**
     * Read the class's methods.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readMethods(DataInputStream in) throws IOException {
        int numMethods = in.readUnsignedShort();

        methods = new Method[numMethods];

        for (int i = 0; i < numMethods; i++) {
            methods[i] = createMethod(in);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * polyglot.types.reflect.ClassFileI#readAttributes(java.io.DataInputStream)
     */
    @Override
    public void readAttributes(DataInputStream in) throws IOException {
        int numAttributes = in.readUnsignedShort();

        attrs = new Attribute[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();
            String name = (String) constants[nameIndex].value();
            Attribute a = createAttribute(in, name, nameIndex, length);
            if (a != null) {
                attrs[i] = a;
            }
            else {
                long n = in.skip(length);
                if (n != length) {
                    throw new EOFException();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * polyglot.types.reflect.ClassFileI#createMethod(java.io.DataInputStream)
     */
    @Override
    public Method createMethod(DataInputStream in) throws IOException {
        Method m = new Method(in, this);
        m.initialize();
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * polyglot.types.reflect.ClassFileI#createField(java.io.DataInputStream)
     */
    @Override
    public Field createField(DataInputStream in) throws IOException {
        Field f = new Field(in, this);
        f.initialize();
        return f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * polyglot.types.reflect.ClassFileI#createAttribute(java.io.DataInputStream
     * , java.lang.String, int, int)
     */
    @Override
    public Attribute createAttribute(DataInputStream in, String name,
            int nameIndex, int length) throws IOException {
        if (name.equals("InnerClasses")) {
            innerClasses = new InnerClasses(in, nameIndex, length);
            return innerClasses;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getAttrs()
     */
    @Override
    public Attribute[] getAttrs() {
        return attrs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getConstants()
     */
    @Override
    public Constant[] getConstants() {
        return constants;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getFields()
     */
    @Override
    public Field[] getFields() {
        return fields;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getInnerClasses()
     */
    @Override
    public InnerClasses getInnerClasses() {
        return innerClasses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getInterfaces()
     */
    @Override
    public int[] getInterfaces() {
        return interfaces;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getMethods()
     */
    @Override
    public Method[] getMethods() {
        return methods;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getModifiers()
     */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getSuperClass()
     */
    @Override
    public int getSuperClass() {
        return superClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see polyglot.types.reflect.ClassFileI#getThisClass()
     */
    @Override
    public int getThisClass() {
        return thisClass;
    }

    @Override
    public String toString() {
        return name();
    }
}
