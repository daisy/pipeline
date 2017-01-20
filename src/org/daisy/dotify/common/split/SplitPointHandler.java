package org.daisy.dotify.common.split;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.daisy.dotify.common.collection.SplitList;


/**
 * Breaks units into results. All allowed break points are supplied with the input.
 * 
 * @author Joel Håkansson
 *
 * @param <T> the type of split point units
 */
public class SplitPointHandler<T extends SplitPointUnit> {
	private final List<T> EMPTY_LIST = Collections.emptyList();
	private boolean trimTrailing;
	private SplitPointCost<T> cost;
	
	/**
	 * Creates a new split point handler.
	 */
	public SplitPointHandler() {
		this.trimTrailing = true;
		this.cost = new SplitPointCost<T>() {
			@Override
			public double getCost(List<T> units, int breakpoint) {
				// 1. the smaller the result, the higher the cost
				// 2. breakable units are always preferred over forced ones
				return (units.get(breakpoint).isBreakable()?1:2)*units.size()-breakpoint;
			}
		};
	}
	
	/**
	 * Splits the data at, or before, the supplied breakPoint according to the rules
	 * in the data. If force is used, rules may be broken to achieve a result.
	 * @param breakPoint the split point
	 * @param force true if force is allowed, false otherwise
	 * @param units the data
	 * @return returns a split point result
	 */
	@SafeVarargs
	public final SplitPoint<T> split(float breakPoint, boolean force, T ... units) {
		return split(breakPoint, force, new SplitPointDataList<>(units));
	}

	/**
	 * Splits the data at, or before, the supplied breakPoint according to the rules
	 * in the data. If force is used, rules may be broken to achieve a result.
	 * @param breakPoint the split point
	 * @param force true if force is allowed, false otherwise
	 * @param units the data
	 * @return returns a split point result
	 */
	public SplitPoint<T> split(float breakPoint, boolean force, List<T> units) {
		return split(breakPoint, force, new SplitPointDataList<>(units));
	}

	/**
	 * Splits the data at, or before, the supplied breakPoint according to the rules
	 * in the data. If force is used, rules may be broken to achieve a result.
	 * 
	 * @param breakPoint the split point
	 * @param force true if force is allowed, false otherwise
	 * @param data the data to split
	 * @return returns a split point result
	 */
	public SplitPoint<T> split(float breakPoint, boolean force, SplitPointDataSource<T> data) {
		if (data.isEmpty()) {
			// pretty simple...
			return new SplitPoint<>(EMPTY_LIST, EMPTY_LIST, EMPTY_LIST, EMPTY_LIST, false);
		} else if (breakPoint<=0) {
			return emptyHead(data);
		} else if (totalSize(data.getUnits(), data.getSupplements(), breakPoint)<=breakPoint) {
			return emptyTail(data);
		} else {
			int startPos = findCollapse(data.getUnits(), new SizeStep<>(breakPoint, data.getSupplements()));
			if (startPos<0) {
				return emptyHead(data);
			} else {
				return findBreakpoint(data.getUnits(), data.getSupplements(), force, startPos);
			}
		}
	}
	
	private SplitPoint<T> emptyHead(SplitPointDataSource<T> data) {
		return finalizeBreakpointTrimTail(new SplitList<>(EMPTY_LIST, EMPTY_LIST), data.getUnits(), data.getSupplements(), false);
	}
	
	private SplitPoint<T> emptyTail(SplitPointDataSource<T> data) {
		return finalizeBreakpointTrimTail(new SplitList<>(data.getUnits(), EMPTY_LIST), EMPTY_LIST, data.getSupplements(), false);
	}

	/**
	 * Returns true if trailing skippable units will be removed.
	 * @return true if trailing skippable units will be removed, false otherwise
	 */
	public boolean isTrimTrailing() {
		return trimTrailing;
	}

	/**
	 * Sets whether to trim trailing skippable units or not.
	 * @param trimTrailing the new value
	 */
	public void setTrimTrailing(boolean trimTrailing) {
		this.trimTrailing = trimTrailing;
	}
	
	/**
	 * Gets the cost function
	 * @return returns the cost function
	 */
	public SplitPointCost<T> getCost() {
		return cost;
	}
	
	/**
	 * <p>Sets the cost function used when determining the optimal <i>forced</i> split point.</p>
	 * <p>Note that the cost function is only used if there are no breakable units available.</p>
	 * @param cost the cost function
	 * @throws IllegalArgumentException if cost is null
	 */
	public void setCost(SplitPointCost<T> cost) {
		if (cost==null) {
			throw new IllegalArgumentException("Null not allowed.");
		}
		this.cost = cost;
	}
	
	private SplitPoint<T> findBreakpoint(List<T> units, Supplements<T> map, boolean force, int startPos) {
		int strPos = forwardSkippable(units, startPos);
		// check next unit to see if it can be removed.
		if (strPos==units.size()-1) { // last unit?
			List<T> head = units.subList(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(units, head, tailStart, map, false);
		} else {
			return newBreakpointFromPosition(units, strPos, map, force);
		}
	}
	
	private SplitPoint<T> newBreakpointFromPosition(List<T> units, int strPos, Supplements<T> map, boolean force) {
		// back up
		BreakPointScannerResult result=findBreakpointBefore(units, strPos, cost);
		List<T> head;
		boolean hard = false;
		int tailStart;
		if (result.bestBreakable!=result.bestSplitPoint) { // no breakable found, break hard 
			if (force) {
				hard = true;
				head = units.subList(0, result.bestSplitPoint+1);
				tailStart = result.bestSplitPoint+1;
			} else {
				head = EMPTY_LIST;
				tailStart = 0;
			}
		} else {
			head = units.subList(0, result.bestBreakable+1);
			tailStart = result.bestBreakable+1;
		}
		return finalizeBreakpointFull(units, head, tailStart, map, hard);
	}
	
	private SplitPoint<T> finalizeBreakpointFull(List<T> units, List<T> head, int tailStart, Supplements<T> map, boolean hard) {
		List<T> tail = getTail(units, tailStart);

		if (trimTrailing) {
			return finalizeBreakpointTrimTail(trimTrailing(head), tail, map, hard);
		} else {
			return finalizeBreakpointTrimTail(new SplitList<>(head, EMPTY_LIST), tail, map, hard);
		}
	}

	private SplitPoint<T> finalizeBreakpointTrimTail(SplitList<T> head, List<T> tail, Supplements<T> map, boolean hard) {
		TrimStep<T> trimmed = new TrimStep<>(map);
		findCollapse(head.getFirstPart(), trimmed);
		List<T> discarded = trimmed.getDiscarded();
		discarded.addAll(head.getSecondPart());
		return new SplitPoint<>(trimmed.getResult(), trimmed.getSupplements(), tail, discarded, hard);
	}

	/**
	 * Trims leading skippable units in the supplied list. The result is backed by the
	 * original list. 
	 * 
	 * @param in the list to trim
	 * @param <T> the type of split list
	 * @return the list split in two parts, one with the leading skippable units, one with
	 * the remainder
	 */
	public static <T extends SplitPointUnit> SplitList<T> trimLeading(List<T> in) {
		int i;
		for (i = 0; i<in.size(); i++) {
			if (!in.get(i).isSkippable()) {
				break;
			}
		}
		return SplitList.split(in, i);
	}

	static <T extends SplitPointUnit> T maxSize(T u1, T u2) {
		return (u1.getUnitSize()>=u2.getUnitSize()?u1:u2); 
	}
	
	static <T extends SplitPointUnit> SplitList<T> trimTrailing(List<T> in) {
		int i;
		for (i = in.size()-1; i>=0; i--) {
			if (!in.get(i).isSkippable()) {
				break;
			}
		}
		return SplitList.split(in, i+1);
	}
	
	static <T extends SplitPointUnit> List<T> getTail(List<T> units, int tailStart) {
		if (units.size()>tailStart) {
			List<T> tail = units.subList(tailStart, units.size());
			return tail;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Finds the index for the last unit that fits into the given space
	 * @param charsStr
	 * @param impl
	 * @return returns the index for the last unit
	 */
	static <T extends SplitPointUnit> int findCollapse(List<T> charsStr, StepForward<T> impl) {
		int units = -1;
		T maxCollapsable = null;
		for (T c : charsStr) {
			units++;
			if (c.isCollapsible()) {
				if (maxCollapsable!=null) {
					if (maxCollapsable.collapsesWith(c)) {
						if (maxSize(maxCollapsable, c)==c) {
							//new one is now max, add the previous to collapsed
							impl.addDiscarded(maxCollapsable);
							maxCollapsable = c;
						} else {
							//old one is max, add the new one to collapsed
							impl.addDiscarded(c);
						}
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

	static <T extends SplitPointUnit> BreakPointScannerResult findBreakpointBefore(List<T> units, int strPos, SplitPointCost<T> cost) {
		BreakPointScannerResult res = new BreakPointScannerResult();
		res.bestBreakable = -1;
		res.bestSplitPoint = strPos;
		double currentCost = Double.MAX_VALUE;
		double currentBreakableCost = Double.MAX_VALUE;
		for (int index=0; index<=strPos; index++) {
			double c = cost.getCost(units, index);
			if (c<currentCost) { // this should always be true for the first unit
				res.bestSplitPoint = index;
				currentCost = c;
			}
			if (c<currentBreakableCost && units.get(index).isBreakable()) {
				res.bestBreakable = index;
				currentBreakableCost = c;
			}
		}
		return res;
	}
	
	private static class BreakPointScannerResult {
		int bestBreakable;
		int bestSplitPoint;
	}
	
	/**
	 * If the total size is less than the limit, the size is returned, otherwise a value greater
	 * than the limit is returned.
	 * 
	 * @param units the units
	 * @param map the supplements
	 * @param limit the maximum width that is relevant to calculate
	 * @return returns the size 
	 */
	static <T extends SplitPointUnit> float totalSize(List<T> units, Supplements<T> map, float limit) {
		float ret = 0;
		Set<String> ids = new HashSet<>();
		// we check up to the limit and beyond by one element, to make sure that we check enough units
		for (int i=0; i<units.size() && ret<=limit; i++) {
			T unit = units.get(i);
			List<String> suppIds = unit.getSupplementaryIDs();
			if (suppIds!=null) {
				for (String id : suppIds) {
					if (ids.add(id)) { //id didn't already exist in the list
						T item = map.get(id);
						if (item!=null) {
							ret += item.getUnitSize();
						}
					}
				}
			}
			if (i==units.size()-1) {
				ret += unit.getLastUnitSize();
			} else {
				ret += unit.getUnitSize();
			}
		}
		return ret;
	}

}
