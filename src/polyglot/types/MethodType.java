package jltools.types;

import jltools.util.TypedList;
import java.util.List;
import java.util.ArrayList;

/**
 * A <code>MethodType</code> represents the immutable typing information
 * associated with a Java method.
 * <p>
 * A MethodType can be used as keys for method lookup; a particualr defn 
 * would be a MethodTypeInstance with the additional fields of returnType, 
 * accessFlags and exceptionTypes, in which case the TypeSystem would be null.
 */
public class MethodType extends Type implements Cloneable 
{
  static final long serialVersionUID = 5464098520246013181L;

  protected String name;
  protected TypedList argumentTypes;

  protected MethodType()
  {
    super();
  }

  public MethodType(TypeSystem ts, 
                    String methodName,
		    List argumentTypes) 
  {
    super(ts);
    this.name = methodName;
    this.argumentTypes = TypedList.copy(argumentTypes, Type.class, false);
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
  public boolean isReferenceType() { return false; }
  public boolean isClassType() { return false; }
  public boolean isArrayType() { return false; }
  public boolean isPackageType() { return false; }
  // FIXME: is this correct?
  public  boolean isCanonical() { return true; }
  
  public boolean equals(Object o)
  {
    if (o instanceof MethodType)
    {
      return (name.equals ( ((MethodType)o).name) &&
              getTypeSystem().hasSameArguments( this, (MethodType)o));
    }
    return false;
  }
}
