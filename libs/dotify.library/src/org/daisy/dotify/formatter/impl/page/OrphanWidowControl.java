package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;

/**
 * Provides an orphan/widow control utility.
 *
 * @author Joel Håkansson
 */
class OrphanWidowControl {
    private final int orphans, widows, size;
    private final BlockAddress blockId;
    private OrphanWidowControl parent;
    private int rowCount = 0;

    OrphanWidowControl(OrphanWidowControl template, OrphanWidowControl parent) {
        this.orphans = template.orphans;
        this.widows = template.widows;
        this.size = template.size;
        this.blockId = template.blockId;
        this.rowCount = template.rowCount;
        this.parent = parent;
    }

    /**
     * Creates a new instance.
     *
     * @param orphans the minimum number of block-opening lines that may appear by
     *                themselves at the bottom of a page.
     * @param widows  the minimum number of block-ending lines that may fall at the
     *                beginning of the following page.
     * @param size    the expected total number of rows in the block
     * @param blockId the address of the block
     * @param parent  an {@link OrphanWidowControl} to inherit from, or {@code null}
     */
    OrphanWidowControl(int orphans, int widows, int size, BlockAddress blockId, OrphanWidowControl parent) {
        this.orphans = orphans;
        this.widows = widows;
        this.size = size;
        this.blockId = blockId;
        this.parent = parent;
    }

    /**
     * Specify that a new row was added to the block.
     *
     * If this {@link OrphanWidowControl} inherits from a parent, the row count of the
     * parent is also increased.
     *
     * @return the current number of rows in the block
     */
    int increaseRowCount() {
        if (parent != null) {
            parent.increaseRowCount();
        }
        return ++rowCount;
    }

    /**
     * Store the row count of this block in the given {@link CrossReferenceHandler}.
     *
     * If this {@link OrphanWidowControl} inherits from a parent, the row count of the
     * parent is also stored.
     *
     * @param crh the {@link CrossReferenceHandler} that should be used to store the info
     */
    void storeRowCount(CrossReferenceHandler crh) {
        if (blockId != null) {
            // note that crh.commitRowCount() must be called at the end of the iteration
            // this happens in VolumeProvider
            crh.keepRowCount(blockId, rowCount);
        }
        if (parent != null) {
            parent.storeRowCount(crh);
        }
    }

    /**
     * Returns true if a break is allowed after the current row.
     *
     * @return whether a break is allowed after the current row
     */
    boolean allowsBreakAfter() {
        if (parent != null && !parent.allowsBreakAfter()) {
            return false;
        }
        if (rowCount >= size) {
            return true;
        } else {
            return rowCount >= orphans && widows <= size - rowCount;
        }
    }
}
