package jltools.types;


public class LoadedClassResolver implements ClassResolver
{
  protected TypeSystem ts;

  public LoadedClassResolver()
  {
    this( null);
  }

  public LoadedClassResolver( TypeSystem ts)
  {
    this.ts = ts;
  }

  public void setTypeSystem( TypeSystem ts)
  {
    this.ts = ts;
  }

  public JavaClass findClass( String name) throws NoClassException
  {
    Class clazz;

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

    return new LoadedJavaClass( clazz, ts);
  }

  public void findPackage( String name) throws NoClassException {}
}
