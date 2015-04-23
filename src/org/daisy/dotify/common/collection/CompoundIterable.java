package org.daisy.dotify.common.collection;

import java.util.Iterator;

/**
 * Provides a method to iterate over several iterables of the same type
 * as if the items were part of the same iterable.
 * @author Joel Håkansson
 *
 * @param <T> the type of iterable
 */
public class CompoundIterable<T> implements Iterable<T> {
	private final Iterable<? extends Iterable<T>> iterables;
	
	public CompoundIterable(Iterable<? extends Iterable<T>> iterables) {
		this.iterables = iterables;
	}

	public Iterator<T> iterator() {
		return new CompoundIterator<T>(iterables);
	}
}