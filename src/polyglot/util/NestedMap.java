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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A NestedMap is a map which, when it cannot find an element in itself,
 * defers to another map.  Modifications, however, are not passed on to
 * the supermap.
 *
 * A NestedMap and its backing collections and iterators support all
 * operations except 'remove' and 'clear', since operations to a
 * NestedMap must not affect the backing map.  Instead, use the 'release'
 * method.
 *
 * It is used to implement nested namespaces, such as those which store
 * local-variable bindings.
 **/
public class NestedMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    /**
     * Creates a new nested map, which defers to <containing>.  If containing
     * is null, it defaults to a NilMap.
     **/
    public NestedMap(Map<K, V> containing) {
        this.superMap =
                containing == null ? NilMap.<K, V> emptyMap() : containing;
        this.myMap = new HashMap<K, V>();
        setView = new EntrySet();
        nShadowed = 0;
    }

    /////
    // For NestedMap.
    /////
    /**
     * Returns the map to which this map defers, or null for none.
     **/
    public Map<K, V> getContainingMap() {
        return superMap instanceof NilMap ? null : superMap;
    }

    /**
     * Removes any binding in this for <key>, returning to the binding (if any)
     * from the supermap.
     **/
    public void release(Object key) {
        myMap.remove(key);
    }

    /**
     * Returns the map containing the elements for this level of nesting.
     **/
    public Map<K, V> getInnerMap() {
        return myMap;
    }

    /////
    // Methods required for AbstractMap.
    /////

    @Override
    public Set<Entry<K, V>> entrySet() {
        return setView;
    }

    @Override
    public int size() {
        return superMap.size() + myMap.size() - nShadowed;
    }

    @Override
    public boolean containsKey(Object key) {
        return myMap.containsKey(key) || superMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        if (myMap.containsKey(key))
            return myMap.get(key);
        else return superMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (myMap.containsKey(key)) {
            return myMap.put(key, value);
        }
        else {
            V oldV = superMap.get(key);
            myMap.put(key, value);
            nShadowed++;
            return oldV;
        }
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Remove from NestedMap");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Clear in NestedMap");
    }

    public final class KeySet extends AbstractSet<K> {
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<K> iterator() {
            return new ConcatenatedIterator<K>(myMap.keySet().iterator(),
                                               new FilteringIterator<K>(superMap.keySet(),
                                                                        keyNotInMyMap));
        }

        @Override
        public int size() {
            return NestedMap.this.size();
        }

        // No add; it's not meaningful.
        @Override
        public boolean contains(Object o) {
            return NestedMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Remove from NestedMap.keySet");
        }
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ConcatenatedIterator<Entry<K, V>>(myMap.entrySet()
                                                              .iterator(),
                                                         new FilteringIterator<Entry<K, V>>(superMap.entrySet(),
                                                                                            entryKeyNotInMyMap));
        }

        @Override
        public int size() {
            return NestedMap.this.size();
        }

        // No add; it's not meaningful.
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> ent = (Entry<K, V>) o;
            K entKey = ent.getKey();
            V entVal = ent.getValue();
            if (entVal != null) {
                V val = NestedMap.this.get(entKey);
                return (val != null) && val.equals(entVal);
            }
            else {
                return NestedMap.this.containsKey(entKey)
                        && (NestedMap.this.get(entKey) == null);
            }
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Remove from NestedMap.entrySet");
        }
    }

    private HashMap<K, V> myMap;
    private int nShadowed;
    private Set<Entry<K, V>> setView; // the set view of this.
    private Map<K, V> superMap;
    private Predicate<Entry<K, V>> entryKeyNotInMyMap =
            new Predicate<Entry<K, V>>() {
                @Override
                public boolean isTrue(Entry<K, V> ent) {
                    return !myMap.containsKey(ent.getKey());
                }
            };
    private Predicate<K> keyNotInMyMap = new Predicate<K>() {
        @Override
        public boolean isTrue(K o) {
            return !myMap.containsKey(o);
        }
    };

}
