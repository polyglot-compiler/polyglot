
package jltools.types;

/**
 * Represents a particular instance of a type (either as a method or a
 * class field of a class). it therefore also has access flags, 
 * since accessflags are tied to specific instances.
 */
public interface TypeInstance
{
  
  /**
   * Returns the access flags if they exist, null otherwise
   */
  public AccessFlags getAccessFlags();

  /**
   * Returns the type associated with this object
   */
  public Type getType();
}
