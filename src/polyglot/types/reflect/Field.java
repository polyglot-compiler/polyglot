package polyglot.types.reflect;

import polyglot.types.*;
import java.util.*;
import java.io.*;

/**
 * Field models a field (member variable) in a class.  The Field class
 * grants access to information such as the field's modifiers, its name
 * and type descriptor (represented as indices into the constant pool),
 * and any attributes of the field.  Static fields have a ConstantValue
 * attribute.
 *
 * @see polyglot.types.reflect ConstantValue
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class Field {
    protected DataInputStream in;
    protected ClassFile clazz; 
    protected int modifiers;
    protected int name;
    protected int type;
    protected Attribute[] attrs;
    protected ConstantValue constantValue;
    protected boolean synthetic;

    /**
     * Constructor.  Read a field from a class file.
     *
     * @param in
     *        The data stream of the class file.
     * @param clazz
     *        The class file containing the field.
     * @exception IOException
     *        If an error occurs while reading.
     */
    Field(DataInputStream in, ClassFile clazz)
        throws IOException
    {
        this.clazz = clazz;
        this.in = in;
    }

    public void initialize() throws IOException {
        modifiers = in.readUnsignedShort();

        name = in.readUnsignedShort();
        type = in.readUnsignedShort();
        
        int numAttributes = in.readUnsignedShort();
        
        attrs = new Attribute[numAttributes];
        
        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();
            
            Constant name = clazz.getConstants()[nameIndex];
            
            if (name != null) {
                if ("ConstantValue".equals(name.value())) {
                    constantValue = new ConstantValue(in, nameIndex, length);
                    attrs[i] = constantValue;
                }
                if ("Synthetic".equals(name.value())) {
                    synthetic = true;
                }
            }
            
            if (attrs[i] == null) {
                long n = in.skip(length);
                if (n != length) {
                    throw new EOFException();
                }
            }
        }
    }
    
    /**
     * Return true of t is java.lang.String.
     * We don't compare against ts.String() because ts.String() may not
     * yet be set.
     */
    public boolean isString(Type t) {
      return t.isClass()
          && t.toClass().isTopLevel()
          && t.toClass().fullName().equals("java.lang.String");
    }

    public boolean isSynthetic() {
      return synthetic;
    }

    public boolean isConstant() {
      return this.constantValue != null;
    }
    
    public Constant constantValue() {
      if (this.constantValue != null) {
        int index = this.constantValue.getIndex();
        return clazz.getConstants()[index];
      }

      return null;
    }

    public int getInt() throws SemanticException {
      Constant c = constantValue();

      if (c != null && c.tag() == Constant.INTEGER) {
        Integer v = (Integer) c.value();
        return v.intValue();
      }

      throw new SemanticException("Could not find expected constant " +
                                  "pool entry with tag INTEGER.");
    }

    public float getFloat() throws SemanticException {
      Constant c = constantValue();

      if (c != null && c.tag() == Constant.FLOAT) {
        Float v = (Float) c.value();
        return v.floatValue();
      }

      throw new SemanticException("Could not find expected constant " +
                                  "pool entry with tag FLOAT.");
    }

    public double getDouble() throws SemanticException {
      Constant c = constantValue();

      if (c != null && c.tag() == Constant.DOUBLE) {
        Double v = (Double) c.value();
        return v.doubleValue();
      }

      throw new SemanticException("Could not find expected constant " +
                                  "pool entry with tag DOUBLE.");
    }

    public long getLong() throws SemanticException {
      Constant c = constantValue();

      if (c != null && c.tag() == Constant.LONG) {
        Long v = (Long) c.value();
        return v.longValue();
      }

      throw new SemanticException("Could not find expected constant " +
                                  "pool entry with tag LONG.");
    }

    public String getString() throws SemanticException {
      Constant c = constantValue();

      if (c != null && c.tag() == Constant.STRING) {
        Integer i = (Integer) c.value();
        c = clazz.getConstants()[i.intValue()];

        if (c != null && c.tag() == Constant.UTF8) {
          String v = (String) c.value();
          return v;
        }
      }

      throw new SemanticException("Could not find expected constant " +
                                  "pool entry with tag STRING or UTF8.");
    }

    public Attribute[] getAttrs() {
        return attrs;
    }
    public ClassFile getClazz() {
        return clazz;
    }
    public ConstantValue getConstantValue() {
        return constantValue;
    }
    public int getModifiers() {
        return modifiers;
    }
    public int getName() {
        return name;
    }
    public int getType() {
        return type;
    }
    public String name() {
      return (String) clazz.getConstants()[this.name].value();
    }
}
