package jltools.parse;

import jltools.util.Position;

/**
 * Thrown during the parser phase when a syntax error is detected that
 * is not caught be the automatically-generated parser because it accepts
 * a less restricted language.
 */
public class SyntaxException extends Exception
{
  protected Position position;
  
  public SyntaxException()
  {
    this("Syntax error.", null);
  }

  public SyntaxException(Position position)
  {
    this("Syntax error.", position);
  }

  public SyntaxException(String m)
  {
    this(m, null);
  }

  public SyntaxException(String m, Position position)
  {
    super("Syntax error: " + m);
    this.position = position;
  }

  public Position getPosition()
  {
    return position;
  }
}
