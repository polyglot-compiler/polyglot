/*
 * NullType.java
 */

package jltools.types;

/**
 * NullType
 *
 * Overview:
 *    An NullType represents an Null of base java types.
 *
 *    ==> _All_ types are immutable.
 **/
public class NullType extends Type {
  public NullType(TypeSystem ts) { 
    super(ts);
  }
  public boolean equals(Object o) {
    return o instanceof NullType;
  }
  public int hashCode() {
    return 6060842;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }

  public boolean isCanonical()
  {
    // FIXME: Correct?
    return true;
  }
}


