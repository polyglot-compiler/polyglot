package jltools.util;

import jltools.types.*;

import java.io.*;


public class TypeInputStream extends ObjectInputStream
{
  protected TypeSystem ts;

  public TypeInputStream( InputStream in, TypeSystem ts) 
    throws IOException
  {
    super( in);

    this.ts = ts;
  }

  public TypeSystem getTypeSystem()
  {
    return ts;
  }
}
