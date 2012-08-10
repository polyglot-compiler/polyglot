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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This subclass of TransformingList applies the transformation to each
 * element of the underlying list at most once.
 */
public class CachingTransformingList<T, U> extends TransformingList<T, U> {
    protected ArrayList<U> cache;

    public CachingTransformingList(Collection<? extends T> underlying,
            Transformation<T, U> trans) {
        this(new ArrayList<T>(underlying), trans);
    }

    public CachingTransformingList(List<T> underlying,
            Transformation<T, U> trans) {
        super(underlying, trans);
        cache =
                new ArrayList<U>(Collections.<U> nCopies(underlying.size(),
                                                         null));
    }

    @Override
    public U get(int index) {
        U o = cache.get(index);
        if (o == null) {
            o = trans.transform(underlying.get(index));
            cache.set(index, o);
        }
        return o;
    }
}
