package jltools.frontend;

import jltools.util.*;

import java.io.*;


public class MainErrorQueueFactory implements ErrorQueueFactory
{
  private static final int ERROR_COUNT_LIMIT = 98;

  private PrintStream err;

  public MainErrorQueueFactory()
  {
    this( System.err);
  }

  public MainErrorQueueFactory( PrintStream err)
  {
    this.err = err;
  }

  public ErrorQueue createQueue( String filename, Reader source)
  {
    return new MainErrorQueue( filename, source);
  }

  class MainErrorQueue extends ErrorQueue
  {
    private String filename;
    private Reader source;
    private int errorCount;
    private boolean flushed;

    public MainErrorQueue( String filename, Reader source) 
    {
      this.filename = filename;
      this.source = source;
      this.errorCount = 0;
      this.flushed = true;
    }

    public void enqueue( ErrorInfo e)
    {
      hasErrors = true;
      errorCount++;
      flushed = false;

      if( e.getLineNumber() == -1) {
        err.println( filename + ": " + e.getMessage());
      } 
      else {
        err.println( filename + ":" +  e.getLineNumber() + ": " 
                     + e.getMessage());
      }

      if( errorCount > ERROR_COUNT_LIMIT) {
        err.println( filename + ": Too many errors. Aborting compilation.");
        flush();
        throw new ErrorLimitError();
      }
    }

    public void flush()
    {
      if( hasErrors && !flushed) {
        err.println( filename + ": " + errorCount + " error" 
                     + (errorCount > 1 ? "s." : "."));
        flushed = true;
      }
    }
  }
}
