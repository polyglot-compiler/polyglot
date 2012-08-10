package polyglot.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 
 * A <code>MultiMap</code> is a map that allows the same key to be used to map 
 * to multiple values. 
 * It is therefore an arbitrary binary relation on keys and values.
 */
public class MultiMap<K, V> extends AbstractMap<K, Set<V>> {
    /* The representation is a mapping from each key to the set of values that 
     * that key maps to. */
    private Map<K, Set<V>> map = new HashMap<K, Set<V>>(); // Map <Object, Set <Object> >

    private Set<V> getValueSet(K key) {
        Set<V> values = map.get(key);
        if (values == null) {
            values = new HashSet<V>();
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
     * Makes <code>key</code> map to <code>value</code>, where <code>value</code>
     * has to be a collection of objects.
     */
    @Override
    public Set<V> put(K key, Set<V> value) {
        Set<V> original = map.get(key);
        Set<V> values = new HashSet<V>();
        values.addAll(value);
        map.put(key, values);
        return original;
    }

    /**
     * Adds a single value into the set associated with <code>key</code>.
     */
    public boolean add(K key, V value) {
        return getValueSet(key).add(value);
    }

    /**
     * Adds all the values in <code>values</code> into the set associated with 
     * <code>key</code>.
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
