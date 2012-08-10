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

import java.util.Collection;
import java.util.Iterator;

/**
 * ConcatenatedIterator
 *
 * Overview:
 *     This iterator wraps other iterators, and returns all their elements
 *     in order.
 *
 *     Does not support Remove.
 **/
public final class ConcatenatedIterator<T> implements Iterator<T> {

    /**
     * Constructs a new ConcatenatedIterator which yields every element, in
     *  order, of every element of the array iters, in order.
     **/
    public ConcatenatedIterator(Iterator<T>... iters) {
        this.backing_iterators = iters.clone();
        findNextItem();
    }

    /**
     * Constructs a new ConcatenatedIterator which yields every element, in
     * order, of every element of the collection iters, in order.
     **/
    @SuppressWarnings("unchecked")
    public ConcatenatedIterator(Collection<T> iters) {
        this.backing_iterators = (Iterator<T>[]) iters.toArray();
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
        throw new UnsupportedOperationException("ConcatenatedIterator.remove");
    }

    // Advances the internal iterator.
    private void findNextItem() {
        while (index < backing_iterators.length) {
            Iterator<T> it = backing_iterators[index];
            if (it.hasNext()) {
                next_item = it.next();
                return;
            }
            else {
                index++;
            }
        }
        next_item = null;
    }

    // AF:  if next_item==null, this iterator has no more elts to yield.
    //      otherwise, this iterator will yield next_item, followed by the 
    //      remaining elements of backing_iterators[index], followed by the
    //      elements of backing_iterators[index+1]...
    protected T next_item;
    protected Iterator<T>[] backing_iterators;
    protected int index;
}
