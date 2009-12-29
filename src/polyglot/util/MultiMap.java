package polyglot.util;

import java.util.*;

/** 
 * A <code>MultiMap</code> is a map that allows the same key to be used to map 
 * to multiple values. 
 * It is therefore an arbitrary binary relation on keys and values.
 */
public class MultiMap extends AbstractMap {
	/* The representation is a mapping from each key to the set of values that 
	 * that key maps to. */
	private Map map = new HashMap(); // Map <Object, Set <Object> >
	
	private Set getValueSet(Object key) {
		Set values = (Set)map.get(key);
		if (values == null) {
			values = new HashSet();
			map.put(key, values);
		}
		return values;
	}
	
	public Set entrySet() {
		return map.entrySet();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			Set values = (Set)entry.getValue();
			if (values.contains(values)) return true;
		}
		return false;
	}
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	/** The set of values associated with a key. Not modifiable. */
	public Set getValues(Object key) {
		Set s = (Set)map.get(key);
		if (s == null) {
			return Collections.EMPTY_SET;
		} else {
			return Collections.unmodifiableSet(s);
		}
	}
	
	/** 
	 * Makes <code>key</code> map to <code>value</code>, where <code>value</code>
	 * has to be a collection of objects.
	 */
	public Object put(Object key, Object value) {
		if (value instanceof Collection) {
			Object original = map.get(key);
			Set values = new HashSet();
			values.addAll((Collection)value);
			map.put(key, values);
			return original;
		}
		throw new IllegalArgumentException(value.getClass().toString());
	}

	/**
	 * Adds a single value into the set associated with <code>key</code>.
	 */
	public boolean add(Object key, Object value) {
		return getValueSet(key).add(value);
	}
	
	/**
	 * Adds all the values in <code>values</code> into the set associated with 
	 * <code>key</code>.
	 */
	public boolean addAll(Object key, Collection values) {
		return getValueSet(key).addAll(values);
	}
	
	public Object remove(Object key) {
		Object original = map.get(key);
		map.remove(key);
		return original;
	}
	
	public boolean remove(Object key, Object value) {
		Set values = (Set)map.get(key);
		if (values == null) {
			return false;
		} else {
			return values.remove(value);
		}
	}
	
	public void clear() {
		map.clear();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			Object key = it.next();
			Set values = (Set)map.get(key);
			sb.append("[" + key + ": " + values + "]");
		}
		sb.append("}");
		return sb.toString();
	}
}
