package org.daisy.dotify.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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
		return split(breakPoint, force, Arrays.asList(units));
	}

	/**
	 * Gets the next result from this BreakPointHandler
	 * @param breakPoint the desired breakpoint for this result
	 * @return returns the next BreakPoint
	 */
	public SplitPoint<T> split(float breakPoint, boolean force, List<T> units) {
		if (units.size()==0) {
			// pretty simple...
			return new SplitPoint<T>(units, EMPTY_LIST, false);
		} else if (breakPoint<=0) {
			return finalizeBreakpointTrimTail(EMPTY_LIST, units, false);
		} else if (totalSize(units)<=breakPoint) {
			return finalizeBreakpointTrimTail(units, EMPTY_LIST, false);
		} else {
			return findBreakpoint(units, breakPoint, force);
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

	private float totalSize(List<T> units) {
		float ret = 0;
		for (T unit : units) {
			ret += unit.getUnitSize();
		}
		return ret;
	}
	
	private SplitPoint<T> findBreakpoint(List<T> units, float breakPoint, boolean force) {
		int strPos = forwardSkippable(units, findCollapse(units, new SizeStep(breakPoint)));
		// check next unit to see if it can be removed.
		if (strPos==units.size()-1) { // last unit?
			List<T> head = units.subList(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(units, head, tailStart, false);
		} else if (units.get(strPos).isBreakable()) {
			List<T> head = units.subList(0, strPos+1);
			int tailStart = strPos+1;
			return finalizeBreakpointFull(units, head, tailStart, false);
		} else {
			return newBreakpointFromPosition(units, strPos, force);
		}
	}
	
	private SplitPoint<T> newBreakpointFromPosition(List<T> units, int strPos, boolean force) {
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
		return finalizeBreakpointFull(units, head, tailStart, hard);
	}
	
	private SplitPoint<T> finalizeBreakpointFull(List<T> units, List<T> head, int tailStart, boolean hard) {
		List<T> tail = getTail(units, tailStart);

		if (trimTrailing) {
			head = trimTrailing(head);
		}
		
		return finalizeBreakpointTrimTail(head, tail, hard);
	}

	private SplitPoint<T> finalizeBreakpointTrimTail(List<T> head, List<T> tail, boolean hard) {
		if (trimLeading) {
			tail = trimLeading(tail);
		}
		TrimStep trimmed = new TrimStep();
		findCollapse(head, trimmed);
		head = trimmed.getResult();
		return new SplitPoint<T>(head, tail, hard);
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
		private final float breakPoint;
		
		SizeStep(float breakPoint) {
			this.breakPoint = breakPoint;
		}

		@Override
		public void addUnit(T unit) {
			size+=unit.getUnitSize();
		}

		@Override
		public boolean overflows(T buffer) {
			return size+(buffer!=null?buffer.getUnitSize():0)>breakPoint;
		}

	}
	
	class TrimStep implements StepForward<T> {
		private List<T> ret = new ArrayList<T>();

		@Override
		public void addUnit(T unit) {
			ret.add(unit);
		}

		@Override
		public boolean overflows(T buffer) {
			return false;
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

}
