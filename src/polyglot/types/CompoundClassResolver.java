package jltools.types;

import java.util.*;


public class CompoundClassResolver implements ClassResolver
{
  protected Vector resolvers;

  public CompoundClassResolver()
  {
    resolvers = new Vector();
  }

  public void addClassResolver( ClassResolver cr)
  {
    resolvers.add( cr);
  }

  public ClassType findClass( String name) throws NoClassException
  {
    ClassType clazz;
    ClassResolver cr;

    for( int i = 0; i < resolvers.size(); i++)
    {
      try
      {
        cr = (ClassResolver)resolvers.elementAt( i);
        clazz = cr.findClass( name);
        
        return clazz;
      }
      catch( NoClassException e) {}
    }

    throw new NoClassException( "Class " + name + " not found.");
  }

  public void findPackage( String name) throws NoClassException
  {
    ClassResolver cr;

    for( int i = 0; i < resolvers.size(); i++)
    {
      try
      {
        cr = (ClassResolver)resolvers.elementAt( i);
        cr.findPackage( name);

        return;
      }
      catch( NoClassException e) {}
    }
    
    throw new NoClassException( "Package " + name + " not found.");
  }
}
