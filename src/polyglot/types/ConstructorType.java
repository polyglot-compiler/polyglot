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
  public ConstructorType(TypeSystem ts, ClassType ct, List argumentTypes) {
    super(ts, ct.getTypeString() + ".[Constructor]", argumentTypes);
  }
}
