
package jltools.types;

import java.util.*;
import jltools.util.InternalCompilerError;

/**
 * A context to be used within the scope of a method body.  It provides a convenient wrapper
 * for the Type System.
 */
public class LocalContext 
{
  /**
   * Resolve anything that we dont' know about to the type system
   */
  TypeSystem ts;
  /**
   * the context which we pass on to the typesystem. tells it who "we" are.
   */
  TypeSystem.Context context; 
  /**
   * Contains the hashtable mapping for name => symbol. top of stack is context for current scope.
   */
  Stack /* of Hashtable */ stkContexts; 


  /** 
   * Creates a LocalContext without a parent context (i.e, for a method level
   * block).  All unresolved queries are passed on to the TypeSystem.  To do this, 
   * we'll also need the import table and what our enclosing class is.
   */
  public LocalContext ( ImportTable itImports, ClassType tThisClass, TypeSystem ts)
  {  
    this.ts = ts;
    
    context = new TypeSystem.Context ( itImports, tThisClass, null);
    
    stkContexts = new Stack();
    stkContexts.push( new Hashtable () );
  }
  
  /**
   * Returns whether the particular symbol is defined locally. If it isn't in this 
   * scope, we ask the parent scope, but don't traverse to enclosing classes.
   */
  public boolean isDefinedLocally(String s)
  {
    for (ListIterator i = stkContexts.listIterator(stkContexts.size()) ; i.hasPrevious() ; )
    {
      if ( ((Hashtable) i.previous()).contains( s ) )
        return true;
    }
    return false;
  }

  /**
   * Gets the methodMatch with name with "name" and a list of argument types "argumentTypes"
   * against Type "type". type may be null; 
   */
  public MethodTypeInstance getMethod( ClassType type, String methodName, List argumentTypes) throws TypeCheckException
  {
    return ts.getMethod( type, new MethodType( ts, methodName, argumentTypes), context);
  }

  /**
   * Gets a field matched against a particular type
   */  
  public FieldInstance getField( ClassType type, String fieldName) throws TypeCheckException
  {
    Object result;
    if ( type == null ) // could be a local, so check there first.
    {
      for (ListIterator i = stkContexts.listIterator(stkContexts.size()) ; i.hasPrevious() ; )
      {
        if ( (result = ((Hashtable) i.previous()).get( fieldName )) != null )
        {
          return new FieldInstance (fieldName, (Type)result, null, AccessFlags.flagsForInt(0));
        }
      }      
    }
    // not a local variable, so pass on to the type system.
    return ts.getField(type, fieldName, context);
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  
   **/
  public Type getType( Type type ) throws TypeCheckException
  {
    return ts.checkAndResolveType(type, context);
  }

  /**
   * Finds the definition of a particular type
   */
  public Type getType( String s) throws Exception
  {
    return ts.checkAndResolveType( new AmbiguousType( ts, s), context);
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
  public void pushScope()
  {
    stkContexts.push(new Hashtable());
  }

  /**
   * Removes a scoping level by returning our parent context
   */
  public void popScope()
  {
    if ( stkContexts.size() > 1)
    {
      try { stkContexts.pop(); }
      catch (EmptyStackException ese ) { }
    }
    else
    {
      throw new InternalCompilerError("No more scopes to pop!");
    }
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String sName, Type t)
  {
    ((Hashtable)stkContexts.peek()).put(sName, t);
  }
 
}
