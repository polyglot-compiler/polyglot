

package jltools.util;

import java.util.*;
import jltools.types.*;

/**
 * Set containing a list of types.  adds a type to the set if only if no supertype of the set is in the class.
 */
public class SubtypeSet implements java.util.Set
{
  Vector v; 
  public  SubtypeSet()
  {
    v = new Vector();
  }

  public boolean add( Object o)
  {
    boolean bHaveToAdd = true;

    if ( o == null) return false;
    
    if (o instanceof jltools.ast.TypeNode)
      o = ((jltools.ast.TypeNode)o).getType();

    try
    {
      if (( o instanceof Type) && (( Type )o).isThrowable() )
      {
        for (Iterator i = v.iterator(); i.hasNext() ; )
        {
          Type t = (Type)i.next();
          if ( t.descendsFrom( (Type)o) )
          {
            i.remove();
          }
          if ( ((Type)o).descendsFrom( t))
          {
            bHaveToAdd = false;
            break;
          }
        }
      if (bHaveToAdd)
        v.add( o);
      return bHaveToAdd;
      }
      else 
      {
        throw new ClassCastException("Can only add types to the set");
      }
    }
    catch ( TypeCheckException tce) 
    {
      throw new IllegalArgumentException(" Cannont perform typesystem operations: " + tce.getMessage());
    }

  }

  public boolean addAll( Collection c )
  {
    if ( c == null) return false;
    boolean bChanged = false;
    for (Iterator i = c.iterator() ; i.hasNext() ; )
      bChanged |= add ( i.next()) ;
    return bChanged;
  }

  public void  clear()
  {
    v.clear();
  }

  public boolean contains(Object o)
  {
    if ( o instanceof Type)
    {
      try
      {
        for (Iterator i = v.iterator(); i.hasNext() ; )
        {
          Type t = (Type)i.next();
          if (((Type)o).descendsFrom ( t ) ||
              ((Type)o).equals(t))
            return true;
        }
      }
      catch (TypeCheckException tce ) 
      {
        return false;
      }
    }
    return false;
  }

  public boolean containsAll(Collection c)
  {
    for (Iterator i = c.iterator() ; i.hasNext() ; )
      if (! contains (i.next()))
        return false;
    return true;
  }

  public boolean isEmpty()
  {
    return v.isEmpty();
  }
  
  public Iterator iterator()
  {
    return v.iterator();
  }

  public boolean remove(Object o )
  {
    boolean bContains = contains ( o );
    v.remove (o );
    return bContains;
  }
  
  public boolean removeAll(Collection c) 
  {
    throw new UnsupportedOperationException( " Not supported" );
  }

  public boolean retainAll(Collection c) 
  {
    throw new  UnsupportedOperationException( " Not supported" );
  }
  public int size()
  {
    return v.size();
  }

  public Object[] toArray()
  {
    return v.toArray();
  }

  public Object[] toArray(Object[] a)
  {
    return v.toArray(a);
  }
}
