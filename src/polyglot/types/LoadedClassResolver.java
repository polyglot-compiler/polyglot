package jltools.types;

import java.util.Hashtable;

public class LoadedClassResolver implements ClassResolver
{
  protected TypeSystem ts;
  Hashtable htCache;

  public LoadedClassResolver()
  {
    this( null);
    htCache = new Hashtable();
  }

  public LoadedClassResolver( TypeSystem ts)
  {
    this.ts = ts;
    htCache = new Hashtable();
  }

  public void setTypeSystem( TypeSystem ts)
  {
    this.ts = ts;
  }

  public ClassType findClass( String name) throws SemanticException
  {
    Class clazz;
    ClassType t; 

    if( ts == null) {
      throw new NoClassException( "No type system in place.");
    }

    try
    {
      clazz = Class.forName( name);
    }
    catch( Exception e)
    {
      throw new NoClassException( "Class " + name + " not found.");
    }

    if ( ( t = (ClassType)htCache.get(clazz)) != null) return t;
    t = new LoadedClassType( clazz, ts);
    htCache.put( clazz, t);
    return t;    
  }

  public void findPackage( String name) throws NoClassException {}
}
