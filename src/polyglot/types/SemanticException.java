package jltools.types;

import jltools.util.Annotate;
import jltools.util.AnnotatedObject;
import jltools.util.Position;

/**
 * Thrown during any number of phases of the compiler during which a semantic
 * error may be detected.
 */
public class SemanticException extends Exception
{
  protected Position position;
  
  public SemanticException()
  {
    super();
  }

  public SemanticException( Position position)
  {
    super();
    this.position = position;
  }

  public SemanticException( AnnotatedObject a) {
    this( Annotate.getPosition(a));
  }

  public SemanticException( String m)
  {
    super(m);
    this.position = position;
  }

  public SemanticException( String m, Position position)
  {
    super(m);
    this.position = position;
  }

  public SemanticException( String m, AnnotatedObject a) {
    this( m, Annotate.getPosition(a));
  }

  public Position getPosition()
  {
    return position;
  }
}
