package org.daisy.pipeline.braille.common;

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
			
			private final Map<K,V> cache;
			
			protected AbstractMemoizing() {
				cache = new HashMap<K,V>();
			}

			protected AbstractMemoizing(AbstractMemoizing<K,V> shareCacheWith) {
				this.cache = shareCacheWith.cache;
			}
			
			/**
			 * @param key must not be mutated.
			 */
			protected abstract V _apply(K key);
			
			protected boolean skip(K key) {
				return false;
			}

			public final V apply(K key) {
				if (cache.containsKey(key))
					return cache.get(key);
				V value = _apply(key);
				if (value != null && !skip(key))
					cache.put(key, value);
				return value;
			}
			
			public void invalidateCache() {
				cache.clear();
			}
		}
	}
}
