
package jltools.types;

/**
 * Provides a wrapper to abstract the LocalContext and Import Context
 */
public interface Context 
{
  /**
   * Finds the type of a particular string within a context. If it is not found
   * in this context, checks the parent context.
   */
  public TypeInstance lookup(String s) throws TypeCheckError;

  /**
   * Returns whether the particular symbol is defined within the current method scope. 
   * If it isn't in this scope, we ask the parent scope, as long as it is not 
   * a ClassContext
   */
  public boolean isDefinedLocally(String s);
}
