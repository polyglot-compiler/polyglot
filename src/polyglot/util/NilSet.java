/*
 * NilSet.java
 */

package jltools.util;

import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.lang.UnsupportedOperationException;

/**
 * This class represents a constant set which never contains any elements.
 **/
public final class NilSet implements Set {
  public static final NilSet Member = new NilSet();

  public NilSet() {}
 
  public boolean contains(Object key) { return false; }
  public boolean containsAll(Collection c) { return c.size() == 0; }
  public boolean equals(Object o) 
    { return (o instanceof Set) && ((Set) o).size() == 0; }
  public int hashCode() { return 0; }
  public boolean isEmpty() { return true; }
  public Iterator iterator() { return NilIterator.Member; }
  public int size() { return 0; }
  public Object[] toArray() { return new Object[0]; }
  public Object[] toArray(Object[] oa) { 
    if (oa.length > 0) oa[0] = null;
    return oa;
  }

  public boolean add(Object o) {
    throw new UnsupportedOperationException();
  }
  public boolean addAll(Collection c) {
    throw new UnsupportedOperationException();
  }
  public void clear() {
    throw new UnsupportedOperationException();
  }
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }
  public boolean removeAll(Collection c) {
    throw new UnsupportedOperationException();
  }
  public boolean retainAll(Collection c) {
    throw new UnsupportedOperationException();
  }
}

