
package jltools.types;
import java.util.Hashtable;
import java.util.List;

/**
 * A context to be used within the scope of a method body.  
 */
public class LocalContext 
{
  LocalContext lcParent;
  TypeSystem ts;
  ImportTable itImports;
  Type tEnclosingClass;
  Hashtable htLocalVariables;

  /**
   * Creates a LocalContext with a parent context. We refer to the parent
   * whenever the symbol is not found locally.
   */
  public LocalContext( LocalContext lcParent)
  {
    this.lcParent = lcParent;
    this.itImports = null;
    this.ts = null;
    this.tEnclosingClass = null;
    htLocalVariables = new Hashtable();
  }

  /** 
   * Creates a LocalContext without a parent context (i.e, for a method level
   * block).  All unresolved queries are passed on to the TypeSystem.  To do this, 
   * we'll also need the import table and what our enclosing class is.
   */
  public LocalContext ( ImportTable itImports, Type tEnclosingClass, TypeSystem ts)
  {  
    this.lcParent = null;
    this.itImports = itImports;
    this.tEnclosingClass = tEnclosingClass;
    this.ts = ts;
    htLocalVariables = new Hashtable();
  }

  /**
   * Finds the type of a particular string within a context. If it is not found
   * in this context, checks the parent context.
   */
  public Type lookup(String s) throws TypeCheckError
  {
    Type t = (Type)htLocalVariables.get(s);
    
    if ( t != null ) 
      return t;
    
    if (lcParent != null)
      return lcParent.lookup(s);
    
    if (ts != null)
    {
      // FIXME: properly figure out if it's canonical or not 
      Object o =  ts.checkAndResolveType( new ClassType ( ts, s, true) , 
                                 new TypeSystem.Context ( itImports, tEnclosingClass , null));
      if (o instanceof Type)
        return (Type)o;
      throw new TypeCheckError((String)o);
    }
    return null;
  }
  
  /**
   * Returns whether the particular symbol is defined locally. If it isn't in this 
   * scope, we ask the parent scope, as long as it is not a ClassContext
   */
  public boolean isDefinedLocally(String s)
  {
    boolean b = htLocalVariables.contains(s);
    
    return (  ( b || lcParent != null) ? 
              b : lcParent.isDefinedLocally(s));
  }

  /**
   * Gets the methodMatch with  name with "name" and a list of argument types "argumentTypes"
   * against Type "type".
   */
  public TypeSystem.MethodMatch getMethod( Type type, String methodName, List argumentTypes)
  {
    //FIXME: implement
    return null;
  }

  /**
   * Gets the MethodMatch with a possibly ambiguous name "name" and list of "argumentTypes"
   */
  public TypeSystem.MethodMatch getMethod( String methodName, List argumentTypes)
  {
    return null;
  }
  
  /**
   * Returns a new LocalContext with an additional scoping level.
   */
  public LocalContext pushScope()
  {
    return new LocalContext( this );
  }

  /**
   * Removes a scoping level by returning our parent context
   */
  public LocalContext popScope()
  {
    if (lcParent != null)
      return  lcParent;
    return this;
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String sName, Type t)
  {
    htLocalVariables.put(sName, t);
  }

}
