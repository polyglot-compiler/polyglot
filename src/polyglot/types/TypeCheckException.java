/**
 * TypeCheck.java
 */

package jltools.types;

/**
 * Thrown when the typesystem notices a type error.
 */
public class TypeCheckException extends Exception
{
  public TypeCheckException()
  {
    super();
  }
  public TypeCheckException(String m)
  {
    super(m);
  }
}
