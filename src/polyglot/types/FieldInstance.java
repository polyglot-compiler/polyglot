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
    LValue = null;
  }

  public AccessFlags getAccessFlags() { return flags.copy(); }
  public Type getType()         { return type; }
  public void setType( Type type) { this.type = type; }
  public String getName()       { return name; }
  public Type getEnclosingType() { return enclosingType; }
  public void setConstantValue(long i) { LValue = new Long ( i) ; }
  public boolean isConstant() { return LValue != null; }
  public int getConstantValue() 
  { 
    if ( LValue == null) throw new InternalCompilerError(
         "Tried to obtain constant value on a non-constant field.");
    return (int)LValue.longValue();
  }

  private String name;
  private Type type, enclosingType;
  private AccessFlags flags;
  // used for constant values of 
  Long LValue;
}
