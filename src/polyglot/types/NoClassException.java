package jltools.types;

/**
 * Signals an error in the class resolver system. This exception is thrown
 * when a <code>ClassResovler</code> is unable to resolve a given class.
 */
public class NoClassException extends SemanticException 
{
  public NoClassException() 
  {
  }

  public NoClassException( String s) 
  {
    super(s); 
  }
}
