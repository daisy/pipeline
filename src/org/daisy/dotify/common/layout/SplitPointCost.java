package org.daisy.dotify.common.layout;

import java.util.List;

/**
 * 
 * @author Joel Håkansson
 * @param <T> the type of split point unit
 *
 */
@FunctionalInterface
public interface SplitPointCost<T extends SplitPointUnit> {

	/**
	 * Returns the cost of breaking after the unit with the specified index
	 * @param units the units
	 * @param index the index of the breakpoint unit
	 * @return returns the cost
	 */
	public double getCost(List<T> units, int index);
	
}
