package jltools.types;

import java.util.StringTokenizer;
import jltools.util.*;

/**
 * An <code>PackageType</code> represents a package type. It may or may
 * not be fully qualified. Package types are never canonical and never
 * primitive.
 */
public class PackageType extends Type 
{
  PackageType prefix;
  String name;

  protected PackageType()
  {
    super();
  }
  
  public PackageType(TypeSystem ts, PackageType prefix, String name) 
  { 
    super(ts);

    this.prefix = prefix;

    StringTokenizer st = new StringTokenizer(name, ".");

    while (st.hasMoreTokens()) {
	String p = st.nextToken();

	if (st.hasMoreTokens()) {
	  this.prefix = new PackageType(ts, this.prefix, p);
	}
	else {
	  this.name = p;
	}
    }

    if (this.name == null) {
      throw new InternalCompilerError("null package type");
    }
  }

  public PackageType(TypeSystem ts, String name) 
  { 
    this(ts, null, name);
  }
  
  public PackageType getPrefix() 
  {
    return prefix;
  }

  public String getName() 
  {
    return name;
  }
  
  public String translate(LocalContext c) {
    return getTypeString();
  }

  public String getTypeString() 
  {
    return prefix == null ? name : prefix.getTypeString() + "." + name;
  }

  public boolean isPrimitive()
  {
    return false;
  }
  public boolean isReferenceType()
  {
    return false;
  }
  public boolean isClassType()
  {
    return false;
  }
  public boolean isArrayType()
  {
    return false;
  }
  public boolean isPackageType()
  {
    return true;
  }
  public boolean isCanonical()
  {
    return false;
  }
}
