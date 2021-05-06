package org.daisy.dotify.formatter.impl.search;

/**
 * Sheet starting at a specific location within a block.
 */
public class SheetIdentity {
    private final Space space;
    private final Integer volumeIndex;
    private final Integer volumeGroup;
    private final BlockLineLocation blockLineLocation;

    /**
     * Create a SheetIdentity from a BlockLineLocation. The BlockLineLocation points to the last
     * line of the preceding sheet within the current volume group or pre-/post-content
     * section. Additional arguments are needed to uniquely identify the sheet when it is the first
     * of the current volume group or pre-/post-content section.
     *
     * @param blockLineLocation location of the last line of the preceding sheet within the current
     *                          volume group or pre-/post-content section, or null if there is no
     *                          preceding sheet.
     * @param space             the space ({@link Space#BODY}, {@link Space#PRE_CONTENT} or {@link
     *                          Space#POST_CONTENT}).
     * @param volumeIndex       the volume index if <code>space</code> is {@link Space#PRE_CONTENT}
     *                          or {@link Space#POST_CONTENT}.
     * @param volumeGroup       the volume group index if <code>space</code> is
     *                          {@link Space#BODY}.
     */
    public SheetIdentity(
        BlockLineLocation blockLineLocation,
        Space space,
        Integer volumeIndex,
        Integer volumeGroup
    ) {
        this.space = space;
        if (space == Space.BODY) {
            if (volumeIndex != null) {
                volumeIndex = null;
            }
            if (volumeGroup == null) {
                throw new IllegalArgumentException();
            }
        } else {
            if (volumeIndex == null || volumeGroup != null) {
                throw new IllegalArgumentException();
            }
        }
        this.volumeIndex = volumeIndex;
        this.volumeGroup = volumeGroup;
        this.blockLineLocation = blockLineLocation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (space == null ? 0 : space.hashCode());
        result = prime * result + (blockLineLocation == null ? 0 : blockLineLocation.hashCode());
        result = prime * result + (volumeIndex == null ? 0 : volumeIndex);
        result = prime * result + (volumeGroup == null ? 0 : volumeGroup);
        return result;
    }

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
        SheetIdentity other = (SheetIdentity) obj;
        if (space != other.space) {
            return false;
        }
        if (blockLineLocation == null) {
            if (other.blockLineLocation != null) {
                return false;
            }
        } else if (!blockLineLocation.equals(other.blockLineLocation)) {
            return false;
        }
        if (volumeIndex == null) {
            if (other.volumeIndex != null) {
                return false;
            }
        } else if (!volumeIndex.equals(other.volumeIndex)) {
            return false;
        }
        if (volumeGroup == null) {
            if (other.volumeGroup != null) {
                return false;
            }
        } else if (!volumeGroup.equals(other.volumeGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SheetIdentity [space=" + space
                + (volumeIndex == null ? "" : ", volumeIndex=" + volumeIndex)
                + (volumeGroup == null ? "" : ", volumeGroup=" + volumeGroup)
                + ", blockLineLocation=" + blockLineLocation + "]";
    }

}
