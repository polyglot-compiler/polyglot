
package jltools.types;

import java.util.*;
import jltools.util.InternalCompilerError;
import jltools.types.MethodType;

/**
 * A context to be used within the scope of a method body.  
 * It provides a convenient wrapper for the Type System.
 */
public class LocalContext 
{
  /**
   * Resolve anything that we dont' know about to the type system
   */
  TypeSystem ts;
  /**
   * Contains the stack of inner class tuples.
   */
  Stack /* of ClassTuple */ stkContexts;
  /**
   * the import table for the file
   */
  ImportTable itImports;

  /** 
   * Creates a LocalContext without a parent context (i.e, for a method 
   * level block).  All unresolved queries are passed on to the TypeSystem.
   * To do this, we'll also need the import table and what our enclosing 
   * class is.
   */
  public LocalContext ( ImportTable itImports, TypeSystem ts)
  {  
    this.itImports = itImports;
    this.ts = ts;
    
    stkContexts = new Stack();
  }
  
  /**
   * Returns whether the particular symbol is defined locally. If it 
   * isn't in this scope, we ask the parent scope, but don't traverse to 
   * enclosing classes.
   */
  public boolean isDefinedLocally(String s)
  {
    Stack blockStack =  ((ClassTuple)stkContexts.peek()).getBlockStack();
    for (ListIterator i =blockStack.listIterator(blockStack.size()) ; 
         i.hasPrevious() ; )
    {
      Hashtable ht = (Hashtable) i.previous();
      if ( ht.get( s ) != null )
        return true; 
    }
    return false;
  }

  /**
   * Gets the methodMatch with name with "name" and a list of argument 
   * types "argumentTypes" against Type "type". type may be null; 
   */
  public MethodTypeInstance getMethod( ClassType type, String methodName, 
                                       List argumentTypes) 
    throws TypeCheckException
  {
    return ts.getMethod( type, 
                         new MethodType( ts, methodName, argumentTypes), 
                         ((ClassTuple)stkContexts.peek()).getContext() ); 
  }

  /**
   * Gets the methodMatch with name with of a MethodNode m on object t 
   * type may be null; 
   */
  public MethodTypeInstance getMethod( ClassType t, MethodType m) 
    throws TypeCheckException
  {
    return ts.getMethod( t, m, 
                         ((ClassTuple)stkContexts.peek()).getContext() );
  }
  
  /**
   * Gets a field matched against a particular type
   */  
  public FieldInstance getField( Type type, String fieldName) 
    throws TypeCheckException
  {
    Object result;
    Stack blockStack =  ((ClassTuple)stkContexts.peek()).getBlockStack();
    if ( type == null ) // could be a local, so check there first.
    {
      for (ListIterator i = blockStack.listIterator(blockStack.size()) ; 
           i.hasPrevious() ; )
      {
        if ((result = ((Hashtable) i.previous()).get( fieldName )) != null)
        {
          return (FieldInstance)result;
        }
      }      
    }
    // not a local variable, so pass on to the type system.
    return ts.getField(type, fieldName, 
                       ((ClassTuple)stkContexts.peek()).getContext());
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  
   **/
  public Type getType( Type type ) throws TypeCheckException
  {
    return ts.checkAndResolveType(type, 
                     ((ClassTuple)stkContexts.peek()).getContext());
  }

  /**
   * Finds the definition of a particular type
   */
  public Type getType( String s) throws TypeCheckException
  {
    return ts.checkAndResolveType( new AmbiguousType( ts, s), 
                         ((ClassTuple)stkContexts.peek()).getContext());
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
    stkContexts.push ( new ClassTuple ( 
                      new TypeSystem.Context ( itImports, c, null) ));
    if ( c.getSuperType() instanceof AmbiguousType)
      throw new Error();
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
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't push block since not " 
                                      + "in a class.");
    ((ClassTuple)stkContexts.peek()).pushBlock();
  }

  /**
   * Removes a scoping level 
   */
  public void popBlock()
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't pop block since not " 
                                      + "in a class.");
    ((ClassTuple)stkContexts.peek()).popBlock();
  }

  /**
   * enters a method
   */
  public void enterMethod(MethodTypeInstance mti)
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't enter function since " 
                                      + "not currently in a class.");
    ((ClassTuple)stkContexts.peek()).enterMethod(mti);
  }

  /**
   * leaves a method
   */
  public void leaveMethod()
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't leave function "
                                      + "since not currently in a class.");
    ((ClassTuple)stkContexts.peek()).leaveMethod();
  }

  /**
   * Gets the current method
   */
  public MethodTypeInstance getCurrentMethod() 
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't pop block since not "
                                      + " in a class.");
    return ((ClassTuple)stkContexts.peek()).getMethod();
  }

  /**
   * Gets current class
   */
  public ClassType getCurrentClass()
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't pop block since not " +
                                      " in a class.");
    return ((ClassTuple)stkContexts.peek()).getCurrentClass();
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String sName, FieldInstance fi) 
    throws TypeCheckException
  {
    if ( stkContexts.size() < 1)
      throw new InternalCompilerError("Can't pop block since not " 
                                      + "in a class.");
    Stack blockStack =  ((ClassTuple)stkContexts.peek()).getBlockStack();
    if ( blockStack == null || blockStack.size() == 0)
      throw new InternalCompilerError(" Can't add symbol since " 
                                      + "not inside a method");
    if ( ! ((Hashtable)blockStack.peek()).contains( sName ))
      ((Hashtable)blockStack.peek()).put(sName, fi);
    else
      throw new TypeCheckException ( "Symbol \"" + sName + 
                                     "\" already defined in this block.");

  }

  class ClassTuple
  {
    // contains a stack of hashtables ( e.g. for blocking structures) 
    // and a MethodTypeInstance giving the method that is currently 
    // being processed, and the TypeSystem.context

    Stack sBlocks;
    MethodTypeInstance mti;
    TypeSystem.Context context;
    
    ClassTuple (TypeSystem.Context c)
    {
      context = c;
      sBlocks = new Stack();
      mti = null;
    }
    
    void pushBlock() 
    {
      if ( mti == null)
      {
        throw new InternalCompilerError("Cannot push blocks since "
                                        + "MethodTypeInstance == null!");
      }
      sBlocks.push( new Hashtable () );
    }

    void popBlock()
    {
      try { sBlocks.pop(); }
      catch (EmptyStackException ese ) 
      { 
        throw new InternalCompilerError("Not enough blocks to pop.");
      }
    }
    
    void enterMethod(MethodTypeInstance mti)
    {
      this.mti = mti;
      sBlocks = new Stack();
      pushBlock();
    }
    
    void leaveMethod()
    {
      if ( mti == null) 
        throw new InternalCompilerError(" Cannot leave method, " 
                                        + "since not in one.");
      mti = null;
      popBlock();
    }
    
    Stack getBlockStack()  { return sBlocks; }

    ClassType getCurrentClass() { return context.inClass; }

    MethodTypeInstance getMethod()  { return mti; }
    
    TypeSystem.Context getContext() { return context;  }
  }

}
