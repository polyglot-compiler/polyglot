/*
 * Type.java
 */

package jltools.types;

import jltools.util.AnnotatedObject;

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
public abstract class Type extends AnnotatedObject {
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
  public final Type extendArrayDims(int dims) throws TypeCheckException
    { return ts.extendArrayDims(this,dims); }  
  public final boolean isSameType(Type t) throws TypeCheckException
    { return ts.isSameType(this,t); }
  public final boolean descendsFrom(Type ancestorType)throws TypeCheckException
    { return ts.descendsFrom(this,ancestorType); }
  public final boolean isAssignableSubtype(Type ancestorType) throws TypeCheckException
    { return ts.isAssignableSubtype(this,ancestorType); }
  public final boolean isCastValid(Type toType) throws TypeCheckException
    { return ts.isCastValid(this, toType); }
  public final boolean isImplicitCastValid(Type toType) throws TypeCheckException
    { return ts.isImplicitCastValid(this, toType); }
  public final boolean isThrowable() throws TypeCheckException
    { return ts.isThrowable(this); }
  public final boolean isUncheckedException() throws TypeCheckException
    { return ts.isUncheckedException(this); }

  public final TypeSystem getTypeSystem() 
    { return ts; }

  public final boolean isComparable(Type t) {
    return t.ts == this.ts;
  }
  protected TypeSystem ts;
  // Fixme: Temporary until types are figured out.
  protected String type;
}


