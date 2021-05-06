package org.daisy.dotify.common.splitter;

import java.util.Collections;
import java.util.List;

/**
 * Provides split point data source. Data provided by via this interface
 * is expected to be immutable. The interface is designed to work with
 * data sources of unknown size. Access to the interfaces methods should
 * be done with care to limit unnecessary computation in the data source.
 *
 * @param <T> the type of split point units
 * @param <U> the type of data source
 * @author Joel Håkansson
 */
public interface SplitPointDataSource<T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> {

    /**
     * Gets the item at index.
     *
     * @param index the index
     * @return returns the item
     * @throws IndexOutOfBoundsException if the index is beyond the end of the stream
     */
    public T get(int index);

    /**
     * Gets all remaining items. Note that using this method will
     * result in all elements being computed, if this is not what
     * is needed. Consider using {@link #split(int)} instead.
     *
     * @return returns all remaining items
     */
    public List<T> getRemaining();

    /**
     * Creates a new empty data source of the implementing type.
     *
     * @return returns a new empty data source of the implementing type
     */
    public U createEmpty();

    /**
     * <p>Gets the result of splitting at the specified index.
     * An implementation must be able to handle indexes where
     * {@link #hasElementAt(int)} returns false.</p>
     *
     * <p>If atIndex is 0, the head of the result is empty and the
     * original stream is in the tail. Conversely, if atIndex is greater
     * than {@link #hasElementAt(int)} the  head of the result contains
     * {@link #getRemaining()} and the tail is empty.</p>
     *
     * @param atIndex the index where the tail starts
     * @return returns a split result at the specified index
     */
    public default SplitResult<T, U> split(int atIndex) {
        if (atIndex == 0) {
            return new DefaultSplitResult<T, U>(Collections.emptyList(), getDataSource());
        } else if (hasElementAt(atIndex - 1)) {
            return splitInRange(atIndex);
        } else {
            return new DefaultSplitResult<>(getRemaining(), createEmpty());
        }
    }

    /**
     * Gets the result of splitting at the specified index.
     *
     * @param atIndex the index where the tail starts
     * @return returns a split result at the specified index
     * @throws IndexOutOfBoundsException if the index isn't within the bounds of
     *                                   available data.
     */
    public SplitResult<T, U> splitInRange(int atIndex);

    /**
     * Gets the data source as is, typically "<code>return this</code>".
     *
     * @return returns the data source
     */
    public U getDataSource();

    /**
     * Returns true if the manager has an element at the specified index.
     *
     * @param index the index
     * @return returns true if the manager has an element at the specified index, false otherwise
     */
    public boolean hasElementAt(int index);

    /**
     * Gets the size of the manager or the limit if the number of items is greater than the limit.
     *
     * @param limit the limit
     * @return returns the size, or the limit
     */
    public int getSize(int limit);

    /**
     * Returns true if the manager contains no items. Note that
     * this method can returns true, even if {@link #getSupplements()} is
     * non-empty.
     *
     * @return returns true if the manager has no items, false otherwise
     */
    public boolean isEmpty();

    /**
     * Gets the split point data source supplements.
     *
     * @return the supplements
     */
    public Supplements<T> getSupplements();

}
