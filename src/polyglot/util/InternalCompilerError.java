
package jltools.util;

public class InternalCompilerError extends Error
{
  public InternalCompilerError( String msg) 
  {
    super ( msg ); 
  }
  public InternalCompilerError(Position position, String msg) 
  {
    super ( position == null ? msg : position + ": " + msg );
  }
  public InternalCompilerError(jltools.util.AnnotatedObject n, String msg) 
  {
    this( n == null ? null : Annotate.getPosition(n), msg );
  }
}
