package jltools.types;

import jltools.frontend.Target;

import java.io.IOException;


public interface TargetTable
{
  public abstract ClassResolver getResolver( Target t) throws IOException;
}
