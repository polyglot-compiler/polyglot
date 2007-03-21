/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 1997-2001 Purdue Research Foundation, Purdue University
 * 
 */

package polyglot.types.reflect;

import java.io.*;
import java.util.*;
import polyglot.frontend.*;

import polyglot.types.*;
import polyglot.util.*;

/**
 * ClassFile represents a Java classfile as it is found on 
 * disk.  The classfile is modelled according to the Java Virtual Machine
 * Specification.  Methods are provided to access the classfile at a very
 * low level.
 *
 * @see polyglot.types.reflect Attribute
 * @see polyglot.types.reflect ConstantValue
 * @see polyglot.types.reflect Field
 * @see polyglot.types.reflect Method
 *
 * @author Nate Nystrom
 */
public class ClassFile {
    protected Constant[] constants; // The constant pool
    protected int modifiers;        // This class's modifer bit field
    protected int thisClass;              
    protected int superClass;             
    protected int[] interfaces;           
    protected Field[] fields;
    protected Method[] methods;
    protected Attribute[] attrs;
    protected InnerClasses innerClasses;
    protected File classFileSource;
    protected ExtensionInfo extensionInfo;
    
    protected Map jlcInfoCache = new HashMap();
   
    protected static Collection verbose = ClassFileLoader.verbose;
  
    /**
     * Constructor.  This constructor parses the class file from the byte array
     *
     * @param code
     *        A byte array containing the class data
     */
    public ClassFile(File classFileSource, byte[] code, ExtensionInfo ext) {
        this.classFileSource = classFileSource;
        this.extensionInfo = ext;

        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(code);
            DataInputStream in = new DataInputStream(bin);
            read(in);
            in.close();
            bin.close();
        }
        catch (IOException e) {
            throw new InternalCompilerError("I/O exception on ByteArrayInputStream");
        }
    }

    JLCInfo getJLCInfo(String typeSystemKey) {
      // Check if already set.
      JLCInfo jlc = (JLCInfo) jlcInfoCache.get(typeSystemKey);

      if (jlc != null) {
        return jlc;
      }

      jlc = new JLCInfo();
      jlcInfoCache.put(typeSystemKey, jlc);

      try {
        int mask = 0;

        for (int i = 0; i < fields.length; i++) {
          if (fields[i].name().equals("jlc$SourceLastModified$" + typeSystemKey)) {
            jlc.sourceLastModified = fields[i].getLong();
            mask |= 1;
          }
          else if (fields[i].name().equals("jlc$CompilerVersion$" + typeSystemKey)) {
            jlc.compilerVersion = fields[i].getString();
            mask |= 2;
          }
          else if (fields[i].name().equals("jlc$ClassType$" + typeSystemKey)) {
            // there is encoded class type information.
            StringBuffer encodedClassTypeInfo = new StringBuffer(fields[i].getString());
            // check to see if there are more fields.
            int seeking = 1;
            boolean found;
            do {
                found = false;
                String suffix = ("$" + seeking);
                String seekingFieldName = "jlc$ClassType$" + typeSystemKey + suffix;
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
          // Not all the information is there.  Reset to default.
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

    /**
     * Get the encoded source modified time.
     */
    public long sourceLastModified(String ts) {
      JLCInfo jlc = getJLCInfo(ts);
      return jlc.sourceLastModified;
    }

    /**
     * Get the encoded compiler version used to compile the source.
     */
    public String compilerVersion(String ts) {
      JLCInfo jlc = getJLCInfo(ts);
      return jlc.compilerVersion;
    }

    /**
     * Get the encoded class type for the given type system.
     */
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

  /**
   * Get the class name at the given constant pool index.
   */
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

  /**
   * Get the name of the class, including the package name.
   *
   * @return
   *        The name of the class.
   */
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
   *        The stream from which to read.
   * @return
   *        The constant.
   * @exception IOException
   *        If an error occurs while reading.
   */
  Constant readConstant(DataInputStream in)
       throws IOException
  {
    int tag = in.readUnsignedByte();
    Object value;
    
    switch (tag) 
      {
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
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  void readHeader(DataInputStream in)
       throws IOException
  {
    int magic = in.readInt();
    
    if (magic != 0xCAFEBABE) {
      throw new ClassFormatError("Bad magic number.");
    }
    
    int major = in.readUnsignedShort();
    int minor = in.readUnsignedShort();
  }
  
  /**
   * Read the class's constant pool.  Constants in the constant pool
   * are modeled by an array of <tt>reflect.Constant</tt>/
   *
   * @param in
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   *
   * @see Constant
   * @see #constants
   */
  void readConstantPool(DataInputStream in)
       throws IOException
  {
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
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  void readAccessFlags(DataInputStream in)
       throws IOException
  {
    modifiers = in.readUnsignedShort();
  }
  
  /**
   * Read the class's name, superclass, and interfaces.
   *
   * @param in
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  void readClassInfo(DataInputStream in)
       throws IOException
  {
    int index;
    
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
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  void readFields(DataInputStream in)
       throws IOException
  {
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
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  void readMethods(DataInputStream in)
       throws IOException
  {
    int numMethods = in.readUnsignedShort();
    
    methods = new Method[numMethods];
    
    for (int i = 0; i < numMethods; i++) {
      methods[i] = createMethod(in);
    }
  }
  
  /**
   * Read the class's attributes.  Since none of the attributes
   * are required, just read the length of each attribute and
   * skip that many bytes.
   *
   * @param in
   *        The stream from which to read.
   * @exception IOException
   *        If an error occurs while reading.
   */
  public void readAttributes(DataInputStream in)
       throws IOException
  {
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

    public Method createMethod(DataInputStream in) throws IOException {
      Method m = new Method(in, this);
      m.initialize();
      return m;
    }

    public Field createField(DataInputStream in) throws IOException {
      Field f = new Field(in, this);
      f.initialize();
      return f;
    }

    public Attribute createAttribute(DataInputStream in, String name,
                                    int nameIndex, int length)
                                    throws IOException {
      if (name.equals("InnerClasses")) {
        innerClasses = new InnerClasses(in, nameIndex, length);
        return innerClasses;
      }
      return null;
    }

    public Attribute[] getAttrs() {
        return attrs;
    }
    public Constant[] getConstants() {
        return constants;
    }
    public Field[] getFields() {
        return fields;
    }
    public InnerClasses getInnerClasses() {
        return innerClasses;
    }
    public int[] getInterfaces() {
        return interfaces;
    }
    public Method[] getMethods() {
        return methods;
    }
    public int getModifiers() {
        return modifiers;
    }
    public int getSuperClass() {
        return superClass;
    }
    public int getThisClass() {
        return thisClass;
    }
    
    public String toString() {
        return name();
    }
}
