package polyglot.util;

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

    this.ts = ts;
  }

  public TypeSystem getTypeSystem()
  {
    return ts;
  }
}
