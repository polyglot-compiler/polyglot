package jltools.types;

import jltools.util.*;
import jltools.ast.TypeNode;

import java.io.IOException;
import java.util.*;


public class TableClassResolver implements ClassResolver
{
  protected Map table;
  protected List queue;
  protected LinkedList dirty;

  public TableClassResolver()
  {
    table = new HashMap();
    queue = new LinkedList();
    dirty = new LinkedList();
  }
  
  public void addClass( String fullName, ClassType clazz)
  {
    table.put( fullName, clazz);
    queue.add( clazz);
    dirty.add( clazz);
  }

  /**
   * Adds all the classes found in <code>other</code> to the current table.
   * 
   * @post <code>other</code>remains unchanged.
   */
  public void include( TableClassResolver other)
  {
    for( Iterator iter = other.table.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry entry = (Map.Entry)iter.next();
      table.put( entry.getKey(), entry.getValue());
    }
  }

  public Iterator classes() 
  {
    return queue.iterator();
  }

  public void cleanupSignatures() throws SemanticException {
      while (! dirty.isEmpty()) {
	  ClassType ct = (ClassType) dirty.removeFirst();
	  ct.getTypeSystem().cleanClass(ct);
      }
  }

  public boolean containsClass( String name)
  {
    return table.containsKey( name);
  }

  public ClassType findClass( String name) throws NoClassException
  {
    ClassType clazz = (ClassType)table.get( name);
    if( clazz == null)
      throw new NoClassException( "Class \"" + name + "\" not found.");
    return clazz;
  }
}
