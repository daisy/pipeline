package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides a manager for {@link SheetGroup}.</p>
 *
 * <p>Used by {@link org.daisy.dotify.formatter.impl.VolumeProvider}.</p>
 *
 * @author Joel Håkansson
 */
public class SheetGroupManager {
    private final List<SheetGroup> groups;
    private int groupIndex = 0;
    private int volumeIndex = 0;

    /**
     * Creates a new sheet group manager.
     */
    public SheetGroupManager() {
        this.groups = new ArrayList<>();
    }


    /**
     * Creates a new sheet group, adds it to the manager and returns it.
     *
     * @return returns the new sheet group
     */
    public SheetGroup add() {
        SheetGroup ret = new SheetGroup();
        groups.add(ret);
        return ret;
    }

    /**
     * Gets the sheet group at the given index, zero based.
     *
     * @param index the index, zero based
     * @return returns the sheet group
     */
    public SheetGroup atIndex(int index) {
        return groups.get(index);
    }

    /**
     * Gets the number of groups in the manager.
     *
     * @return the size
     */
    int size() {
        return groups.size();
    }

    /**
     * Gets the currently active group.
     *
     * @return returns the current group
     */
    public SheetGroup currentGroup() {
        return groups.get(groupIndex);
    }

    /**
     * Informs the manager that a new volume has started. If the current group's splitter
     * has reached its target number of volumes (as counted by previous calls to this method),
     * the group following that is activated.
     */
    public void nextVolume() {
        if (groupIndex == groups.size()) {
            throw new IllegalStateException("No more groups.");
        }
        volumeIndex++;
        if (!currentGroup().hasNext()) {
            groupIndex++;
        }
    }

    /**
     * Resets the state of this manager.
     */
    public void resetAll() {
        for (SheetGroup g : groups) {
            g.reset();
        }
        groupIndex = 0;
        volumeIndex = 0;
    }

    /**
     * Returns true if there is content left.
     *
     * @return returns true if the manager has content, false otherwise
     */
    public boolean hasNext() {
        return groups.stream().anyMatch(g -> g.hasNext());
    }

    /**
     * Counts the total number of sheets.
     * <b>Note: only use after all volumes have been calculated.</b>
     *
     * @return returns the sheet count
     */
    public int countTotalSheets() {
        return groups.stream().mapToInt(g -> g.countTotalSheets()).sum();
    }

    /**
     * Gets the total number of volumes.
     *
     * @return returns the total number of volumes
     */
    public int getVolumeCount() {
        return volumeIndex;
    }

}
