
package jltools.util;

public class InternalCompilerError extends Error
{
  public InternalCompilerError( String msg) 
  {
    super ( msg ); 
  }
  public InternalCompilerError(int linenum, String msg) 
  {
    super ( "line " + linenum + ": " + msg ); 
  }
  public InternalCompilerError(jltools.util.AnnotatedObject n, String msg) 
  {
    super ( ((n == null) ? "" : ("line " + Annotate.getLineNumber(n) + ": "))
	    + msg ); 
  }
}
