package jltools.util;

import java.io.*;


public class TypeOutputStream extends ObjectOutputStream
{
  protected boolean atRoot = true;
  
  public TypeOutputStream( OutputStream out) throws IOException
  {
    super( out);
  }
  
  public boolean atRootType()
  {
    if( atRoot) {
        atRoot = false;
        return true;
    }
    else {
      return false;
    }
  }
}
