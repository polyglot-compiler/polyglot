package jltools.types;

/**
 * An <code>ArrayType</code> represents an array of base java types.
 */
public class ArrayType extends Type 
{
  static final long serialVersionUID = -2145638221364361658L;

  protected Type base;
  protected int dims;

  protected ArrayType()
  {
    super();
  }

  public ArrayType(TypeSystem ts, Type baseType, int dims) 
  { 
    super( ts);
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

  public Type getBaseType() 
  {
    return base;
  }
  
  public int getDimensions() 
  {
    return dims;
  }
  
  public String getTypeString() {
    StringBuffer sb = new StringBuffer();
    sb.append(base.getTypeString());
    for(int i = 0; i < dims; i++) {
      sb.append("[]");
    }
    return sb.toString();
  }

  public boolean isPrimitive() 
  {
    return false;
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
    return t.dims == dims && getTypeSystem().isSameType(t.base, base);
  }
  
  public int hashCode() 
  {
    return base.hashCode() ^ (dims << 3);
  }
}


