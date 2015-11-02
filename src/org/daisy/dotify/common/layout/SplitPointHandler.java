package org.daisy.dotify.common.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Breaks units into results. All allowed break points are supplied with the input.
 * 
 * @author Joel Håkansson
 *
 */
public class SplitPointHandler<T extends SplitPointUnit> {
	private final List<T> EMPTY_LIST = Collections.emptyList();
	private boolean trimLeading;
	private boolean trimTrailing;
	
	public SplitPointHandler() {
		this.trimLeading = true;
		this.trimTrailing = true;
	}
	
	public SplitPoint<T> split(float breakPoint, boolean force, T ... units) {
		return split(breakPoint, force, new SplitPointData<T>(units));
	}

	/**
	 * Gets the next result from this BreakPointHandler
	 * @param breakPoint the desired breakpoint for this result
	 * @return returns the next BreakPoint
	 */
	public SplitPoint<T> split(float breakPoint, boolean force, List<T> units) {
		return split(breakPoint, force, new SplitPointData<T>(units));
	}

	public SplitPoint<T> split(float breakPoint, boolean force, SplitPointData<T> data) {
		if (data.getUnits().size()==0) {
			// pretty simple...
			return new SplitPoint<T>(data.getUnits(), EMPTY_LIST, EMPTY_LIST, false);
		} else if (breakPoint<=0) {
			return finalizeBreakpointTrimTail(EMPTY_LIST, data.getUnits(), data.getSupplements(), false);
		} else if (totalSize(data.getUnits(), data.getSupplements())<=breakPoint) {
			return finalizeBreakpointTrimTail(data.getUnits(), EMPTY_LIST, data.getSupplements(), false);
		} else {
			return findBreakpoint(data.getUnits(), breakPoint, data.getSupplements(), force);
		}
	}

	public boolean isTrimLeading() {
		return trimLeading;
	}

	public void setTrimLeading(boolean trimLeading) {
		this.trimLeading = trimLeading;
	}

	public boolean isTrimTrailing() {
		return trimTrailing;
	}

	public void setTrimTrailing(boolean trimTrailing) {
		this.trimTrailing = trimTrailing;
	}
	
	private SplitPoint<T> findBreakpoint(List<T> units, float breakPoint, Map<String, T> map, boolean force) {
		int strPos = forwardSkippable(units, findCollapse(units, new SizeStep(breakPoint, map)));
		// check next unit to see if it can be removed.
		if (strPos==units.size()-1) { // last unit?
			List<T> head = units.subList(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(units, head, tailStart, map, false);
		} else if (units.get(strPos).isBreakable()) {
			List<T> head = units.subList(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(units, head, tailStart, map, false);
		} else {
			return newBreakpointFromPosition(units, strPos, map, force);
		}
	}
	
	private SplitPoint<T> newBreakpointFromPosition(List<T> units, int strPos, Map<String, T> map, boolean force) {
		// back up
		int i=findBreakpointBefore(units, strPos);
		List<T> head;
		boolean hard = false;
		int tailStart;
		if (i<0) { // no breakpoint found, break hard 
			if (force) {
				hard = true;
				head = units.subList(0, strPos+1);
				tailStart = strPos+1;
			} else {
				head = EMPTY_LIST;
				tailStart = 0;
			}
		} else {
			head = units.subList(0, i+1);
			tailStart = i+1;
		}
		return finalizeBreakpointFull(units, head, tailStart, map, hard);
	}
	
	private SplitPoint<T> finalizeBreakpointFull(List<T> units, List<T> head, int tailStart, Map<String, T> map, boolean hard) {
		List<T> tail = getTail(units, tailStart);

		if (trimTrailing) {
			head = trimTrailing(head);
		}
		
		return finalizeBreakpointTrimTail(head, tail, map, hard);
	}

	private SplitPoint<T> finalizeBreakpointTrimTail(List<T> head, List<T> tail, Map<String, T> map, boolean hard) {
		if (trimLeading) {
			tail = trimLeading(tail);
		}
		TrimStep trimmed = new TrimStep(map);
		findCollapse(head, trimmed);
		head = trimmed.getResult();
		return new SplitPoint<T>(head, trimmed.getSupplements(), tail, hard);
	}
	
	static <T extends SplitPointUnit> List<T> trimLeading(List<T> in) {
		List<T> ret = new ArrayList<T>();
		for (int i = 0; i<in.size(); i++) {
			if (!in.get(i).isSkippable()) {
				ret = in.subList(i, in.size());
				break;
			}
		}
		return ret;
	}

	static <T extends SplitPointUnit> T maxSize(T u1, T u2) {
		return (u1.getUnitSize()>=u2.getUnitSize()?u1:u2); 
	}
	
	static <T extends SplitPointUnit> List<T> trimTrailing(List<T> in) {
		List<T> ret = new ArrayList<T>();
		for (int i = in.size()-1; i>=0; i--) {
			if (!in.get(i).isSkippable()) {
				ret = in.subList(0, i+1);
				break;
			}
		}
		return ret;
	}
	
	static <T extends SplitPointUnit> List<T> getTail(List<T> units, int tailStart) {
		if (units.size()>tailStart) {
			List<T> tail = units.subList(tailStart, units.size());
			return tail;
		} else {
			return Collections.emptyList();
		}
	}
	
	static <T extends SplitPointUnit> int findCollapse(List<T> charsStr, StepForward<T> impl) {
		int units = -1;
		T maxCollapsable = null;
		for (T c : charsStr) {
			units++;
			if (c.isCollapsible()) {
				if (maxCollapsable!=null) {
					if (maxCollapsable.collapsesWith(c)) {
						maxCollapsable = maxSize(maxCollapsable, c);
					} else {
						impl.addUnit(maxCollapsable);
						maxCollapsable = c;
					}
				} else {
					maxCollapsable = c;
				}
			} else {
				if (maxCollapsable!=null) {
					impl.addUnit(maxCollapsable);
					maxCollapsable = null;
				}
				impl.addUnit(c);
			}
			if (impl.overflows(maxCollapsable)) { //time to exit
				units--;
				return units;
			}
		}
		if (maxCollapsable!=null) {
			impl.addUnit(maxCollapsable);
			maxCollapsable = null;
		}
		return units;
	}
	
	class SizeStep implements StepForward<T> {
		private float size = 0;
		private final Map<String, T> map;
		private final Set<String> ids;
		private final float breakPoint;
		
		SizeStep(float breakPoint, Map<String, T> map) {
			this.breakPoint = breakPoint;
			this.map = map;
			this.ids = new HashSet<String>();
		}

		@Override
		public void addUnit(T unit) {
			List<String> idList = unit.getSupplementaryIDs();
			if (idList!=null) {
				for (String id : idList) {
					if (ids.add(id)) { //id didn't already exist in the list
						size+=map.get(id).getUnitSize();
					}
				}
			}
			size+=unit.getUnitSize();
		}

		@Override
		public boolean overflows(T buffer) {
			return size+(buffer!=null?unitSize(buffer):0)>breakPoint;
		}
		
		private float unitSize(T b) {
			float ret = 0;
			List<String> idList = b.getSupplementaryIDs();
			if (idList!=null) {
				for (String id : idList) {
					if (!ids.contains(id)) { //id didn't already exist in the list
						ret+=map.get(id).getUnitSize();
					}
				}
			}
			ret += b.getUnitSize();
			return ret;
		}

	}
	
	class TrimStep implements StepForward<T> {
		private final List<T> ret;
		private final List<T> supplements;
		private final Map<String, T> map;
		private final Set<String> ids;
		
		TrimStep(Map<String, T> map) {
			this.ret = new ArrayList<T>();
			this.supplements = new ArrayList<T>();
			this.map = map;
			this.ids = new HashSet<String>();
		}

		@Override
		public void addUnit(T unit) {
			List<String> idList = unit.getSupplementaryIDs();
			if (idList!=null) {
				for (String id : idList) {
					if (ids.add(id)) { //id didn't already exist in the list
						supplements.add(map.get(id));
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
		
	}
	
	static int forwardSkippable(List<? extends SplitPointUnit> charsStr, final int pos) {
		SplitPointUnit c;
		int ret = pos;
		if (ret<charsStr.size() && !(c=charsStr.get(ret)).isBreakable()) {
			ret++;
			while (ret<charsStr.size() && (c=charsStr.get(ret)).isSkippable()) {
				if (c.isBreakable()) {
					return ret;
				} else {
					ret++;
				}
			}
			if (ret==charsStr.size()) {
				return ret-1;
			} else {
				return pos;
			}
		} else {
			return ret;
		}
	}

	static <T extends SplitPointUnit> int findBreakpointBefore(List<T> units, int strPos) {
		int i = strPos;
		while (i>=0) {
			if (units.get(i).isBreakable()) {
				break;
			}
			i--;
		}
		return i;
	}
	
	static <T extends SplitPointUnit> float totalSize(List<T> units, Map<String, T> map) {
		float ret = 0;
		Set<String> ids = new HashSet<String>();
		for (T unit : units) {
			List<String> suppIds = unit.getSupplementaryIDs();
			if (suppIds!=null) {
				for (String id : suppIds) {
					if (ids.add(id)) { //id didn't already exist in the list
						ret += map.get(id).getUnitSize();
					}
				}
			}
			ret += unit.getUnitSize();
		}
		return ret;
	}

}
