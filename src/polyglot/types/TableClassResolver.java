package jltools.types;

import java.util.*;

public class TableClassResolver implements ClassResolver
{
  protected Map table;

  public TableClassResolver()
  {
    table = new HashMap();
  }
  
  public void addClass( String fullName, JavaClass clazz)
  {
    table.put( fullName, clazz);
  }

  public JavaClass findClass( String name) throws NoClassException
  {
    JavaClass clazz = (JavaClass)table.get( name);
    if( clazz == null)
      throw new NoClassException( "Class " + name + " not found.");
    return clazz;
  }

  public void findPackage( String name) throws NoClassException {}
}
