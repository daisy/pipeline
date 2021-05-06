package org.daisy.dotify.common.collection;

import java.util.Iterator;

/**
 * Provides a method to iterate over several iterables of the same type
 * as if the items were part of the same iterable.
 *
 * @param <T> the type of iterable
 * @author Joel Håkansson
 */
public class CompoundIterable<T> implements Iterable<T> {
    private final Iterable<? extends Iterable<T>> iterables;

    /**
     * Creates a new compound iterable.
     *
     * @param iterables the iterables to use
     */
    public CompoundIterable(Iterable<? extends Iterable<T>> iterables) {
        this.iterables = iterables;
    }

    @Override
    public Iterator<T> iterator() {
        return new CompoundIterator<>(iterables);
    }
}
