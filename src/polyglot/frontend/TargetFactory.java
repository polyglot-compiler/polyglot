package jltools.frontend;

import jltools.types.ClassType;

import java.io.IOException;


public interface TargetFactory
{
  public abstract Target createFileTarget( String fileName) 
    throws IOException;

  public abstract Target createClassTarget( String className) 
    throws IOException;

  public abstract Target createClassTarget( ClassType classType)
    throws IOException;
}
