package jltools.util;


import java.io.Reader;

public interface ErrorQueueFactory
{
  public abstract ErrorQueue createQueue( String filename, Reader source);
}
