package jltools.types;

/**
 * Thrown during any number of phases of the compiler during which a semantic
 * error may be detected.
 */
public class SemanticException extends Exception
{
  public static final int INVALID_LINE = -1;

  protected int line;
  
  public SemanticException()
  {
    this( INVALID_LINE);
  }

  public SemanticException( int line)
  {
    super();
    this.line = line;
  }

  public SemanticException( String m)
  {
    this( m, INVALID_LINE);
  }

  public SemanticException( String m, int line)
  {
    super(m);
    this.line = line;
  }

  public int getLineNumber()
  {
    return line;
  }
}
