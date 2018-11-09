package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a manager for sheet groups (consecutive sheets without manual volume breaks
 * inside).
 * 
 * @author Joel Håkansson
 */
public class SheetGroupManager {
	private final SplitterLimit splitterLimit;
	private final List<SheetGroup> groups;
	private int indexInGroup = 0;
	private int index = 0;
	
	/**
	 * Creates a new sheet group manager
	 * @param splitterLimit the splitter limit
	 */
	public SheetGroupManager(SplitterLimit splitterLimit) {
		this.groups = new ArrayList<>();
		this.splitterLimit = splitterLimit;
	}

	
	/**
	 * Creates a new sheet group, adds it to the manager and returns it.
	 * @return returns the new sheet group
	 */
	public SheetGroup add() {
		SheetGroup ret = new SheetGroup();
		ret.setSplitter(new EvenSizeVolumeSplitter(new SplitterLimit() {
			private final int groupIndex = groups.size();
			
			@Override
			public int getSplitterLimit(int volume) {
				int offset = 0;
				for (int i=0; i<groupIndex; i++) {
					offset += groups.get(i).getSplitter().getVolumeCount();
				}
				return splitterLimit.getSplitterLimit(volume + offset);
			}
		}));
		groups.add(ret);
		return ret;
	}
		
	/**
	 * Gets the sheet group at the given index, zero based.
	 * @param index the index, zero based
	 * @return returns the sheet group
	 */
	public SheetGroup atIndex(int index) {
		return groups.get(index);
	}
	
	/**
	 * Gets the number of groups in the manager.
	 * @return the size
	 */
	int size() {
		return groups.size();
	}
	
	/**
	 * Gets the currently active group.
	 * @return returns the current group
	 */
	public SheetGroup currentGroup() {
		return groups.get(index);
	}

	/**
	 * Informs the manager that a new volume has started. If the current group's splitter
	 * has reached its target number of volumes (as counted by previous calls to this method),
	 * the group following that is activated. 
	 * 
	 */
	public void nextVolume() {
		if (indexInGroup+1>=currentGroup().getSplitter().getVolumeCount()) {
			nextGroup();
			indexInGroup = 0;
		} else {
			indexInGroup++;
		}
	}
	
	private void nextGroup() {
		index++;
		if (groups.size()<index) {
			throw new IllegalStateException("No more groups.");
		}
	}

	/**
	 * Returns true if the current volume is the last according to the current group's splitter.
	 * @return returns true if the current volume is the last, false otherwise
	 */
	public boolean lastInGroup() {
		return indexInGroup==currentGroup().getSplitter().getVolumeCount();
	}

	/**
	 * Gets the number of sheets in the current volume, according the the group's splitter.
	 * @return returns the number of sheets in the current volume
	 */
	public int sheetsInCurrentVolume() {
		return currentGroup().getSplitter().sheetsInVolume(1+indexInGroup);
	}
	
	/**
	 * Resets the state of this manager.
	 */
	public void resetAll() {
		for (SheetGroup g : groups) {
			g.reset();
		}
		index = 0;
		indexInGroup = 0;
	}
	
	/**
	 * Returns true if there is content left or left behind.
	 * @return returns true if the manager has content, false otherwise
	 */
	public boolean hasNext() {
		return groups.stream().anyMatch(g -> g.hasNext());
	}
	
	/**
	 * Updates the sheet count and adjusts the volume count in every group.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 */
	public void updateAll() {
		for (SheetGroup g : groups) {
			int remaining = g.hasNext() ? g.getUnits().getRemaining().size() : 0;
			g.getSplitter().updateSheetCount(g.countTotalSheets(), remaining);
		}
	}
	
	/**
	 * Counts the total number of sheets.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 * @return returns the sheet count
	 */
	public int countTotalSheets() {
		return groups.stream().mapToInt(g -> g.countTotalSheets()).sum();
	}
	
	/**
	 * Counts the remaining sheets.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 * @return returns the number of remaining sheets
	 */
	public int countRemainingSheets() {
		return groups.stream().mapToInt(g -> g.getUnits().getRemaining().size()).sum();
	}
	
	/**
	 * Counts the remaining pages.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 * @return returns the number of remaining pages
	 */
	public int countRemainingPages() {
		return groups.stream().map(g -> g.getUnits().getRemaining()).mapToInt(Sheet::countPages).sum();
	}
	
	/**
	 * Gets the total number of volumes.
	 * @return returns the total number of volumes
	 */
	public int getVolumeCount() {
		return groups.stream().mapToInt(g -> g.getSplitter().getVolumeCount()).sum();
	}

}
