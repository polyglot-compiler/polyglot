package jltools.frontend;

import java.io.IOException;


public interface TargetFactory
{
  public abstract Target createFileTarget( String fileName) 
       throws IOException;

  public abstract Target createClassTarget( String className) 
       throws IOException;
}
