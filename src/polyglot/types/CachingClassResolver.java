package jltools.types;

import java.util.*;

public class CachingClassResolver implements ClassResolver
{
  protected ClassResolver resolver;
  protected Map cache;

  public CachingClassResolver( ClassResolver resolver)
  {
    this.resolver = resolver;
    
    this.cache = new HashMap();
  }

  public ClassType findClass( String name) throws SemanticException
  {
    ClassType ct = (ClassType)cache.get( name);

    if( ct != null) {
      return ct;
    }
    else {
      ct = resolver.findClass( name);

      cache.put( name, ct);
      return ct;
    }
  }

  public void findPackage( String name) throws NoClassException
  {
    resolver.findPackage( name);
  }
}
