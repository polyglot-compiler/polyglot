package jltools.types;

import jltools.util.InternalCompilerError;
import java.util.List;

/**
 * A <code>ClassType</code> represents a class -- either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 */
public abstract class ClassType extends ReferenceType 
{
  static final long serialVersionUID = -176302096315403062L;

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
  public boolean isCanonical() { return true; }
  public boolean isClassType() { return true; }
  public boolean isArrayType() { return false; }

  public ClassType toClassType() {
      return this;
  }

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

  public String translate(LocalContext c) 
  {
      return ts.translateClassType(c, this);
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
   * Returns this class's access flags.
   */
  public abstract AccessFlags getAccessFlags();

  // Inner class stuff.
  /**
   * Returns true iff this is an inner class.
   */
  public abstract boolean isInner();
  /**
   * Returns true iff this class is local or anonymous.
   */
  public abstract boolean isLocal(); 
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
  public abstract Type getInnerNamed(String name);
}
