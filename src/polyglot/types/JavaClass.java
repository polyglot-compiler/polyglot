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
 *    The Type, Name, and Package of a class are immutable.  All other data
 *    may or may not be mutable, depending on the implementation.
 *
 *    This class is used to implement TypeSystem.
 **/
public abstract class JavaClass {
  
  /**
   * Returns the full name of this class, including package name and
   * containing class names, as it would appear in Java source.  Returns
   * null for an anonymous class.
   **/
  public abstract String getFullName();

  /**
   * Returns the short name of this class, not including package name
   * and containing class names.  Returns null for an anonymous class.
   **/
  public abstract String getShortName();

  /**
   * Returns the full package name of this class.
   **/
  public abstract String getPackage();

  /**
   * Returns a TypedList of MethodTypes for all the methods declared
   * in this.  It does not return methods declared in supertypes.
   **/
  public abstract List getMethods();

  /**
   * Returns a TypedList of FieldTypes for all the fields declared
   * in this.  It does not return fields declared in supertypes.
   **/
  public abstract List getFields();

  /**
   * Returns the type corresponding to 'this' within this class.
   **/
  public abstract Type getType();

  /**
   * Returns this class's access flags.
   **/
  public abstract AccessFlags getAccessFlags();

  // Inheritance stuff
  /** 
   * Returns the supertype of this class.  For every class except Object, this
   * is non-null.
   **/
  public abstract Type getSupertype();
  /**
   * Returns a TypedList of the types of this class's interfaces.
   **/
  public abstract List getInterfaces();

  // Inner class stuff.

  /**
   * Returns true iff this is an inner class.
   **/
  public abstract boolean isInner();
  /**
   * Returns true iff this class is anonymous.
   **/
  public abstract boolean isAnonymous(); 
  /**
   * If this class is inner, returns the containing class.  Otherwise returns
   * null.
   **/
  public abstract JavaClass getContainingClass();
  /**
   * If this class is an inner class, return its short name, encoded
   * with the name of its containing class.
   **/
  public abstract String getInnerName();  

  // We can't implement this with JDK 1.1, so we won't depend on it.
  //  public abstract List getInnerClasses();
}
