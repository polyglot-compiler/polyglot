
package jltools.types;

/**
 * Provides a wrapper to abstract the LocalContext and Import Context
 */
public class ClassContext implements Context
{
  ClassContext cParent;
  ClassResolver crResolver;

  public ClassContext( ClassContext cParent)
  {
    this.cParent = cParent;
  }

  pubilc ClassContext ( ClassResolver crResolver)
  {
    this.crResolver = crResolver;
  }

  public Type lookup(String s)
  {
    return null;
  }
  public boolean isDefinedLocally(String s)
  {
    false;
  }
}
