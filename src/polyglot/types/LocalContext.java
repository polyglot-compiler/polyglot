
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
   * Contains the hashtable mapping for name => symbol. top of stack is context for current scope.
   */
  Stack /* of Hashtable */ stkBlocks; 
  /**
   * Contains the stack of inner class contexts.
   */
  Stack /* of TypeSystem.Context */ stkContexts;
  /**
   * the import table for the file
   */
  ImportTable itImports;

  /** 
   * Creates a LocalContext without a parent context (i.e, for a method level
   * block).  All unresolved queries are passed on to the TypeSystem.  To do this, 
   * we'll also need the import table and what our enclosing class is.
   */
  public LocalContext ( ImportTable itImports, TypeSystem ts)
  {  
    this.itImports = itImports;
    this.ts = ts;
    
    stkBlocks = new Stack();
    stkBlocks.push( new Hashtable () );

    stkContexts = new Stack();
  }
  
  /**
   * Returns whether the particular symbol is defined locally. If it isn't in this 
   * scope, we ask the parent scope, but don't traverse to enclosing classes.
   */
  public boolean isDefinedLocally(String s)
  {
    for (ListIterator i = stkBlocks.listIterator(stkBlocks.size()) ; i.hasPrevious() ; )
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
    return ts.getMethod( type, new MethodType( ts, methodName, argumentTypes), (TypeSystem.Context)stkContexts.peek());
  }

  /**
   * Gets a field matched against a particular type
   */  
  public FieldInstance getField( ClassType type, String fieldName) throws TypeCheckException
  {
    Object result;
    if ( type == null ) // could be a local, so check there first.
    {
      for (ListIterator i = stkBlocks.listIterator(stkBlocks.size()) ; i.hasPrevious() ; )
      {
        if ( (result = ((Hashtable) i.previous()).get( fieldName )) != null )
        {
          return new FieldInstance (fieldName, (Type)result, null, AccessFlags.flagsForInt(0));
        }
      }      
    }
    // not a local variable, so pass on to the type system.
    return ts.getField(type, fieldName, (TypeSystem.Context)stkContexts.peek());
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  
   **/
  public Type getType( Type type ) throws TypeCheckException
  {
    return ts.checkAndResolveType(type, (TypeSystem.Context)stkContexts.peek());
  }

  /**
   * Finds the definition of a particular type
   */
  public Type getType( String s) throws TypeCheckException
  {
    return ts.checkAndResolveType( new AmbiguousType( ts, s), (TypeSystem.Context)stkContexts.peek());
  }
  
  /**
   * Returns the current type system
   */
  public TypeSystem getTypeSystem()
  {
    return ts;
  }

  /**
   * Pushes on a class  scoping
   */
  public void pushClass( ClassType c)
  {
    stkContexts.push ( new TypeSystem.Context ( itImports, c, null) );
  }

  /**
   * Pops the most recently pushed class scoping
   */
  public void popClass()
  {
    if ( stkContexts.size() >= 1)
    {
      try { stkContexts.pop(); }
      catch (EmptyStackException ese ) { }
    }
    else
    {
      throw new InternalCompilerError("No more class-scopes to pop!");
    }
  }

  /**
   * pushes an additional block-scoping level.
   */
  public void pushBlock()
  {
    // FIXME: nks put block scoping within class scoping
    stkBlocks.push(new Hashtable());
  }

  /**
   * Removes a scoping level 
   */
  public void popBlock()
  {
    if ( stkBlocks.size() > 1)
    {
      try { stkBlocks.pop(); }
      catch (EmptyStackException ese ) { }
    }
    else
    {
      throw new InternalCompilerError("No more block-scopes to pop!");
    }
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String sName, Type t)
  {
    ((Hashtable)stkBlocks.peek()).put(sName, t);
  }

}
