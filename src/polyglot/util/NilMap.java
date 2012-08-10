/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework. Copyright
 * (c) 2000-2008 Polyglot project group, Cornell University Copyright (c)
 * 2006-2008 IBM Corporation All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html This program and the accompanying
 * materials are made available under the terms of the Lesser GNU Public License
 * v2.0 which accompanies this distribution. The development of the Polyglot
 * project has been supported by a number of funding sources, including DARPA
 * Contract F30602-99-1-0533, monitored by USAF Rome Laboratory, ONR Grant
 * N00014-01-1-0968, NSF Grants CNS-0208642, CNS-0430161, and CCF-0133302, an
 * Alfred P. Sloan Research Fellowship, and an Intel Research Ph.D. Fellowship.
 * See README for contributors.
 ******************************************************************************/

package polyglot.util;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * This class represents a constant map which never contains any elements.
 **/
public final class NilMap<K, V> implements Map<K, V> {
    public static final NilMap<?, ?> EMPTY_MAP = new NilMap<Object, Object>();

    @SuppressWarnings("unchecked")
    public static final <K, V> NilMap<K, V> emptyMap() {
        return (NilMap<K, V>) EMPTY_MAP;
    }

    private NilMap() {
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object val) {
        return false;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Collection<V> values() {
        return Collections.emptySet();
    }

    @Override
    public V get(Object k) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Map) && ((Map<?, ?>) o).size() == 0;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(K o1, V o2) {
        throw new UnsupportedOperationException();
    }
}
