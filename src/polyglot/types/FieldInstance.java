/*
 * FieldInstnace.java
 */

package jltools.types;

import jltools.util.AnnotatedObject;
import jltools.util.InternalCompilerError;

/**
 * FieldInstance
 *
 * Overview:
 *    A FieldInstance represents the immutable typing information
 *    associated with a Java field: a set of access flags, a name, and
 *    a type.
 **/
public class FieldInstance extends AnnotatedObject 
  implements Cloneable, TypeInstance 
{

  public FieldInstance(String fieldName, Type fieldType, 
                       Type enclosingType, AccessFlags fieldFlags) {
    this.name = fieldName;
    this.type = fieldType;
    this.enclosingType = enclosingType;
    this.flags = fieldFlags.copy();
    oValue = null;
  }

  public AccessFlags getAccessFlags() { return flags.copy(); }
  public Type getType()         { return type; }
  public void setType( Type type) { this.type = type; }
  public String getName()       { return name; }
  public Type getEnclosingType() { return enclosingType; }
  public void setConstantValue(Object o) { oValue = o ; }
  public boolean isConstant() { return oValue != null; }
  public Object getConstantValue() 
  { 
    if ( oValue == null) throw new InternalCompilerError(
         "Tried to obtain constant value on a non-constant field.");
    return oValue;
  }

  private String name;
  private Type type, enclosingType;
  private AccessFlags flags;
  // used for constant values of 
  Object oValue;
}
