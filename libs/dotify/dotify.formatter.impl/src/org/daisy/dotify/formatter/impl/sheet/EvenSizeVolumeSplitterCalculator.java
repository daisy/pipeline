package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides information needed to split a book into volumes. 
 * 
 * @author Joel Håkansson
 */
class EvenSizeVolumeSplitterCalculator {
	// number of sheets
	private final int sheets;
	// breakpoints
	private final Set<Integer> breakpoints;

	private final List<Integer> volumeSize;
	private final Map<Integer, Integer> volumeForSheet;

	public EvenSizeVolumeSplitterCalculator(int sheets, final int splitterMax) {
		this(sheets, splitterMax, 0);
	}
	/**
	 * 
	 * @param sheets total number of sheets
	 * @param splitterMax maximum number of sheets in a volume
	 */
	public EvenSizeVolumeSplitterCalculator(int sheets, SplitterLimit splitterMax) {
		this(sheets, splitterMax, 0);
	}
	
	public EvenSizeVolumeSplitterCalculator(int sheets, final int splitterMax, int volumeOffset) {
		this(sheets, new SplitterLimit() {
			@Override
			public int getSplitterLimit(int volume) {
				return splitterMax;
			}}, volumeOffset);
	}
	/**
	 * @param sheets
	 * @param splitterMax
	 * @param volumeOffset
	 */
	public EvenSizeVolumeSplitterCalculator(int sheets, SplitterLimit splitterMax, int volumeOffset) {
		this.sheets = sheets;
		SplitterSpecification spec = getSplitterSpecification(sheets, splitterMax, volumeOffset);
		int volumes = spec.volumeCount;
		this.volumeSize = trimHeadroom(makeVolumeSizes(volumes, splitterMax), spec.sheetCapacity - sheets);
		this.breakpoints = makeBreakpoints(volumeSize);
		this.volumeForSheet = makeVolumeForSheetMap(sheets, breakpoints);
	}
	
	private static class SplitterSpecification {
		int sheetCapacity;
		int volumeCount;
	}
	
	/**
	 * Calculates the splitter specification.
	 * 
	 * @param sheets then number of sheets
	 * @param splitterMax the splitter limits
	 * @param volumeOffset a number of additional volumes (not strictly needed to accommodate the number of sheets)
	 * @return returns the volume capacity
	 */
	private static SplitterSpecification getSplitterSpecification(int sheets, SplitterLimit splitterMax, int volumeOffset) {
		SplitterSpecification ret = new SplitterSpecification();
		ret.sheetCapacity = 0;
		{
			int i=0;
			while (ret.sheetCapacity < sheets) {
				ret.sheetCapacity += splitterMax.getSplitterLimit(i+1);
				i++;
			}
			for (int x=0; x<volumeOffset; x++) {
				ret.sheetCapacity += splitterMax.getSplitterLimit(i+x+1);
			}
			ret.volumeCount = i + volumeOffset;
		}
		return ret;
	}
	
	/**
	 * Makes a list of volume sizes
	 * @param volumes the number of volumes
	 * @param splitterMax the volume 
	 * @return returns a list with the sizes of volumes
	 */
	private static List<Integer> makeVolumeSizes(int volumes, SplitterLimit splitterMax) {
		List<Integer> volumeSize = new ArrayList<>();
		for (int i=1; i<=volumes; i++) {
			volumeSize.add(splitterMax.getSplitterLimit(i));
		}
		return volumeSize;
	}
	
	/**
	 * Trims down left over headroom by removing an one sheet from each volume,
	 * starting with the last volume until there is no more headroom.
	 * 
	 * @param volumeSize the volume sizes
	 * @param headroom the number of sheets to trim off
	 * @return returns the volumeSize list, for convenience
	 * @throws IllegalArgumentException if the volume size goes below 1
	 */
	private static List<Integer> trimHeadroom(List<Integer> volumeSize, int headroom) {
		int volumes = volumeSize.size();
		int v = 0;
		for (int i = headroom; i>0; i--) {
			int m = (volumes-1) - (v % volumes); // 4, 3, 2, 1, 0
			int current = volumeSize.get(m);
			if (current==1) {
				throw new IllegalArgumentException("Volume sizes too uneven: " + volumeSize);
			}
			volumeSize.set(m, current-1);
			v++;
		}
		return volumeSize;
	}
	
	/**
	 * Makes a set of volume breakpoints for the volume sizes provided
	 * @param volumeSize the volume sizes
	 * @return returns a set of volume breakpoints
	 */
	private static Set<Integer> makeBreakpoints(List<Integer> volumeSize) {
		int volumes = volumeSize.size();
		int vx = volumes -1;
		int breakpoint = 0;
		Set<Integer> breakpoints = new HashSet<Integer>();
		for (int i=0; i<vx; i++) {
			breakpoint += volumeSize.get(i);
			breakpoints.add(breakpoint);
		}
		return breakpoints;
	}
	
	/**
	 * Makes a volume for sheet map
	 * @param sheets the number of sheets
	 * @param breakpoints the breakpoints
	 * @return returns a volume for sheet map
	 */
	private static Map<Integer, Integer> makeVolumeForSheetMap(int sheets, Set<Integer> breakpoints) {
		Map<Integer, Integer> volumeForSheet = new HashMap<>();
		int currentVolume = 1;
		for (int i=1; i<=sheets; i++) {
			if (breakpoints.contains(i)) {
				currentVolume++;
			}
			volumeForSheet.put(i, currentVolume);
		}
		return volumeForSheet;
	}
	
	/**
	 * Tests if the supplied sheetIndex is a breakpoint. This sheetIndex counts all sheets,
	 * including sheets inserted in volume splitting. 
	 * @param sheetIndex sheet index, one based 
	 * @return returns true if the sheet is a breakpoint, false otherwise
	 * @throws IndexOutOfBoundsException if sheetIndex is outside of agreed boundaries
	 */
	public boolean isBreakpoint(int sheetIndex) {
		if (sheetIndex<1) {
			throw new IndexOutOfBoundsException("Sheet index must be greater than zero: " + sheetIndex);
		}
		if (sheetIndex>sheets) {
			throw new IndexOutOfBoundsException("Sheet index must not exceed agreed value.");
		}
		return breakpoints.contains(sheetIndex);
	}
	
	/**
	 * Gets the number of sheets in a volume
	 * @param volIndex volume index, one-based
	 * @return returns the number of sheets in the volume
	 */
	public int sheetsInVolume(int volIndex) {
		return volumeSize.get(volIndex-1);
	}
	
	/**
	 * Gets the number of volumes.
	 * @return returns the number of volumes
	 */
	public int getVolumeCount() {
		return volumeSize.size();
	}
	
	/**
	 * Gets the volume for the supplied sheetIndex. This sheetIndex counts all sheets,
	 * including sheets inserted in volume splitting. 
	 * @param sheetIndex sheet index, one based 
	 * @return returns the volume number, one based
	 * @throws IndexOutOfBoundsException if sheetIndex is outside of agreed boundaries
	 */
	public int getVolumeForSheet(int sheetIndex) {
		if (sheetIndex<1) {
			throw new IndexOutOfBoundsException("Sheet index must be greater than zero: " + sheetIndex);
		}
		if (sheetIndex>sheets) {
			throw new IndexOutOfBoundsException("Sheet index must not exceed agreed value.");
		}
		return volumeForSheet.get(sheetIndex);
	}

	//volumeForSheet and breakpoints are not included below, as their values can be determined from the other values
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((volumeSize == null) ? 0 : volumeSize.hashCode());
		return result;
	}

	//volumeForSheet and breakpoints are not included below, as their values can be determined from the other values
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EvenSizeVolumeSplitterCalculator other = (EvenSizeVolumeSplitterCalculator) obj;
		if (volumeSize == null) {
			if (other.volumeSize != null) {
				return false;
			}
		} else if (!volumeSize.equals(other.volumeSize)) {
			return false;
		}
		return true;
	}
	
}
