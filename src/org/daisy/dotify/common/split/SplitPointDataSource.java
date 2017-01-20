package org.daisy.dotify.common.split;

import java.util.List;

/**
 * Provides split point data source. Data provided by via this interface
 * is expected to be immutable. The interface is designed to work with
 * data sources of unknown size. Access to the interfaces methods should
 * be done with care to limit unnecessary computation in the data source.
 * 
 * @author Joel Håkansson
 *
 * @param <T> the type of split point units
 */
public interface SplitPointDataSource<T extends SplitPointUnit> {

	/**
	 * Gets the item at index.
	 * @param index the index
	 * @return returns the unit
	 * @throws IndexOutOfBoundsException if the unit cannot be returned
	 */
	public T get(int index);
	
	/**
	 * Gets the units before index.
	 * @param toIndex the index, exclusive
	 * @return returns a head list
	 */
	public List<T> head(int toIndex);
	
	/**
	 * Gets all remaining items. Note that using this method will
	 * result in all elements being computed, if this is not what
	 * is needed. Consider using {@link #head(int)} instead.
	 * @return returns all remaining items
	 */
	public List<T> getRemaining();

	/**
	 * Gets a tail list.
	 * @param fromIndex the starting index, inclusive
	 * @return returns a new split point data source starting from fromIndex
	 */
	public SplitPointDataSource<T> tail(int fromIndex);
	
	/**
	 * Returns true if the manager has an element at the specified index
	 * @param index the index
	 * @return returns true if the manager has an element at the specified index, false otherwise
	 */
	public boolean hasElementAt(int index);
	
	/**
	 * Gets the size of the manager or the limit if the number of units is greater than the limit
	 * @param limit the limit
	 * @return returns the size, or the limit
	 */
	public int getSize(int limit);
	
	/**
	 * Returns true if the manager contains no units. Note that
	 * this method can returns true, even if {@link #getSupplements()} is
	 * non-empty.
	 * @return returns true if the manager has no units, false otherwise
	 */
	public boolean isEmpty();
	
	/**
	 * Gets the split point data source supplements.
	 * @return the supplements
	 */
	public Supplements<T> getSupplements();

}
