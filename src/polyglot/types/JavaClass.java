/*
 * JavaClass.java
 */

package jltools.types;

import java.util.List;

/**
 * JavaClass
 *
 * Overview: 
 *    A JavaClass represents a class -- either loaded from a ClassPath, 
 *    parsed from a source file, or obtained from other source.
 *
 *    The Type, Name, and Package of a class are immutable.  All other
 *    data may or may not be mutable, depending on the implementation.
 *    If a piece of data (other than Type, Name, and Package) is not
 *    ready, an Error will be thrown if code attempts to read it.
 *
 * Notes:
 *    This class is used to implement TypeSystem.
 **/
public interface JavaClass {
  
  /**
   * Returns the full name of this class, including package name and
   * containing class names, as it would appear in Java source.  Returns
   * null for an anonymous class.
   **/
  public String getFullName();

  /**
   * Returns the short name of this class, not including package name
   * and containing class names.  Returns null for an anonymous class.
   **/
  public String getShortName();

  /**
   * Returns the full package name of this class.
   **/
  public String getPackage();

  /**
   * Returns a TypedList of MethodTypeInstances for all the methods declared
   * in this.  It does not return methods declared in supertypes.
   **/
  public List getMethods();

  /**
   * Returns a TypedList of FieldInstances for all the fields declared
   * in this.  It does not return fields declared in supertypes.
   **/
  public List getFields();

  /**
   * Returns the type corresponding to 'this' within this class.
   **/
  public Type getType();

  /**
   * Returns this class's access flags.
   **/
  public AccessFlags getAccessFlags();

  // Inheritance stuff
  /** 
   * Returns the supertype of this class.  For every class except Object, this
   * is non-null.
   **/
  public Type getSupertype();
  /**
   * Returns a TypedList of the types of this class's interfaces.
   **/
  public List getInterfaces();

  // Inner class stuff.

  /**
   * Returns true iff this is an inner class.
   **/
  public boolean isInner();
  /**
   * Returns true iff this class is anonymous.
   **/
  public boolean isAnonymous(); 
  /**
   * If this class is inner, returns the containing class.  Otherwise returns
   * null.
   **/
  public Type getContainingClass();
  /**
   * If this class is an inner class, return its short name, encoded
   * with the name of its containing class.
   **/
  public String getInnerName();  

  /**
   * Return a list of the types of all the inner classes declared in this.
   **/
  public List getInnerClasses();

  /**
   * Returns the type of the inner in this whose short name is <name>.
   * Returns null if no such inner exists.
   **/
  public Type getInnerNamed(String name);
  
  // FIXME:  InMethod?
}
