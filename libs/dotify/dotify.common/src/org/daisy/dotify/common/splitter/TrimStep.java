package org.daisy.dotify.common.splitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TrimStep<T extends SplitPointUnit> implements StepForward<T> {
	private final List<T> ret;
	private final List<T> supplements;
	private final List<T> discarded;
	private final Supplements<T> map;
	private final Set<String> ids;
	
	TrimStep(Supplements<T> map) {
		this.ret = new ArrayList<>();
		this.supplements = new ArrayList<>();
		this.discarded = new ArrayList<>();
		this.map = map;
		this.ids = new HashSet<>();
	}

	@Override
	public void addUnit(T unit) {
		List<String> idList = unit.getSupplementaryIDs();
		if (idList!=null) {
			for (String id : idList) {
				if (ids.add(id)) { //id didn't already exist in the list
					T item = map.get(id);
					if (item!=null) {
						supplements.add(item);
					}
				}
			}
		}
		ret.add(unit);
	}

	@Override
	public boolean overflows(T buffer) {
		return false;
	}

	List<T> getSupplements() {
		return supplements;
	}

	List<T> getResult() {
		return ret;
	}
	
	List<T> getDiscarded() {
		return discarded;
	}

	@Override
	public void addDiscarded(T unit) {
		discarded.add(unit);
	}
	
}