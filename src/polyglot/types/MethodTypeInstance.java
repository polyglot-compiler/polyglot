/**
 * MethodTypeInstance.java
 */

package jltools.types;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import jltools.util.TypedList;


/**
 * MethodTypeInstance
 * 
 * An instance of a particular method type ( a method type is 
 * a name with arguments. An instance also contains the return type
 * exception types and access flags
 */
public class MethodTypeInstance extends MethodType implements TypeInstance
{
  static final long serialVersionUID = 327848486110215910L;

  // RI: every element is a Type.  May be null.  Immutable.
  protected TypedList exceptionTypes;
  // RI: May be null.
  protected AccessFlags flags;
  // RI: May be null.
  protected Type returnType;
  // The encompassing class
  protected ClassType enclosingType;
  
  /**
   *    ExceptionTypes, returnType, and AccessFlags may be null.
   **/
  public MethodTypeInstance(TypeSystem ts, 
                            ClassType enclosingType, 
                            String methodName, 
                            Type returnType,
                            List argumentTypes,
                            List exceptionTypes,
                            AccessFlags flags) {
    super( ts, methodName, argumentTypes);

    this.enclosingType = enclosingType;
    this.returnType = returnType;
    if (exceptionTypes != null)
      this.exceptionTypes = TypedList.copy(exceptionTypes,
					   Type.class, false);

    if (flags != null)
      this.flags = flags.copy();    
  }

  public Type getReturnType()
  {
    return returnType; 
  }

  public ClassType getEnclosingType()
  {
    return enclosingType;
  }
  
  public void setReturnType( Type returnType)  
  {
    this.returnType = returnType;
  }

  public TypedList exceptionTypes() { 
    if (exceptionTypes == null)
      exceptionTypes = new TypedList(new ArrayList(),
				     Type.class, true);   
    return exceptionTypes; 
  }

  public AccessFlags getAccessFlags() { return flags.copy(); }

  public Type getType()
  {
    return returnType;
  }

  public MethodTypeInstance copyInstance()
  {
    return new MethodTypeInstance( getTypeSystem(), 
               enclosingType, 
               getName(), 
               returnType, 
               argumentTypes(),                    
               (exceptionTypes != null ? exceptionTypes.copy() : null),
               (flags != null ? flags.copy() : null));
  }

}
