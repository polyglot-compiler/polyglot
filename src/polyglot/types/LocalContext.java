
package jltools.types;
import java.util.Hashtable;
import java.util.List;

/**
 * A context to be used within the scope of a method body.  It provides a convenient wrapper
 * for the Type System.
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
    // FIXME: implement
    return null;
  }

  /**
   * Finds a particular field within the current type system.
   */
  public TypeSystem.FieldMatch getField (String fieldName)
  {
    return getField(null, fieldName);
  }

  /**
   * Gets a field matched against a particular type
   */  
  public TypeSystem.FieldMatch getField( Type type, String fieldName)
  {
    // look up locally (only if type is null)
    if ( type == null)
    {
      Object o = htLocalVariables.get(fieldName);
      if (o != null) 
        return new TypeSystem.FieldMatch(null, 
                              new FieldInstance (fieldName, 
                                                 (Type)o, 
                                                 null, 
                                                 AccessFlags.flagsForInt(0)
                                                 )
                                );
    }
    if (lcParent != null)
    {
      return lcParent.getField(type, fieldName);
    }
    else 
    {
      // Fixme: implement
      // pass on call to typesystem.
      return null;
    }

  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  
   **/
  public Type checkAndResolveType( Type type ) throws TypeCheckError
  {
    return ts.checkAndResolveType(type, 
                               new TypeSystem.Context ( itImports, tEnclosingClass , null)) ;
  }

  /**
   * Finds the definition of a particular type
   */
  public Type getType( String s)
  {
    // FIXME: implement
    return null;
  }
  
  /**
   * Returns the current type system
   */
  public TypeSystem getTypeSystem()
  {
    return ts;
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
