package jltools.types;

import java.util.StringTokenizer;
import jltools.util.*;

/**
 * An <code>AmbiguousType</code> represents an unknown type. It may or may
 * not be full qualified. Ambiguous types are never canonical and never
 * primitive.
 */
public class AmbiguousNameType extends AmbiguousType 
{
  protected Type prefix;
  protected String name;

  protected AmbiguousNameType()
  {
    super();
  }
  
  public AmbiguousNameType(TypeSystem ts, Type prefix, String name) 
  { 
    super(ts);
    this.prefix = prefix;

    StringTokenizer st = new StringTokenizer(name, ".");

    while (st.hasMoreTokens()) {
	String p = st.nextToken();

	if (st.hasMoreTokens()) {
	  this.prefix = new AmbiguousNameType(ts, this.prefix, p);
	}
	else {
	  this.name = p;
	}
    }

    if (this.name == null) {
      throw new InternalCompilerError("null ambiguous name type");
    }
  }

  public AmbiguousNameType(TypeSystem ts, String name) 
  { 
    this(ts, null, name);
  }
  
  public Type getPrefix() 
  {
    return prefix;
  }

  public String getName() 
  {
    return name;
  }

  public boolean isShort()
  {
    return prefix == null;
  }
  
  public String getTypeString() 
  {
    return prefix == null ? name : prefix.getTypeString() + "." + name;
  }
}


