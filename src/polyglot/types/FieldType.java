/*
 * FieldType.java
 */

package jltools.types;

/**
 * FieldType
 *
 * Overview:
 *    A FieldType represents the mutable typing information associated with a 
 *    Java field: a set of access flags, a name, and a type.
 **/
public class FieldType implements Cloneable {

  public FieldType(String fieldName, Type fieldType, AccessFlags fieldFlags) {
    this.name = fieldName;
    this.type = fieldType;
    this.flags = fieldFlags.copy();
  }

  public AccessFlags getFlags() { return flags; }
  public Type getType()         { return type; }
  public String getName()       { return name; }

  public void setFlags(AccessFlags val) { flags = val; }
  public void setType(Type val)         { type  = val; }
  public void setName(String val)       { name = val; } 

  private String name;
  private Type type;
  private AccessFlags flags;
}
