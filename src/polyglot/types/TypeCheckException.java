/**
 * TypeCheck.java
 */

package jltools.types;

/**
 * Thrown when the typesystem notices a type error.
 */
public class TypeCheckException extends Exception
{
  public static final int INVALID_LINE = -1;
  int line;
  
  public TypeCheckException()
  {
    super();
    line = INVALID_LINE;
  }
  public TypeCheckException(String m)
  {
    super(m);
    line = INVALID_LINE;
  }
  public TypeCheckException(String m, int line)
  {
    super(m);
    this.line = line;
  }
  public int getLineNumber()
  {
    return line;
  }
}
