package jltools.types;

/**
 * An <code>AmbiguousType</code> represents an unknown type. It may or may
 * not be full qualified. Ambiguous types are never canonical and never
 * primitive.
 */
public abstract class AmbiguousType extends Type 
{
  static final long serialVersionUID = -630819794355685897L;

  protected AmbiguousType()
  {
    super();
  }
  
  public AmbiguousType(TypeSystem ts)
  { 
    super(ts);
  }
  
  public boolean isPrimitive()
  {
    return false;
  }
  public boolean isReferenceType()
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
  public boolean isPackageType()
  {
    return false;
  }
  public boolean isCanonical()
  {
    return false;
  }
}


