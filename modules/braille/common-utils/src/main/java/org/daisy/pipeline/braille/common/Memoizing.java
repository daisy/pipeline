package org.daisy.pipeline.braille.common;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;

public interface Memoizing<K,V> extends Function<K,V> {
	
	public void invalidateCache();
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		public static abstract class AbstractMemoizing<K,V> implements Memoizing<K,V> {
			
			private final Map<K,V> cache = new HashMap<K,V>();
			
			/**
			 * @param key must not be mutated.
			 */
			protected abstract V _apply(K key);
	
			public final V apply(K key) {
				if (cache.containsKey(key))
					return cache.get(key);
				V value = _apply(key);
				if (value != null) {
					cache.put(key, value);
					return value; }
				return null;
			}
			
			public void invalidateCache() {
				cache.clear();
			}
		}
		
		public static abstract class CloningMemoizing<K,V extends Cloneable> implements Memoizing<K,V> {
			
			private final Map<K,V> cache = new HashMap<K,V>();
			
			/**
			 * @param key must not be mutated.
			 */
			protected abstract V _apply(K key);
	
			public final V apply(K key) {
				V value;
				if (cache.containsKey(key))
					value = cache.get(key);
				else {
					value = _apply(key);
					if (value != null)
						cache.put(key, value); }
				if (value == null)
					return null;
				else {
					try {
						return (V)value.getClass().getMethod("clone").invoke(value); }
					catch (IllegalAccessException
					       | IllegalArgumentException
					       | InvocationTargetException
					       | NoSuchMethodException
					       | SecurityException e) {
						throw new RuntimeException("Could not invoke clone() method", e);
					}
				}
			}
			
			public void invalidateCache() {
				cache.clear();
			}
		}
	}
}
