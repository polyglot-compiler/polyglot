package jltools.frontend;

import jltools.ast.*;
import jltools.util.*;

import java.io.*;


public abstract class Target
{
  protected String name;
  protected Reader source;
  protected Writer output;
  protected ErrorQueue eq = null;
  
  public Target( String name, Reader source, Writer output)
  {
    this.name = name;
    this.source = source;
    this.output = output;
  }

  public String getName()
  {
    return name;
  }
  
  public Reader getSourceReader() throws IOException
  {
    return source;
  }

  public abstract java_cup.runtime.lr_parser getParser() throws IOException;
  
  public Writer getOutputWriter( String packageName) throws IOException
  {
    return output;
  }

  public ErrorQueue getErrorQueue() throws IOException
  {
    if( eq == null) {
      eq = createErrorQueue();
    }
    return eq;
  }

  abstract public void closeSource() throws IOException;
  abstract public void closeDestination() throws IOException;
  

  protected abstract ErrorQueue createErrorQueue() throws IOException;

  public NodeVisitor getNextNodeVisitor( int stage)
  {
    return null;
  }

  public boolean equals( Object o)
  { 
    if( o instanceof Target) {
      return name.equals( ((Target)o).name);
    }
    else {
      return false;
    }
  }
}
    
