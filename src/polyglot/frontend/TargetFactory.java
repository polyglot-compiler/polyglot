package jltools.frontend;

import java.io.IOException;


public interface TargetFactory
{
  public abstract void addSourceDirectory( Target t, String packageName)
       throws IOException;

  public abstract Target createFileTarget( String fileName) 
       throws IOException;

  public abstract Target createClassTarget( String className) 
       throws IOException;
}
