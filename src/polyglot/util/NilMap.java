/*
 * NilMap.java
 */

package jltools.util;

import java.util.Map;
import java.util.Collection;
import java.util.Set;

/**
 * This class represents a constant map which never contains any elements.
 **/
public final class NilMap implements Map {
  public static final NilMap Member = new NilMap();

  public NilMap() {}

  public boolean containsKey(Object key)   { return false; }
  public boolean containsValue(Object val) { return false; }
  public Set entrySet() { return NilSet.Member; }
  public boolean equals(Object o) 
    { return (o instanceof Map) && ((Map) o).size() == 0 ; }
  public int hashCode() { return 0; }
  public boolean isEmpty() { return true; }
  public Set keySet() { return NilSet.Member; }
  public int size() { return 0; }
  public Collection values() { return NilSet.Member; }
  public Object get(Object k) { return null; }

  public void clear() {
    throw new UnsupportedOperationException();
  }
  public Object put(Object o1, Object o2) {
    throw new UnsupportedOperationException();
  }
  public void putAll(Map t) {
    throw new UnsupportedOperationException();
  }
  public Object remove(Object o) {
    throw new UnsupportedOperationException();
  }
}

