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
 * TransformingIterator
 *
 * Overview:
 *     This is a swiss-army-knife of iterators.  It concatenates, maps, and
 *     filters.  
 *
 *     Does not support Remove.
 **/
public final class TransformingIterator<T, U> implements Iterator<U> {
    @SuppressWarnings("unchecked")
    public TransformingIterator(Iterator<T> iter, Transformation<T, U> trans) {
        this(new Iterator[] { iter }, trans);
    }

    public TransformingIterator(Collection<Iterator<T>> iters,
            Transformation<T, U> trans) {
        index = 0;
        @SuppressWarnings("unchecked")
        Iterator<T>[] tmp = new Iterator[0];
        backing_iterators = iters.toArray(tmp);
        transformation = trans;
        if (backing_iterators.length > 0) current_iter = backing_iterators[0];
        findNextItem();
    }

    public TransformingIterator(Iterator<T>[] iters, Transformation<T, U> trans) {
        index = 0;
        backing_iterators = iters.clone();
        transformation = trans;
        if (iters.length > 0) current_iter = iters[0];
        findNextItem();
    }

    @Override
    public U next() {
        U res = next_item;
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
        throw new UnsupportedOperationException("TransformingIterator.remove");
    }

    // Advances the internal iterator.
    private void findNextItem() {
        while (current_iter != null) {
            inner_loop: while (current_iter.hasNext()) {
                T o = current_iter.next();
                U res = transformation.transform(o);
                if (res == Transformation.NOTHING) continue inner_loop;
                next_item = res;
                return;
            }
            index++;
            if (index < backing_iterators.length) {
                current_iter = backing_iterators[index];
            }
            else {
                current_iter = null;
            }
        }
        next_item = null;
    }

    // AF:  if next_item==null, this iterator has no more elts to yield.
    //      otherwise, this iterator will yield next_item, followed by
    //      those elements e of backing_iterator[index] transformed by TRANS.
    // RI: current_iter = backing_iterators[index], or null if no 
    //     backing_iterator hasNext.
    protected U next_item;
    protected Iterator<T> current_iter;
    protected int index;
    protected Iterator<T>[] backing_iterators;
    protected Transformation<T, U> transformation;
}
