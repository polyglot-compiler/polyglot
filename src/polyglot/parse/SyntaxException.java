package jltools.parse;

/**
 * Thrown during the parser phase when a syntax error is detected that
 * is not caught be the automatically-generated parser because it accepts
 * a less restricted language.
 */
public class SyntaxException extends Exception
{
  public static final int INVALID_LINE = -1;

  protected int line;
  
  public SyntaxException()
  {
    this( INVALID_LINE);
  }

  public SyntaxException( int line)
  {
    super("Syntax error.");
    this.line = line;
  }

  public SyntaxException( String m)
  {
    this( m, INVALID_LINE);
  }

  public SyntaxException( String m, int line)
  {
    super("Syntax Error: " + m);
    this.line = line;
  }

  public int getLineNumber()
  {
    return line;
  }
}
