/*
 * Type.java
 */

package jltools.types;

/**
 * PrimitiveType
 *
 * Overview:
 *    A PrimitiveType represents a type which may not be directly coerced to
 *    java.lang.Object.    
 *
 *    This class should never be instantiated directly.  Instead, you should
 *    use the TypeSystem.get* methods.
 *
 *    ==> _All_ types are immutable.
 **/
public class PrimitiveType extends Type {
  public static final int VOID    = 1;
  public static final int BOOLEAN = 2;
  public static final int CHAR    = 3;
  public static final int BYTE    = 4;
  public static final int SHORT   = 5;
  public static final int INT     = 6;
  public static final int LONG    = 7;
  public static final int FLOAT   = 8;
  public static final int DOUBLE  = 9;

  public static final int MAX_KIND_USED = DOUBLE;
  
  public PrimitiveType(TypeSystem ts, int kind) {
    super(ts);
    this.kind = kind;
  }

  public int getKind() {
    return kind;
  }

  public final boolean isPrimitive() {
    return true;
  }
  public final boolean isCanonical() {
    return true;
  }

  public boolean isNumeric() {
    switch(kind) 
      {
      case CHAR:
      case BYTE:
      case SHORT:
      case INT:
      case LONG:
      case FLOAT:
      case DOUBLE:
	return true;
      default:
	return false;
      }
  }

  public boolean equals(Object o) {
    if (! (o instanceof PrimitiveType)) return false;
    PrimitiveType pt = (PrimitiveType) o;

    return (pt.kind == this.kind) && isComparable(pt);
  }

  public int hashCode() {
    return kind;
  }
      
  private int kind;
}


