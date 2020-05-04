package org.daisy.dotify.formatter.impl.search;

/**
 * TODO: Write java doc.
 */
public final class PageId {
    private final int ordinal;
    private final int globalStartIndex;
    private final int pageIndex;
    private final SequenceId sequenceId;

    /**
     * @param ordinal Numbering of the current page.
     * @param globalStartIndex Start index of the sequence that contains this page: the number of
     *                         pages that the whole body (all sequences), or the pre- or post-content
     *                         of the current volume, already contains.
     * @param sequenceId Index of the page in current sequence.
     */
    public PageId(int ordinal, int globalStartIndex, SequenceId sequenceId) {
        this.ordinal = ordinal;
        this.globalStartIndex = globalStartIndex;
        this.pageIndex = ordinal + globalStartIndex;
        this.sequenceId = sequenceId;
    }

    /**
     * 0-based index of this page in the current sequence.
     *
     * @return Index of the page in current sequence.
     */
    public int getOrdinal() {
        return ordinal;
    }

    PageId with(int ordinal) {
        return new PageId(ordinal, this.globalStartIndex, this.sequenceId);
    }

    /**
     * Global 0-based index of this page: the total number of pages (within the whole body or within
     * the current pre- or post-content) before this page. In case of duplex, "absent" pages on the
     * backside of sheets are not counted.
     */
    int getPageIndex() {
        return pageIndex;
    }

    SequenceId getSequenceId() {
        return sequenceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pageIndex;
        result = prime * result + ((sequenceId == null) ? 0 : sequenceId.hashCode());
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
        PageId other = (PageId) obj;
        if (pageIndex != other.pageIndex) {
            return false;
        }
        if (sequenceId == null) {
            if (other.sequenceId != null) {
                return false;
            }
        } else if (!sequenceId.equals(other.sequenceId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageId [ordinal=" + ordinal + ", globalStartIndex=" + globalStartIndex + ", pageIndex=" + pageIndex
                + ", sequenceId=" + sequenceId + "]";
    }

}
