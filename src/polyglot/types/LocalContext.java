
package jltools.types;

/**
 * Provides a wrapper to abstract the LocalContext and Import Context
 */
public class LocalContext implements Context
{

  Context cParent;

  public LocalContext( Context cParent)
  {
    this.cParent = cParent;
  }

  public Type lookup(String s)
  {
    return null;
  }
  public boolean isDefinedLocally(String s)
  {
    return false;
  }

  public LocalContext pushScope()
  {
    return new LocalContext( this );
  }

  public LocalContext popScope()
  {
    if (cParent instanceof LocalContext)
      return (LocalContext) cParent;
    return this;
  }

  public void addSymbol( String sName, Type t)
  {
    
  }

}
