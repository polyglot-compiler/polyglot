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
  implements Cloneable, TypeInstance, java.io.Serializable
{
  static final long serialVersionUID = 7947688323544252267L;

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
    if ( oValue == null) throw new InternalCompilerError(this,
         "Tried to obtain constant value on a non-constant field.");
    return oValue;
  }

  public boolean equals(Object other)
  {
    if (other == null || !(other instanceof VariableInstance))
      return false;
    VariableInstance vi = (VariableInstance)other;
    
    if (vi.getName() == null && getName() != null)
      return false;
    else if (!vi.getName().equals(getName()))
      return false;

    if (vi.getType() == null && getType() != null)
      return false;
    else if (!vi.getType().equals(getType()))
      return false;
    
    if (vi.isConstant() && isConstant())
      return vi.getConstantValue().equals(getConstantValue());

    return true;
  }

  public int hashCode() {
    return getName().hashCode() ^ getType().hashCode();
  }

  public String toString() {
    return getType() + " " + getName();
  }

  private String name;
  private Type type;
  private AccessFlags flags;
  // used for constant values of 
  Object oValue;
}
