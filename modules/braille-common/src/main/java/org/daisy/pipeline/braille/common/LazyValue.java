package org.daisy.pipeline.braille.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.daisy.pipeline.braille.common.util.Function0;

public abstract class LazyValue<V> implements Function0<V>, Iterable<V> {
	
	public Iterator<V> iterator() {
		return new Iterator<V>() {
			boolean hasNext = true;
			public boolean hasNext() {
				return hasNext;
			}
			public V next() {
				if (!hasNext())
					throw new NoSuchElementException();
				hasNext = false;
				return apply();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static <V> LazyValue<V> from(final Function0<V> value) {
		return new LazyValue<V>() {
			public V apply() {
				return value.apply();
			}
		};
	}
	
	public static abstract class ImmutableLazyValue<V> extends LazyValue<V> {
		
		private V value = null;
		protected boolean computed = false;
		
		public final V apply() {
			if (!computed) {
				value = _apply();
				computed = true; }
			return value;
		}
		
		protected abstract V _apply();
		
		public static <V> LazyValue<V> from(final Function0<V> value) {
			return new ImmutableLazyValue<V>() {
				public V _apply() {
					return value.apply();
				}
			};
		}
	}
}
