/*
 * FieldInstnace.java
 */

package jltools.types;

/**
 * FieldInstance
 *
 * Overview:
 *    A FieldInstance represents the immutable typing information
 *    associated with a Java field: a set of access flags, a name, and
 *    a type.
 **/
public class FieldInstance implements Cloneable, TypeInstance {

  public FieldInstance(String fieldName, Type fieldType, 
                       Type enclosingType, AccessFlags fieldFlags) {
    this.name = fieldName;
    this.type = fieldType;
    this.enclosingType = enclosingType;
    this.flags = fieldFlags.copy();
  }

  public AccessFlags getAccessFlags() { return flags.copy(); }
  public Type getType()         { return type; }
  public String getName()       { return name; }

  private String name;
  private Type type, enclosingType;
  private AccessFlags flags;
}
