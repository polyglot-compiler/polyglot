
package jltools.types;
import java.util.Hashtable;

/**
 * A context to be used within the scope of a method body.  
 */
public class LocalContext implements Context
{
  Context cParent;
  Hashtable htLocalVariables;

  /**
   * Creates a LocalContext with a parent context. We refer to the parent
   * whenever the symbol is not found locally.
   */
  public LocalContext( Context cParent)
  {
    this.cParent = cParent;
    htLocalVariables = new Hashtable();
  }

  /**
   * Finds the type of a particular string within a context. If it is not found
   * in this context, checks the parent context.
   */
  public TypeInstance lookup(String s) throws TypeCheckError
  {
    TypeInstance t = (TypeInstance)htLocalVariables.get(s);
    
    return (t != (TypeInstance)null ? t : cParent.lookup(s));
  }
  
  /**
   * Returns whether the particular symbol is defined locally. If it isn't in this 
   * scope, we ask the parent scope, as long as it is not a ClassContext
   */
  public boolean isDefinedLocally(String s)
  {
    boolean b = htLocalVariables.contains(s);
    
    return (  ( b || cParent instanceof ClassContext) ? 
              b : cParent.isDefinedLocally(s));
  }

  /**
   * Adds a scoping "level" by createing a new LocalContext.
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
    if (cParent instanceof LocalContext)
      return (LocalContext) cParent;
    return this;
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String sName, TypeInstance t)
  {
    htLocalVariables.put(sName, t);
  }

}
