/*
 * NilIterator.java
 */

package jltools.util;

import java.util.Iterator;
import java.lang.UnsupportedOperationException;

/**
 * This class represents a constant iterator which never yields any elements.
 **/
public final class NilIterator implements Iterator {
  public static final NilIterator Member = new NilIterator();

  public NilIterator() {}

  public boolean hasNext() { return false; }
  public Object next() {
    throw new java.util.NoSuchElementException();
  }
  public void remove() {
    throw new UnsupportedOperationException();
  }
}

