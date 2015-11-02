package org.daisy.dotify.common.layout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SplitPointData<T extends SplitPointUnit> {
	private final List<T> units;
	private final Map<String, T> supplements;

	public SplitPointData(T ... units) {
		this(Arrays.asList(units));
	}
	
	public SplitPointData(List<T> units) {
		this(units, null);
	}
	
	public SplitPointData(List<T> units, Map<String, T> supplements) {
		this.units = units;
		if (supplements==null) {
			this.supplements = Collections.emptyMap();
		} else {
			this.supplements = supplements;
		}
	}

	public List<T> getUnits() {
		return units;
	}

	public Map<String, T> getSupplements() {
		return supplements;
	}

}
