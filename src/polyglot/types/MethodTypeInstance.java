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
  // RI: every element is a Type.  May be null.  Immutable.
  private TypedList exceptionTypes;
  // RI: May be null.
  private AccessFlags flags;
  // RI: May be null.
  private Type returnType;
  
  /**
   *    ExceptionTypes, returnType, and AccessFlags may be null.
   **/
  public MethodTypeInstance(TypeSystem ts, 
                            String methodName, 
                            Type returnType,
                            List argumentTypes,
                            List exceptionTypes,
                            AccessFlags flags) {
    super( ts, methodName, argumentTypes);

    this.returnType = returnType;
    if (exceptionTypes != null)
      this.exceptionTypes = TypedList.copy(exceptionTypes,
					   Type.class, true);

    if (flags != null)
      this.flags = flags.copy();    
  }

  public Type returnType()          { return returnType; }
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
               getName(), 
               returnType, 
               argumentTypes(),                    
               (exceptionTypes != null ? exceptionTypes.copy() : null),
               (flags != null ? flags.copy() : null));
  }

}
