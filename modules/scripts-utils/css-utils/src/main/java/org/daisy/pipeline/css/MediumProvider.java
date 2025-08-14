package org.daisy.pipeline.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterables;

import cz.vutbr.web.css.MediaQuery;

public interface MediumProvider {

	/**
	 * Each element <code>m</code> in the returned sequence must satisfy
	 * <code>m.matches(query)</code>.
	 *
	 * @param query The media query, which is assumed to be validated and normalized (see {@link
	 *              MediaQueryParser}).
	 */
	public Iterable<? extends Medium> get(MediaQuery query);

	/**
	 * {@link MediumProvider} that returns generic instances of {@link Medium} (i.e. no sub-classes
	 * of {@link Medium}).
	 */
	public static final MediumProvider GENERIC_MEDIUM_PROVIDER = new MediumProvider() {
			@Override
			public Iterable<Medium> get(MediaQuery query) {
				return new LazyValue<Medium>(() -> {
						try {
							return Medium.fromMediaQuery(query);
						} catch (IllegalArgumentException e) {
							throw new NoSuchElementException(e.getMessage());
						}
				});
			}
		};

	/**
	 * @param providers Collection of {@link MediumProvider} to dispatch query to. Future calls to
	 *                  {@link #get} on the returned {@link MediumProvider} take into account any
	 *                  modifications to the collection.
	 */
	public static MediumProvider dispatch(Collection<MediumProvider> providers) {
		return dispatch(providers, false);
	}

	public static MediumProvider dispatch(Collection<MediumProvider> providers, boolean includeGeneric) {
		return new MediumProvider() {
			@Override
			public Iterable<Medium> get(MediaQuery query) {
				Collection<MediumProvider> pp = includeGeneric
					? new ArrayList<>(providers)
					: providers;
				if (includeGeneric)
					pp.add(GENERIC_MEDIUM_PROVIDER);
				return Iterables.concat(
					Iterables.transform(
						pp,
						p -> p.get(query)
					)
				);
			}
		};
	}
}

class LazyValue<V> implements Iterable<V> {

	private final Supplier<V> value;

	LazyValue(Supplier<V> value) {
		this.value = value;
	}

	private boolean computed = false;
	private V computedValue = null;
	private NoSuchElementException exception = null;

	private V get() throws NoSuchElementException {
		if (!computed) {
			if (exception != null)
				throw exception;
			try {
				computedValue = value.get();
				computed = true;
			} catch (RuntimeException e) {
				exception = new NoSuchElementException(e.getMessage());
				throw exception;
			}
		}
		return computedValue;
	}

	public Iterator<V> iterator() {
		return new Iterator<V>() {
			boolean hasNext = true;
			public boolean hasNext() {
				if (!hasNext)
					return false;
				try {
					get();
					return true;
				} catch (NoSuchElementException e) {
					return false;
				}
			}
			public V next() {
				if (!hasNext())
					throw new NoSuchElementException();
				hasNext = false;
				return get();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

