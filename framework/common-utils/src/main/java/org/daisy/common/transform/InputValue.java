package org.daisy.common.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

/**
 * A supplier of a XDM value (item or sequence of items). The items may be nodes (see {@link
 * XMLInputValue}), atomic values, or maps, and by extension arbitrary objects ("external" items).
 */
public class InputValue<V> {

	private InputValue<V> backingValue;
	private V object = null;
	private boolean objectSupplied = false;

	/**
	 * @param object A Java object that represents a single atomic value, sequence of atomic values,
	 *     map item, or other object (but not a node sequence).
	 *
	 *     <p>The object may be a plain old Java object, for example:</p>
	 *     <ul>
	 *       <li>{@link String}: a single <code>xs:string</code> atomic value</li>
	 *       <li>{@link Integer}: a single <code>xs:integer</code> atomic value</li>
	 *       <li>{@link QName}: a single <code>xs:QName</code> atomic value</li>
	 *       <li>{@link Iterator}{@code <}{@link Object}{@code >}: a sequence of atomic values</li>
	 *       <li>{@link Map}{@code <}{@link Object}{@code ,}{@link InputValue}{@code >}: a XDM map
	 *         (where the keys are single atomic values and the values are arbitrary values)</li>
	 *     </ul>
	 *     <p>It may also be an object representing a non-XDM item.</p>
	 */
	public InputValue(V value) {
		object = value;
	}

	protected InputValue() {
	}

	protected InputValue(InputValue<V> value) {
		backingValue = value;
	}

	public V asObject() throws UnsupportedOperationException, NoSuchElementException {
		if (backingValue != null)
			return backingValue.asObject();
		else if (object == null && !objectSupplied)
			throw new UnsupportedOperationException();
		else if (valueSupplied())
			throw new NoSuchElementException();
		else {
			objectSupplied = true;
			V ret = object;
			object = null;
			return ret;
		}
	}

	@SuppressWarnings("unchecked") // safe cast
	public final <T> T asObject(Class<T> type) throws UnsupportedOperationException, NoSuchElementException {
		V o = asObject();
		if (!type.isInstance(o))
			throw new UnsupportedOperationException();
		return (T)o;
	}

	@Override
	public String toString() {
		if (backingValue != null)
			return backingValue.toString();
		else if (object != null)
			return object.toString();
		else
			return super.toString();
	}

	/**
	 * Make multiples of this input.
	 *
	 * @param limit The maximum number of multiples that will be made. <code>-1</code> means unlimited.
	 */
	public Mult<? extends InputValue<V>> mult(int limit) {
		return new Mult<InputValue<V>>() {
			Iterable<V> cache = cache(
				iteratorOf(InputValue.this::asObject),
				limit);
			int supplied = 0;
			public InputValue<V> get() throws NoSuchElementException {
				if (supplied >= limit) {
					cache = null;
					throw new NoSuchElementException();
				}
				supplied++;
				return new InputValue<V>() {
					Iterable<V> val = cache;
					@Override
					public V asObject() throws UnsupportedOperationException, NoSuchElementException {
						return val.iterator().next();
					}
				};
			}
		};
	}

	protected boolean valueSupplied() {
		return objectSupplied;
	}

	/**
	 * Cache a {@link Iterator}. Items are evicted after they have been supplied <code>limit</code>
	 * times. Exceptions thrown by the input iterator are replayed.
	 */
	// FIXME: limit argument is not implemented yet
	protected static <T> Iterable<T> cache(Iterator<T> iterator, int limit) {
		if (iterator == null)
			throw new IllegalArgumentException();
		return new Iterable<T>() {
			private final List<T> cache = new ArrayList<T>();
			private RuntimeException exception = null;
			public final Iterator<T> iterator() {
				return new Iterator<T>() {
					private int index = 0;
					public boolean hasNext() {
						synchronized (cache) {
							if (index < cache.size())
								return true;
							if (exception != null)
								throw exception;
							try {
								return iterator.hasNext();
							} catch (RuntimeException e) {
								exception = e;
								throw e;
							}
						}
					}
					public T next() throws NoSuchElementException {
						synchronized (cache) {
							if (index < cache.size())
								return cache.get(index++);
							if (exception != null)
								throw exception;
							try {
								T next = iterator.next();
								cache.add(next);
								index++;
								return next;
							} catch (RuntimeException e) {
								exception = e;
								throw e;
							}
						}
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	protected static <T> Iterator<T> iteratorOf(Supplier<T> supplier) {
		return new Iterator<T>() {
			T next = null;
			boolean nextComputed = false;
			public boolean hasNext() {
				if (nextComputed)
					return true;
				try {
					next = supplier.get();
					nextComputed = true;
					return true;
				} catch (NoSuchElementException e) {
					return false;
				}
			}
			public T next() {
				if (nextComputed) {
					nextComputed = false;
					return next;
				}
				return supplier.get();
			}
		};
	}
}
