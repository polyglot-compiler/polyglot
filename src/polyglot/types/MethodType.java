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
 *    A MethodType represents the mutable typing information associated with a 
 *    Java method. 
 *
 *    A MethodType object may be partial, and contain as little as a name and
 *    a list of arguments.  Such objects are used as keys for method lookup.
 **/
public class MethodType implements Cloneable {
  /**
   * Requires: All references to argumentTypes will be discarded after
   *    this constructor is called.
   **/
  public MethodType(String methodName,
		    List argumentTypes) {
    this.name = methodName;
    this.argumentTypes = new TypedList(argumentTypes, Type.class, false);      
  }  

  /**
   * Requires: All references to argumentTypes and exceptionTypes
   *    will be discarded after this constructor is called.
   *    ExceptionTypes, returnType, and AccessFlags may be null.
   **/
  public MethodType(String methodName, 
		    Type returnType,
		    List argumentTypes,
		    List exceptionTypes,
		    AccessFlags flags) {
    this.name = methodName;
    this.returnType = returnType;
    this.argumentTypes = new TypedList(argumentTypes,
				       Type.class, false);
    if (exceptionTypes != null)
      this.exceptionTypes = new TypedList(exceptionTypes,
					  Type.class, false);

    if (flags != null)
      this.flags = flags.copy();    
  }

  public MethodType copy() {
    return new MethodType(name,
			  returnType,
			  argumentTypes.copy(),
			  exceptionTypes.copy(),
			  flags);
  }
  public Object clone() { return copy(); }

  public AccessFlags getFlags() {
    return flags;
  }
  public void setFlags(AccessFlags flags) {
    this.flags = flags;
  }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public Type returnType()          { return returnType; }
  public void setReturnType(Type t) { returnType = t; }
  public TypedList argumentTypes()  { return argumentTypes; }
  public TypedList exceptionTypes() { 
    if (exceptionTypes == null)
      exceptionTypes = new TypedList(new ArrayList(),
				     Type.class, false);      

    return exceptionTypes; 
  }

  private String name;
  // RI: every element is a Type.
  private TypedList argumentTypes;
  // RI: every element is a Type.  May be null.
  private TypedList exceptionTypes;
  // RI: May be null.
  private AccessFlags flags;
  // RI: May be null.
  private Type returnType;
}
