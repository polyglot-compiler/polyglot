/*
 * ConstructorType.java
 */

package jltools.types;

import java.util.List;

/**
 * ConstructorTypeInstance
 *
 * Overview:
 *    An instance of a particular method type.  Contains additional info such as accessflags.
 *    A ConstructorType represents the immutable typing information
 *    associated with a Java constructor.
 *
 **/
public class ConstructorTypeInstance extends MethodTypeInstance 
{
  static final long serialVersionUID = 1113653833108152935L;

  /**
   *    ExceptionTypes and AccessFlags may be null.
   **/
  public ConstructorTypeInstance(TypeSystem ts, 
                                 ClassType enclosingType, 
                                 List argumentTypes,
                                 List exceptionTypes,
                                 AccessFlags flags) {
    super(ts, enclosingType, enclosingType.getTypeString() + ".[Constructor]", ts.getVoid(), 
          argumentTypes, exceptionTypes, flags);
  }
}
