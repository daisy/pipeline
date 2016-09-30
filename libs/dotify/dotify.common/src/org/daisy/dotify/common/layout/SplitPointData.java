package org.daisy.dotify.common.layout;

import java.util.Arrays;
import java.util.List;

public final class SplitPointData<T extends SplitPointUnit> {
	private final List<T> units;
	private final Supplements<T> supplements;

	@SafeVarargs
	public SplitPointData(T ... units) {
		this(Arrays.asList(units));
	}
	
	public SplitPointData(List<T> units) {
		this(units, null);
	}
	
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

	public List<T> getUnits() {
		return units;
	}

	public Supplements<T> getSupplements() {
		return supplements;
	}

}
