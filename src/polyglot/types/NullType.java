package jltools.types;

/**
 * A <code>NullType</code> represents the type of the Java keyword
 * <code>null</code>.
 */
public class NullType extends Type 
{
  static final long serialVersionUID = 6122344225204874423L;

  protected NullType()
  {
    super();
  }

  public NullType(TypeSystem ts) { 
    super(ts);
  }
  
  public String getTypeString() {
    return "null";
  }
  
  public boolean equals(Object o) {
    return o instanceof NullType;
  }
  public int hashCode() {
    return 6060842;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }
  public boolean isClassType()
  {
    return false;
  }
  public boolean isArrayType()
  {
    return false;
  }

  public boolean isCanonical()
  {
    return true;
  }
}


