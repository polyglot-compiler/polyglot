/*
 * MethodType.java
 */

package jltools.types;

import jltools.util.TypedList;
import java.util.List;
import java.util.ArrayList;

/**
 * MethodType
 *
 * Overview:
 *    A MethodType represents the immutable typing information
 *    associated with a Java method.
 *
 *    A MethodType can be used as keys for method lookup; a particualr defn 
 *    would be a MethodTypeInstance with the additional fields of returnType, 
 *    accessFlags and exceptionTypes, in which case the TypeSystem would be null.
 **/
public class MethodType extends Type implements Cloneable {
  public MethodType(TypeSystem ts, 
                    String methodName,
		    List argumentTypes) {
    super(ts);
    this.name = methodName;
    this.argumentTypes = TypedList.copy(argumentTypes, Type.class, true);
  }  


  public MethodType copy() {
    return new MethodType(getTypeSystem(), name,
			  argumentTypes.copy());
  }

  public String getTypeString() 
  {
    return "METHOD " + name;
  }
  public Object clone() { return copy(); }

  public void setName(String name) { this.name = name; }
  public String getName() { return name; }

  public TypedList argumentTypes()  { return argumentTypes; }

  public boolean isPrimitive() { return false; }
  // FIXME: is this correct?
  public  boolean isCanonical() { return true; }


  private String name;
  // RI: every element is a Type.  Immutable.
  private TypedList argumentTypes;

}
