package org.daisy.dotify.common.split;

import java.util.Arrays;
import java.util.List;

/**
 * Provides split point data
 * @author Joel Håkansson
 *
 * @param <T> the type of split point units
 */
public final class SplitPointData<T extends SplitPointUnit> {
	private final List<T> units;
	private final Supplements<T> supplements;

	/**
	 * Creates a new instance with the specified units
	 * @param units the units
	 */
	@SafeVarargs
	public SplitPointData(T ... units) {
		this(Arrays.asList(units));
	}
	
	/**
	 * Creates a new instance with the specified units
	 * @param units the units
	 */
	public SplitPointData(List<T> units) {
		this(units, null);
	}
	
	/**
	 * Creates a new instance with the specified units and supplements
	 * @param units the units
	 * @param supplements the supplements
	 */
	public SplitPointData(List<T> units, Supplements<T> supplements) {
		this.units = units;
		if (supplements==null) {
			this.supplements = new Supplements<T>(){
				@Override
				public T get(String id) {
					return null;
				}};
		} else {
			this.supplements = supplements;
		}
	}

	/**
	 * Gets the split point data units
	 * @return returns the units
	 */
	public List<T> getUnits() {
		return units;
	}

	/**
	 * Gets the split point data supplements
	 * @return the supplements
	 */
	public Supplements<T> getSupplements() {
		return supplements;
	}

}
