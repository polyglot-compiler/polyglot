/*
 * Type.java
 */

package jltools.types;

/**
 * Type
 *
 * Overview:
 *    A type represents a type in some java-based typesystem.  Each type
 *    is tied to a particular TypeSystem; types with different TypeSystems
 *    are incomparable.
 *
 *    ==> _All_ types are immutable.
 **/
public abstract class Type {
  // Creates a new type in the given typeSystem.
  public Type(TypeSystem ts) { this.ts = ts; }

  ////
  // Methods to be filled in by subtypes.
  ////
  public abstract boolean isPrimitive();

  ////
  // Methods which dispatch to typeSystem
  ////
  public final boolean descendsFrom(Type t) 
    { return ts.descendsFrom(this,t); }
  public final boolean isSameType(Type t)
    { return ts.isSameType(this,t); }
  public final boolean isCanonical()
    { return ts.isCanonical(this); }
  public final Type getCanonicalType()
    { return ts.getCanonicalType(this); }
  public final boolean isTypeOk()
    { return ts.isTypeOk(this); }

  private TypeSystem ts;
}


