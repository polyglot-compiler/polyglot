/*
 * ArrayType.java
 */

package jltools.types;

/**
 * ArrayType
 *
 * Overview:
 *    An ArrayType represents an array of base java types.
 *
 *    ==> _All_ types are immutable.
 **/
public class ArrayType extends Type {
  public ArrayType(TypeSystem ts, Type baseType, int dims) { 
    super(ts);
    if (dims <= 0) 
      throw new Error("Tried to create ArrayType with <=0 dimensions");
    if (baseType instanceof ArrayType) {
      ArrayType baseArray = (ArrayType) baseType;
      base = baseArray.base;
      this.dims = dims + baseArray.dims;
    } else {
      base = baseType;
      this.dims = dims;
    }
  }

  private Type base;
  private int dims;

  public Type getBaseType() {
    return base;
  }
  public int getDimensions() {
    return dims;
  }
  
  public String getTypeString() {
    return base.getTypeString() + "[]";
  }

  ////
  // Methods to be filled in by subtypes.
  ////
  public boolean isPrimitive() {
    return false;
  }
  public boolean isCanonical() {
    return base.isCanonical();
  }
  public boolean equals(Object o) {
    if (! (o instanceof ArrayType)) return false;

    ArrayType t = (ArrayType) o;
    return t.dims == dims && getTypeSystem().isSameType(t.base, base);
  }
  public int hashCode() {
    return base.hashCode() ^ (dims << 3);
  }
}


