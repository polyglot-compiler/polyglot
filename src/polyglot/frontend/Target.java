package jltools.frontend;

import java.io.*;

public class Target
{
  protected String name;
  protected Reader source;
  protected Writer output;
  
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
  
  public Writer getOutputWriter( String packageName) throws IOException
  {
    return output;
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
    
