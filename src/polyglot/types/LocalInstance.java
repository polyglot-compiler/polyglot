/*
 * LocalInstnace.java
 */

package jltools.types;

import jltools.util.AnnotatedObject;
import jltools.util.InternalCompilerError;

/**
 * LocalInstance
 *
 * Overview:
 *    A LocalInstance represents the immutable typing information
 *    associated with a Java local: a name and a type.
 **/
public class LocalInstance extends VariableInstance 
  implements Cloneable, java.io.Serializable 
{
  public LocalInstance(String name, Type type, AccessFlags flags) {
    super(name, type, flags);
  }

  public boolean isLocal() { return true; }
  public boolean isField() { return false; }
}
