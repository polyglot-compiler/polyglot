package jltools.types;

import java.util.List;

/**
 * A <code>ClassType</code> represents a class -- either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 */
public abstract class ClassType extends Type 
{
  protected ClassType()
  {
    super();
  }

  public ClassType( TypeSystem ts)
  {
    super( ts);
  }

  /*
   * First the plain old type stuff...
   */
  public boolean isPrimitive() { return false; }
  public boolean isCanonical() { return true; }

  public boolean equals(Object o) 
  {
    if (! (o instanceof ClassType)) {
      return false;
    }

    ClassType t = (ClassType)o;
    return t.getFullName().equals( getFullName());
  }
  
  public int hashCode() 
  {
    return getFullName().hashCode();
  }

  public String getTypeString() 
  {
    return getFullName();
  }

  /**
   * Returns the full name of this class, including package name and
   * containing class names, as it would appear in Java source.  Returns
   * null for an anonymous class.
   */
  public abstract  String getFullName();

  /**
   * Returns the short name of this class, not including package name
   * and containing class names.  Returns null for an anonymous class.
   */
  public abstract String getShortName();

  /**
   * Returns the full package name of this class.
   */
  public abstract String getPackage();

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

  /**
   * Returns this class's access flags.
   */
  public abstract AccessFlags getAccessFlags();

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

  // Inner class stuff.
  /**
   * Returns true iff this is an inner class.
   */
  public abstract boolean isInner();
  /**
   * Returns true iff this class is anonymous.
   */
  public abstract boolean isAnonymous(); 
  /**
   * If this class is inner, returns the containing class.  Otherwise returns
   * null.
   */
  public abstract ClassType getContainingClass();
  /**
   * If this class is an inner class, return its short name, encoded
   * with the name of its containing class.
   */
  public abstract String getInnerName();  

  /**
   * Return a list of the types of all the inner classes declared in this.
   */
  public abstract List getInnerClasses();

  /**
   * Returns the type of the inner in this whose short name is <name>.
   * Returns null if no such inner exists.
   */
  public abstract ClassType getInnerNamed(String name);
  
  // FIXME:  InMethod?
}
