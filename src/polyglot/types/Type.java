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

  // FIXME: Temporary method until typing figured out
  public abstract String getTypeString();

  ////
  // Methods to be filled in by subtypes.
  ////
  public abstract boolean isPrimitive();
  public abstract boolean isCanonical();

  ////
  // Methods which dispatch to typeSystem
  ////
  public final Type extendArrayDims(int dims)
    { return ts.extendArrayDims(this,dims); }  
  public final boolean isSameType(Type t)
    { return ts.isSameType(this,t); }
  public final Type getCanonicalType(TypeSystem.Context context)
    { return ts.getCanonicalType(this, context); }
  public final String checkTypeOk(TypeSystem.Context context)
    { return ts.checkTypeOk(this, context); }
  public final boolean descendsFrom(Type ancestorType)
    { return ts.descendsFrom(this,ancestorType); }
  public final boolean isAssignableSubtype(Type ancestorType)
    { return ts.isAssignableSubtype(this,ancestorType); }
  public final boolean isCastValid(Type toType) 
    { return ts.isCastValid(this, toType); }
  public final boolean isImplicitCastValid(Type toType) 
    { return ts.isImplicitCastValid(this, toType); }
  public final boolean isThrowable()
    { return ts.isThrowable(this); }
  public final boolean isUncheckedException()
    { return ts.isUncheckedException(this); }

  public final TypeSystem getTypeSystem()
    { return ts; }

  public final boolean isComparable(Type t) {
    return t.ts == this.ts;
  }
  private TypeSystem ts;
  // Fixme: Temporary until types are figured out.
  private String type;
}


