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
public class AmbiguousType extends Type {

  String name;

  public AmbiguousType(TypeSystem ts, String name) { 
    super(ts);
    this.name = name;
  }
  
  public String getTypeString() 
  {
    return name;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }

  public boolean isCanonical()
  {
    return false;
  }
}


