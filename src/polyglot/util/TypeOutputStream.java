package polyglot.util;

import polyglot.types.*;

import java.io.*;
import java.util.Set;

/** Output stream for writing type objects. */
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

    //System.out.println("------------------------------");
    //System.out.println("roots: " + roots);

    enableReplaceObject( true);
  }

  protected Object replaceObject(Object o) throws IOException
  {
    if (roots.contains(o)) {
      //System.out.println("+ In roots: " + o + " : " + o.getClass());
      return o;
    }
    else if (o instanceof TypeObject) {
      //System.out.println("+ Replacing: " + o + " : " + o.getClass());
      return ts.placeHolder((TypeObject) o, roots);
    }
    else {
      //System.out.println("+ " + o + " : " + o.getClass());
      return o;
    }
  }
}
