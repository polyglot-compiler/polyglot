package jltools.types;

import java.util.List;

/**
 * A <code>ReferenceType</code> represents a reference type --
 * a type on which contains methods and fields and is a subtype of
 * Object.
 */
public abstract class ReferenceType extends Type 
{ 
  protected ReferenceType()
  {
    super();
  }

  public ReferenceType( TypeSystem ts)
  {
    super( ts);
  }

  /*
   * First the plain old type stuff...
   */
  public boolean isPrimitive() { return false; }
  public boolean isReferenceType() { return true; }
  public boolean isPackageType() { return false; }

  public ReferenceType toReferenceType() {
    return this;
  }

  /**
   * Returns a TypedList of MethodTypeInstances for all the methods declared
   * in this.  It does not return methods declared in supertypes.
   */
  public abstract List getMethods();

  /**
   * Returns a TypedList of FieldInstances for all the fields declared
   * in this.  It does not return fields declared in supertypes.
   */
  public abstract List getFields();

  // Inheritance stuff
  /** 
   * Returns the supertype of this class.  For every class except Object, this
   * is non-null.
   */
  public abstract Type getSuperType();
  /**
   * Returns a TypedList of the types of this class's interfaces.
   */
  public abstract List getInterfaces();
}
