/*
 * VariableInstance.java
 */

package jltools.types;

import jltools.util.AnnotatedObject;
import jltools.util.InternalCompilerError;

/**
 * VariableInstance
 *
 * Overview:
 *    A VariableInstance represents the immutable typing information
 *    associated with a Java variable: a name and a type.
 **/
public abstract class VariableInstance extends AnnotatedObject 
{
  public VariableInstance(String name, Type type, AccessFlags flags) {
    this.name = name;
    this.type = type;
    this.flags = flags.copy();
    oValue = null;
  }

  public abstract boolean isLocal();
  public abstract boolean isField();

  public Type getType()         { return type; }
  public void setType( Type type) { this.type = type; }
  public String getName()       { return name; }
  public void setConstantValue(Object o) { oValue = o ; }
  public boolean isConstant() { return oValue != null; }
  public AccessFlags getAccessFlags() { return flags.copy(); }

  public Object getConstantValue() 
  { 
    if ( oValue == null) throw new InternalCompilerError(
         "Tried to obtain constant value on a non-constant field.");
    return oValue;
  }

  private String name;
  private Type type;
  private AccessFlags flags;
  // used for constant values of 
  Object oValue;
}
