package polyglot.util;

import polyglot.frontend.Serialize;
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

    if (Serialize.should_report(2)) {
        Serialize.report(2, "Began TypeOutputStream with roots: " + roots);
    }
    
    enableReplaceObject( true);
  }

  protected Object replaceObject(Object o) throws IOException
  {
    if (roots.contains(o)) {
      if (Serialize.should_report(2)) {
	Serialize.report(2, "+ In roots: " + o + " : " + o.getClass());
      }
      return o;
    }
    else if (o instanceof TypeObject) {
      Object r = ts.placeHolder((TypeObject) o, roots);
      if (Serialize.should_report(2)) {
        if (r != o) {
          Serialize.report(2, "+ Replacing: " + o + " : " + o.getClass()
	    + " with " + r);
        } 
	else {
	  Serialize.report(2, "+ " + o + " : " + o.getClass());
        }
      }
      return r;
    }
    else {
      if (Serialize.should_report(2)) {
	Serialize.report(2, "+ " + o + " : " + o.getClass());
      }
      return o;
    }
  }
}
