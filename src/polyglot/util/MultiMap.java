package polyglot.util;

import java.util.*;

public class MultiMap extends AbstractMap {
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
	
	public Set getValues(Object key) {
		Set s = (Set)map.get(key);
		if (s == null) {
			return Collections.EMPTY_SET;
		} else {
			return Collections.unmodifiableSet(s);
		}
	}
	
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
	
	public boolean add(Object key, Object value) {
		return getValueSet(key).add(value);
	}
	
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
