package jltools.util;

public abstract class ErrorQueue
{
  protected boolean hasErrors;

  public ErrorQueue()
  {
    hasErrors = false;
  }

  public void enqueue( int type, String message)
  {
    enqueue( type, message, -1);
  }

  public void enqueue( int type, String message, int lineNumber)
  {
    enqueue( new ErrorInfo( type, message, lineNumber));
  }

  public abstract void enqueue( ErrorInfo e);

  public boolean hasErrors()
  {
    return hasErrors;
  }

  public void flush() 
  {
  }
}
