/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@code MultiMap} is a map that allows the same key to be used to map
 * to multiple values.
 * It is therefore an arbitrary binary relation on keys and values.
 */
public class MultiMap<K, V> extends AbstractMap<K, Set<V>> {
    /* The representation is a mapping from each key to the set of values that
     * that key maps to. */
    private Map<K, Set<V>> map = new HashMap<>();

    private Set<V> getValueSet(K key) {
        Set<V> values = map.get(key);
        if (values == null) {
            values = new HashSet<>();
            map.put(key, values);
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, Set<V>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Set<V> values : map.values()) {
            if (values.contains(values)) return true;
        }
        return false;
    }

    @Override
    public Set<V> get(Object key) {
        return map.get(key);
    }

    /** The set of values associated with a key. Not modifiable. */
    public Set<V> getValues(Object key) {
        Set<V> s = map.get(key);
        if (s == null) {
            return Collections.<V> emptySet();
        }
        else {
            return Collections.unmodifiableSet(s);
        }
    }

    /**
     * Makes {@code key} map to {@code value}, where {@code value}
     * has to be a collection of objects.
     */
    @Override
    public Set<V> put(K key, Set<V> value) {
        Set<V> original = map.get(key);
        Set<V> values = new HashSet<>();
        values.addAll(value);
        map.put(key, values);
        return original;
    }

    /**
     * Adds a single value into the set associated with {@code key}.
     */
    public boolean add(K key, V value) {
        return getValueSet(key).add(value);
    }

    /**
     * Adds all the values in {@code values} into the set associated with
     * {@code key}.
     */
    public boolean addAll(K key, Collection<V> values) {
        return getValueSet(key).addAll(values);
    }

    @Override
    public Set<V> remove(Object key) {
        Set<V> original = map.get(key);
        map.remove(key);
        return original;
    }

    public boolean remove(Object key, Object value) {
        Set<V> values = map.get(key);
        if (values == null) {
            return false;
        }
        else {
            return values.remove(value);
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        boolean first = true;
        for (K key : map.keySet()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            Set<V> values = map.get(key);
            sb.append("[" + key + ": " + values + "]");
        }
        sb.append("}");
        return sb.toString();
    }
}
