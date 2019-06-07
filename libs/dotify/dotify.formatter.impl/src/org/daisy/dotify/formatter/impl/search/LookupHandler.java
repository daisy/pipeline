package org.daisy.dotify.formatter.impl.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>Provides a way for an iterative process to get and put values out 
 * of temporal order. If the value of a key is changed after it has been
 * requested with one of the <code>get</code> methods, the {@link LookupHandler} is 
 * marked as "dirty".
 * When a new iteration is started, the dirty status is reset by calling
 * {@link #setDirty(boolean)}.
 * If all requested values between calls to {@link #setDirty(boolean)} are
 * unchanged, {@link #isDirty()} will return false.</p>
 * 
 * <p>A typical use case involves self-referential output. Here's an
 * example that uses {@link LookupHandler} to calculate the length of
 * a string that indicates its own length:</p>
 * 
 * <pre>
String charCountKey = "character-count";
LookupHandler&lt;String, Integer&gt; variables = new LookupHandler&lt;&gt;();
String message;
do {
	variables.setDirty(false);
	message = String.format("This string is %s characters long.",
			// 0 is returned when no value has been put
			variables.get(charCountKey, 0)  
		);
	variables.put(charCountKey, message.length());
} while (variables.isDirty());
 * </pre>
 * 
 * <p>A value can be changed in one of two ways, either via 
 * {@link #put(Object, Object)} (as seen above) or via {@link #keep(Object, Object)}
 * followed by {@link #commit()}. The purpose of the latter is
 * to support <em>multiple</em> puts of the same key with (possibly) different values
 * within a single iteration. Once the iteration is completed, a call to {@link #commit()}
 * stores the last kept value. Interleaving calls to {@link #get(Object)}
 * will not be affected by the kept, but uncommitted, values.</p>
 * 
 * <p>Note that, if a key is requested that is not in the map, the value
 * of this key is also considered to have changed. It is therefore
 * important to make sure that requested information is added to 
 * the map at some point.</p>
 * 
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a hash map concurrently, and at least one of
 * the threads modifies the map structurally, it <i>must</i> be
 * synchronized externally.</p>
 * 
 * @author Joel Håkansson
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
class LookupHandler<K, V> {
	private final Map<K, V> keyValueMap;
	private final Map<K, V> uncommitted;
	private final Set<K> requestedKeys;
	private boolean dirty;
	
	/**
	 * Creates a new empty lookup handler.
	 */
	LookupHandler() {
		this.keyValueMap = new HashMap<>();
		this.uncommitted = new HashMap<>();
		this.requestedKeys = new HashSet<>();
		this.dirty = false;
	}

	/**
	 * Returns the value to which the specified key is mapped, or null if this map
	 * contains no mapping for the key. 
	 * 
	 * @param key the key
	 * @return the value to which the specified key is mapped, or null if this map
	 * contains no mapping for the key. 
	 */
	V get(K key) {
		return get(key, null);
	}

	/**
	 * Returns the value to which the specified key is mapped, or the default value 
	 * if this map contains no mapping for the key.
	 * @param key the key
	 * @param def the default value
	 * @return the value to which the specified key is mapped, or the default value 
	 * if this map contains no mapping for the key
	 */
	V get(K key, V def) {
		return get(key, def, false);
	}
	
	/**
	 * Returns the value to which the specified key is mapped, or the default value 
	 * if this map contains no mapping for the key.
	 * @param key the key
	 * @param def the default value
	 * @param traceless when true, the integrity mechanism is bypassed. In other
	 * 		words, a value can be retrieved without affecting the value 
	 * 		of {@link #isDirty()}. Use with care.
	 * @return the value to which the specified key is mapped, or the default value 
	 * if this map contains no mapping for the key
	 */
	V get(K key, V def, boolean traceless) {
		if (!traceless) {
			requestedKeys.add(key);
		}
		V ret = keyValueMap.get(key);
		if (ret==null) {
			if (!traceless) {
				dirty = true;
			}
			//ret is null here, so if def is also null, either variable can be returned
			return def;
		} else {
			return ret;
		}
	}

	/**
	 * Keeps a value for later commit. Unlike {@link #put(Object, Object)}, multiple calls to keep for the same key will not 
	 * affect the status of the LookupHandler. Upon calling commit, the final value for each key are put
	 * and, if changed, affects the status of the LookupHandler. Note that kept variables cannot 
	 * be read unless committed.
	 * 
	 * @param key the value to keep
	 * @param value the value
	 */
	void keep(K key, V value) {
		Objects.requireNonNull(value);
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
	
	/**
	 * Associates the specified value with the specified key in this 
	 * map. If the map previously contained a mapping for the key,
	 * {@link #isDirty()} will return true after this call.
	 * @param key the key
	 * @param value the value
	 * @throws IllegalStateException if {@link #keep(Object, Object)} has been used on 
	 * the specified key, but not {@link #commit()}.
	 */
	void put(K key, V value) {
		Objects.requireNonNull(value);
		if (uncommitted.containsKey(key)) {
			throw new IllegalStateException(key + " has uncommitted values. Commit before putting.");
		}
		V prv = keyValueMap.put(key, value);
		if (requestedKeys.contains(key) && prv!=null && !prv.equals(value)) {
			dirty = true;
		}
	}

	/**
	 * Returns true if the value for any requested key was not present,
	 * or if the value for any requested key changed after it was last requested.
	 * @return true if the value for any key has changed, false otherwise
	 */
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
