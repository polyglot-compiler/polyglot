package jltools.types;

import jltools.util.*;

import java.io.*;


/**
 * A type represents a type in some Java-based typesystem.  Each type
 * is tied to a particular TypeSystem; types with different TypeSystems
 * are incomparable.
 * <p>
 * <b>All types are immutable.</b>
 */
public abstract class Type extends AnnotatedObject implements Serializable
{
  static final long serialVersionUID = -7347257353426805117L;

  /**
   * The system to be used for comparison of types. This must be set in one
   * of two ways: either through the constructor taking one argument, or
   * through deserialization.
   * <p>
   * Note that this field itself is not serialized. Instead a 
   * <code>TypeInputStream</code> should be used to deserialize the this
   * object (or any subclass) so that the <code>TypeSystem</code> is properly
   * set.
   */
  protected transient TypeSystem ts;

  /**
   * Used for deserializing types.
   */
  protected Type()
  {
    this( null);
  }
  
  /** Creates a new type in the given a TypeSystem. */
  public Type(TypeSystem ts) { this.ts = ts; }

  public abstract String getTypeString();

  /*
   * To be filled in by subtypes.
   */
  public abstract boolean isPrimitive();
  /*
   * To be filled in by subtypes.
   */
  public abstract boolean isCanonical();

  public final Type extendArrayDims(int dims) throws SemanticException
  { 
    return ts.extendArrayDims(this,dims); 
  }  
  
  public final boolean isSameType(Type t) throws SemanticException
  { 
    return ts.isSameType(this,t); 
  }
  
  public final boolean descendsFrom(Type ancestorType)throws SemanticException
  { 
    return ts.descendsFrom(this,ancestorType); 
  }
  
  public final boolean isAssignableSubtype(Type ancestorType) 
    throws SemanticException
  { 
    return ts.isAssignableSubtype(this,ancestorType); 
  }
  
  public final boolean isCastValid(Type toType) throws SemanticException
  {
    return ts.isCastValid(this, toType); 
  }
  
  public final boolean isImplicitCastValid(Type toType) 
    throws SemanticException
  { 
    return ts.isImplicitCastValid(this, toType); 
  }
  
  public final boolean isThrowable() throws SemanticException
  {
    return ts.isThrowable(this); 
  }
  
  public final boolean isUncheckedException() throws SemanticException
  {
    return ts.isUncheckedException(this); 
  }

  public final TypeSystem getTypeSystem() 
  {
    return ts; 
  }

  public final boolean isComparable(Type t) 
  {
    return t.ts == this.ts;
  }

  private void readObject( ObjectInputStream in)
     throws IOException, ClassNotFoundException
  {
    if( in instanceof TypeInputStream) {
      ts = ((TypeInputStream)in).getTypeSystem();
    }
  }
}


