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
public class FieldInstance extends VariableInstance
  implements Cloneable, java.io.Serializable 
{
  static final long serialVersionUID = -3339170626027684669L;

  public FieldInstance(String fieldName, Type fieldType, 
                       ReferenceType enclosingType, AccessFlags fieldFlags) {
    super(fieldName, fieldType, fieldFlags);
    this.enclosingType = enclosingType;
  }

  public boolean isLocal() { return false; }
  public boolean isField() { return true; }

  public ReferenceType getEnclosingType() { return enclosingType; }

  public boolean equals(Object o) {
    return super.equals(o) &&
      ((FieldInstance)o).getEnclosingType().equals(getEnclosingType());
  }

  public int hashCode() {
    return super.hashCode() ^ getEnclosingType().hashCode();
  }

  private ReferenceType enclosingType;
}
