package jltools.types;

import java.util.*;
import jltools.ext.polyj.types.*;

/**
 * An <code>ArrayType</code> represents an array of base java types.
 */
public class ArrayType extends ReferenceType 
{
  static final long serialVersionUID = -2145638221364361658L;

  protected Type base;
  protected List fields;
  protected List methods;
  protected List interfaces;
  protected int dims;
  protected Type ultimateBase;

  protected ArrayType()
  {
    super();
  }

  public ArrayType(TypeSystem ts, Type baseType, int dims) 
  {
    super( ts);
    if (dims <= 0) 
      throw new Error("Tried to create ArrayType with <=0 dimensions");
    if (dims > 1) {
      base = new ArrayType(ts, baseType, dims - 1);
    }
    else {
      base = baseType;
    }

    AccessFlags cloneFlags = new AccessFlags();
    cloneFlags.setPublic(true);

    methods = new ArrayList(1);
    methods.add(new MethodTypeInstance(ts, this, "clone",
	ts.getObject(), new LinkedList(), new LinkedList(), cloneFlags));

    AccessFlags lengthFlags = new AccessFlags();
    lengthFlags.setFinal(true);
    lengthFlags.setPublic(true);

    fields = new ArrayList(1);
    fields.add(ts.newFieldInstance("length", ts.getInt(), this, lengthFlags));

    interfaces = new ArrayList(2);
    interfaces.add(ts.getCloneable());
    interfaces.add(ts.getSerializable());
    
    this.dims = (base instanceof ArrayType) ? ((ArrayType)base).getDims() + 1 : dims;
    ultimateBase = (base instanceof ArrayType) ? ((ArrayType)base).getUltimateBaseType() : base;
  }

  public ArrayType(TypeSystem ts, Type baseType)
  {
    this( ts, baseType, 1);
  }

  public Type getBaseType() 
  {
    return base;
  }
  
  public int getDims() 
  {
      return dims;
  }

  public Type getUltimateBaseType() 
  {
      return ultimateBase;
  }

  public String getTypeString() {
    StringBuffer sb = new StringBuffer();
    sb.append(base.getTypeString());
    sb.append("[]");
    return sb.toString();
  }

  public String translate(LocalContext c) {
      return ts.translateArrayType(c, this);
  }

  public boolean isPrimitive() 
  {
    return false;
  }
    public ArrayType toArrayType() {
	return this;
    }

  public boolean isClassType() { return false; }
  public boolean isArrayType() { return true; }

  public boolean isCanonical() 
  {
    return base.isCanonical();
  }

  public boolean equals(Object o)
  {
    if (! (o instanceof ArrayType)) return false;

    ArrayType t = (ArrayType) o;
    return getTypeSystem().isSameType(t.base, base);
  }
  
  public int hashCode() 
  {
    return base.hashCode() << 1;
  }

  // Arrays override Object's "clone" method.
  public List getMethods() {
    return methods;
  }

  // Arrays have a public final int field named "length".
  public List getFields() {
    return fields;
  }

  public FieldInstance getLengthField() {
    return (FieldInstance) fields.get(0);
  }

  public Type getSuperType() {
    return ts.getObject();
  }

  // Arrays implement the Cloneable and Serializable interfaces.
  public List getInterfaces() {
    return interfaces;
  }
}


