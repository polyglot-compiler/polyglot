/*
 * ConstructorType.java
 */

package jltools.types;

import java.util.List;

/**
 * ConstructorType
 *
 * Overview:
 *    A ConstructorType represents the immutable typing information
 *    associated with a Java constructor.
 *
 *    A ConstructorType object may be partial, and contain as little as a
 *    list of arguments.  Such objects are used as keys for method
 *    lookup.
 **/
public class ConstructorType extends MethodType {
  public ConstructorType(List argumentTypes) {
    super("[Constructor]", argumentTypes);
  }

  /**
   *    ExceptionTypes and AccessFlags may be null.
   **/
  public ConstructorType(List argumentTypes,
			 List exceptionTypes,
			 AccessFlags flags) {
    super("[Constructor]", null, argumentTypes, exceptionTypes, flags);
  }
}
