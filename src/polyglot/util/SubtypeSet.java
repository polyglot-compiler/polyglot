

package jltools.util;

import java.util.*;
import jltools.types.*;

/**
 * Class to implement sets containing <code>jltools.types.Type </code>.  
 * Set membership is based on the subtype relationships.  Thus, if 
 * <code>S</code> is a supertype of <code>A</code> and <code>B</code>, then
 * { <code>S</code> } union { <code>A</code>,<code>B</code> } = 
 * { <code>S</code> }.  Similarily, we remove elements from the set such 
 * that if <code>s</code> is an element of a set <code>S</code>, then a
 * call to remove <code>r</code> removes all <code>s</code> iff r is a 
 * a supertype of s.
 */
public class SubtypeSet implements java.util.Set
{
  Vector v; 
  /**
   * Creates an empty SubtypeSet
   */
  public  SubtypeSet()
  {
    v = new Vector();
  }

  /**
   * Add an element of type <code>jltools.types.Type</code> to the set
   * only if it has no supertypes already in the set. If we do add it, 
   * remove any subtypes of <code>o</code>
   * 
   * @param o The element to add.
   */
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
          if ( ((Type)o).descendsFrom( t) || ((Type)o).equals ( t) )
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
    catch ( SemanticException tce) 
    {
      throw new IllegalArgumentException(" Cannont perform typesystem operations: " + tce.getMessage());
    }

  }

  /**
   * Adds all elements from c into this set.
   */
  public boolean addAll( Collection c )
  {
    if ( c == null) return false;
    boolean bChanged = false;
    for (Iterator i = c.iterator() ; i.hasNext() ; )
      bChanged |= add ( i.next()) ;
    return bChanged;
  }

  /**
   * Removes all elements from the set
   */
  public void  clear()
  {
    v.clear();
  }

  /**
   * Check whether object <code>o</code> is in the set. Because of the 
   * semantics of the subtype set, <code>o</code> is in the set iff
   * it descends from (or is equal to) one of the elements in the set.
   */
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
      catch (SemanticException tce ) 
      {
        return false;
      }
    }
    return false;
  }

  /**
   * Checks whether all elements of the collection are in the set
   */
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

  /**
   * Removes all elements <code>s</code> in the set such that 
   * <code>s</code> decends from <code>o</code>
   *
   * @return whether or not an element was removed.
   */
  public boolean remove(Object o )
  {
    if ( ! (o instanceof Type ))
      throw new ClassCastException("Can only add types to the set");

    boolean bRemoved = false;
    for (Iterator i = v.iterator(); i.hasNext() ; ) 
    {
      Type t = (Type)i.next();
      try
      {
        if ( t.equals(o) || t.descendsFrom ( (Type)o ) )
        {
          bRemoved = true;
          i.remove() ; 
        }      
      }
      catch ( SemanticException tce) 
      {
        throw new IllegalArgumentException(" Cannont perform typesystem operations: " + 
                                           tce.getMessage());
      }
    }
    return bRemoved;
  }
  
  public boolean removeAll(Collection c) 
  {
    throw new UnsupportedOperationException( " Not supported" );
  }

  public boolean retainAll(Collection c) 
  {
    throw new UnsupportedOperationException( " Not supported" );
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
