package jltools.util;

import jltools.types.*;

import java.io.*;
import java.util.Set;


public class TypeOutputStream extends ObjectOutputStream
{
  protected TypeSystem ts;
  protected Set roots;
  
  public TypeOutputStream( OutputStream out, TypeSystem ts, Type root) 
    throws IOException
  {
    super( out);

    this.ts = ts;
    this.roots = ts.getTypeEncoderRootSet(root);

    enableReplaceObject( true);
  }

  protected Object replaceObject(Object o) throws IOException
  {
    if (roots.contains(o)) {
      return o;
    }
    else if (o instanceof TypeObject) {
      return ts.placeHolder((TypeObject) o);
    }
    else {
      return o;
    }
  }
}
