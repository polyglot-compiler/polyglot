
package jltools.types;

/**
 * Provides a wrapper to abstract the LocalContext and Import Context
 */
public interface Context 
{
  public Type lookup(String s);
  public boolean isDefinedLocally(String s);
}
