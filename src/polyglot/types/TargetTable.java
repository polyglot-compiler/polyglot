package jltools.types;

import jltools.frontend.Target;

import java.io.IOException;


public interface TargetTable
{
  public abstract ClassResolver getResolver( Target t) throws IOException;
  
  public abstract void addTarget( ClassType clazz) throws IOException;
}
