
package jltools.util;

public class ErrorLimitError extends RuntimeException
{
  public ErrorLimitError( String msg)
  {
    super( msg);
  }

  public ErrorLimitError()
  {
    super();
  }
}
