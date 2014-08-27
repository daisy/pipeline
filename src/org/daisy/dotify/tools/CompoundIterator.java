package org.daisy.dotify.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an iterator for a collection of iterables
 * @author Joel Håkansson
 *
 * @param <T> the type of iterator
 */
public class CompoundIterator<T> implements Iterator<T> {
	ArrayList<Iterator<T>> iterators;
	
	public CompoundIterator(Iterable<? extends Iterable<T>> iterables) {
		iterators = new ArrayList<Iterator<T>>();
		for (Iterable<T> e : iterables) {
			iterators.add(e.iterator());
		}
	}

	public boolean hasNext() {
		for (Iterator<T> e : iterators) {
			if (e.hasNext()) {
				return true;
			}
		}
		return false;
	}

	public T next() {
		for (Iterator<T> e : iterators) {
			if (e.hasNext()) {
				return e.next();
			}
		}
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
		
	}
}