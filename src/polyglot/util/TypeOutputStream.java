package jltools.util;

import jltools.types.*;

import java.io.*;


public class TypeOutputStream extends ObjectOutputStream
{
  protected TypeSystem ts;
  protected Type root;
  
  public TypeOutputStream( OutputStream out, TypeSystem ts, Type root) 
    throws IOException
  {
    super( out);

    this.ts = ts;
    this.root = root;

    enableReplaceObject( true);
  }

  protected Object replaceObject( Object o) throws IOException
  {
    if( o == root) {
      return o;
    }
    else if( o instanceof ClassType) {
      return new AmbiguousType( ts, ((ClassType)o).getTypeString());
    }
    else {
      return o;
    }
  }
}
