package jltools.types;

/**
 * An <code>AmbiguousType</code> represents an unknown type. It may or may
 * not be full qualified. Ambiguous types are never canonical and never
 * primitive.
 */
public class AmbiguousType extends Type 
{
  static final long serialVersionUID = -630819794355685897L;

  protected String name;

  protected AmbiguousType()
  {
    super();
  }
  
  public AmbiguousType(TypeSystem ts, String name) 
  { 
    super(ts);
    this.name = name;
  }
  
  public String getTypeString() 
  {
    return name;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }

  public boolean isCanonical()
  {
    return false;
  }
}


