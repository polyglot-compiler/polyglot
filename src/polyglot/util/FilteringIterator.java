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

import java.util.Iterator;
import java.util.Collection;

/**
 * FilteringIterator
 *
 * Overview:
 *     This iterator wraps another iterator, and returns only those elements
 *     for which a given predicate is true.  
 *
 *     Does not support Remove.
 **/
public final class FilteringIterator<T> implements Iterator<T> {
    /**
     * Constructs a new FilteringIterator which returns only those elements of
     * <coll> which have <pred> true.
     **/
    public FilteringIterator(Collection<T> coll, Predicate<T> pred) {
        this(coll.iterator(), pred);
    }

    /**
     * Constructs a new FilteringIterator which returns all the elements
     * of <iter>, in order, only when they have <pred> true.
     **/
    public FilteringIterator(Iterator<T> iter, Predicate<T> pred) {
        backing_iterator = iter;
        predicate = pred;
        findNextItem();
    }

    @Override
    public T next() {
        T res = next_item;
        if (res == null) throw new java.util.NoSuchElementException();
        findNextItem();
        return res;
    }

    @Override
    public boolean hasNext() {
        return next_item != null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("FilteringIterator.remove");
    }

    // Advances the internal iterator.
    private void findNextItem() {
        while (backing_iterator.hasNext()) {
            T o = backing_iterator.next();
            if (predicate.isTrue(o)) {
                next_item = o;
                return;
            }
        }
        next_item = null;
    }

    // AF:  if next_item==null, this iterator has no more elts to yield.
    //      otherwise, this iterator will yield next_item, followed by
    //      those elements e of backing_iterator such that predicate.isTrue(e).
    protected T next_item;
    protected Iterator<T> backing_iterator;
    protected Predicate<T> predicate;
}
