/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util;

import java.util.ListIterator;
import java.lang.UnsupportedOperationException;

/**
 * A TypedListIterator is an ListIterator which will not allow members
 * not belonging to a given type to be added to a collection.
 * Optionally, it may also present an immutable view.
 *
 * If an attempt is made to change an immutable listiterator, or if
 * an attempt is made to insert an improperly-typed element, an
 * UnsupportedOperationException is thrown.
 *
 * This class is given so that we can present a ListIterator for a
 * given given class without worrying about outsiders breaking the
 * rep.
 *
 * This is a poor substitute for PolyJ.
 **/
public class TypedListIterator implements ListIterator {
  /**
   * Requires: <iter> not null
   * Creates a new TypedIterator around <iter> which restricts all
   * members to belong to class <c>.  If <c> is null, no typing
   * restriction is made.  If <immutable> is true, no modifications
   * are allowed.
   **/
  public TypedListIterator(ListIterator iter, Class c, boolean immutable) {
    this.immutable = immutable;
    this.allowed_type = c;
    this.backing_iterator = iter;
  }

  /**
   * Gets the allowed type for this ListIterator.
   **/
  public Class getAllowedType(){
    return allowed_type;
  }

  public void add(Object o) {
    tryIns(o);
    backing_iterator.add(o);
  }

  public void set(Object o) {
    tryIns(o);
    backing_iterator.set(o);   
  }

  public boolean hasNext()     { return backing_iterator.hasNext(); }
  public boolean hasPrevious() { return backing_iterator.hasPrevious(); }
  public Object next()         { return backing_iterator.next(); }
  public int nextIndex()       { return backing_iterator.nextIndex(); }
  public Object previous()     { return backing_iterator.previous(); }
  public int previousIndex()   { return backing_iterator.previousIndex(); }
  public void remove()         { 
    if (immutable) 
      throw new UnsupportedOperationException(
			         "Remove from an immutable TypedListIterator");
    
    backing_iterator.remove();
  }

  private final void tryIns(Object o) {
    if (immutable) 
      throw new UnsupportedOperationException(
			         "Add to an immutable TypedListIterator");
    
    if (allowed_type != null && 
	!allowed_type.isAssignableFrom(o.getClass())) {
      String why = "Tried to add a " + o.getClass().getName() +
	" to a list of type " + allowed_type.getName();
      throw new UnsupportedOperationException(why);
    }    
  }

  // RI: allowed_type may be null.
  private Class allowed_type;
  private boolean immutable;
  private ListIterator backing_iterator;
}

