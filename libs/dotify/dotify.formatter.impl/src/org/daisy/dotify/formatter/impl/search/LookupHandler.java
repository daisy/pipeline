package org.daisy.dotify.formatter.impl.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class LookupHandler<K, V> {
	private final Map<K, V> keyValueMap;
	private final Map<K, V> uncommitted;
	private final Set<K> requestedKeys;
	private boolean dirty;
	
	LookupHandler() {
		this.keyValueMap = new HashMap<>();
		this.uncommitted = new HashMap<>();
		this.requestedKeys = new HashSet<>();
		this.dirty = false;
	}

	V get(K key) {
		return get(key, null);
	}

	V get(K key, V def) {
		requestedKeys.add(key);
		V ret = keyValueMap.get(key);
		if (ret==null) {
			dirty = true;
			//ret is null here, so if def is also null, either variable can be returned
			return def;
		} else {
			return ret;
		}
	}

	/**
	 * Keeps a value for later commit. Unlike put, multiple calls to keep for the same key will not 
	 * affect the status of the LookupHandler. Upon calling commit, the final value for each key are put
	 * and, if changed, affects the status of the LookupHandler. Note that kept variables cannot 
	 * be read unless committed.
	 * 
	 * @param key the value to keep
	 * @param value the value
	 */
	void keep(K key, V value) {
		uncommitted.put(key, value);
	}
	
	/**
	 * Commits values stored with keep.
	 */
	void commit() {
		while (!uncommitted.isEmpty()) {
			K key = uncommitted.keySet().iterator().next();
			V value = uncommitted.remove(key);
			put(key, value);
		}
	}
	
	void put(K key, V value) {
		if (uncommitted.containsKey(key)) {
			throw new IllegalStateException(key + " has uncommitted values. Commit before putting.");
		}
		V prv = keyValueMap.put(key, value);
		if (requestedKeys.contains(key) && prv!=null && !prv.equals(value)) {
			dirty = true;
		}
	}

	boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Sets the dirty status
	 * @param value the value
	 * @throws IllegalStateException if there are uncommitted values.
	 */
	void setDirty(boolean value) {
		if (!uncommitted.isEmpty()) {
			throw new IllegalStateException("Uncommitted values.");
		}
		if (!value) {
			requestedKeys.clear();
		}
		dirty = value;
	}
}
