/**
 * TypeCheck.java
 */

package jltools.types;

/**
 * Thrown when the typesystem notices a type error.
 */
public class TypeCheckError extends Error
{
  public TypeCheckError(String m)
  {
    super(m);
  }
}
