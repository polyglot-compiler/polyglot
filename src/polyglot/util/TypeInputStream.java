package polyglot.util;

import polyglot.main.Report;
import polyglot.types.*;

import java.io.*;

/** Input stream for reading type objects. */
public class TypeInputStream extends ObjectInputStream
{
  protected TypeSystem ts;

  public TypeInputStream( InputStream in, TypeSystem ts) 
    throws IOException
  {
    super( in);
    enableResolveObject(true);
    this.ts = ts;
  }

  public TypeSystem getTypeSystem()
  {
    return ts;
  }

  protected Object resolveObject(Object o) {
    if (Report.should_report("serialize", 2)) {
      String s;
      try {
        s = o.toString();
      } catch (NullPointerException e) {
        s = "<NullPointerException thrown>";
      }
      Report.report(2, "- " + s + " : " + o.getClass());
    }
    return o;
  }
}
