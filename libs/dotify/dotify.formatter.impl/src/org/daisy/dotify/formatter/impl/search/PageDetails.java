package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.api.formatter.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Write java doc.
 */
public class PageDetails {
    private final boolean duplex;
    private final PageId pageId;
    private final BlockLineLocation loc;
    private final int pageNumberOffset;
    private int volumeNumber;
    private int contentMarkersBegin;
    private int contentIdentifiersBegin;

    private final ArrayList<Marker> markers;
    private final ArrayList<String> identifiers;

    /**
     * @param duplex Double sided output.
     * @param pageId ID of the page.
     * @param loc Block location for the start of this page.
     * @param pageNumberOffset The offset that needs to be added to the 1-based index of the page
     *                         within the sequence to obtain the desired page number.
     */
    public PageDetails(boolean duplex, PageId pageId, BlockLineLocation loc, int pageNumberOffset) {
        this.duplex = duplex;
        this.pageId = pageId;
        this.loc = loc;
        this.pageNumberOffset = pageNumberOffset;
        this.markers = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.contentMarkersBegin = 0;
        this.contentIdentifiersBegin = 0;
        this.volumeNumber = 0;
    }

    public PageDetails with(int ordinal) {
        return new PageDetails(
                this.duplex,
                this.pageId.with(ordinal),
                this.loc,
                pageNumberOffset);
    }

    private boolean duplex() {
        return duplex;
    }

    SequenceId getSequenceId() {
        return pageId.getSequenceId();
    }

    public PageId getPageId() {
        return pageId;
    }

    public int getPageNumber() {
        return pageId.getOrdinal() + pageNumberOffset + 1;
    }

    /**
     * BlockLineLocation of the last line of the previous page.
     *
     * @return page location for last line of previous page.
     */
    public BlockLineLocation getPageLocation() {
        return loc;
    }

    /**
     * The volume number.
     *
     * @return current volume number.
     */
    int getVolumeNumber() {
        return volumeNumber;
    }

    void setVolumeNumber(int volNumber) {
        this.volumeNumber = volNumber;
    }

    /**
     * Sets content markers to begin at the current index in the markers list.
     */
    public void startsContentMarkers() {
        contentMarkersBegin = getMarkers().size();
    }

    /**
     * Sets content identifiers to begin at the current index in the identifiers list.
     */
    public void startsContentIdentifiers() {
        contentIdentifiersBegin = getIdentifiers().size();
    }

    /*
     * This method is unused at the moment, but could be activated once additional scopes are added to the API,
     * namely SPREAD_WITHIN_SEQUENCE
     */
    @SuppressWarnings("unused")
    private boolean isWithinSequenceSpreadScope(int offset) {
        return offset == 0 ||
                (
                        duplex() &&
                                (
                                        (offset == 1 && pageId.getOrdinal() % 2 == 1) ||
                                                (offset == -1 && pageId.getOrdinal() % 2 == 0)
                                )
                );
    }

    boolean isWithinSpreadScope(int offset, PageDetails other) {
        if (other == null) {
            return ((offset == 1 && pageId.getOrdinal() % 2 == 1) ||
                    (offset == -1 && pageId.getOrdinal() % 2 == 0));
        } else {
            return (
                (offset == 1 && pageId.getOrdinal() % 2 == 1 && duplex() == true) ||
                (
                    offset == -1 &&
                    pageId.getOrdinal() % 2 == 0 &&
                    other.duplex() == true &&
                    other.pageId.getOrdinal() % 2 == 1
                )
            );
        }
    }

    /**
     * Get all markers for this page.
     *
     * @return returns a list of all markers on a page
     */
    public List<Marker> getMarkers() {
        return markers;
    }

    /**
     * Get markers for this page excluding markers before text content.
     *
     * @return returns a list of markers on a page
     */
    List<Marker> getContentMarkers() {
        return getMarkers().subList(contentMarkersBegin, getMarkers().size());
    }

    /**
     * Get all identifiers for this page.
     *
     * @return returns a list of all identifiers on a page
     */
    public List<String> getIdentifiers() {
        return identifiers;
    }

    /**
     * Get identifiers for this page excluding identifiers before text content.
     *
     * @return returns a list of identifiers on a page
     */
    public List<String> getContentIdentifiers() {
        return getIdentifiers().subList(contentIdentifiersBegin, getIdentifiers().size());
    }

    PageDetails getPageInScope(View<PageDetails> pageView, int offset, boolean adjustOutOfBounds) {
        if (offset == 0) {
            return this;
        } else {
            if (pageView != null) {
                int next = pageView.toLocalIndex(pageId.getPageIndex()) + offset;
                int size = pageView.size();
                if (adjustOutOfBounds) {
                    next = Math.min(size - 1, Math.max(0, next));
                }
                if (next < size && next >= 0) {
                    return pageView.get(next);
                }
            }
            return null;
        }
    }

    boolean isWithinSheetScope(int offset) {
        return offset == 0 ||
            (
                duplex() &&
                (
                    (offset == 1 && pageId.getOrdinal() % 2 == 0) ||
                    (offset == -1 && pageId.getOrdinal() % 2 == 1)
                )
            );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contentMarkersBegin;
        result = prime * result + contentIdentifiersBegin;
        result = prime * result + (duplex ? 1231 : 1237);
        result = prime * result + ((markers == null) ? 0 : markers.hashCode());
        result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
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
        PageDetails other = (PageDetails) obj;
        if (contentMarkersBegin != other.contentMarkersBegin) {
            return false;
        }
        if (contentIdentifiersBegin != other.contentIdentifiersBegin) {
            return false;
        }
        if (duplex != other.duplex) {
            return false;
        }
        if (markers == null) {
            if (other.markers != null) {
                return false;
            }
        } else if (!markers.equals(other.markers)) {
            return false;
        }
        if (identifiers == null) {
            if (other.identifiers != null) {
                return false;
            }
        } else if (!identifiers.equals(other.identifiers)) {
            return false;
        }
        if (pageId == null) {
            if (other.pageId != null) {
                return false;
            }
        } else if (!pageId.equals(other.pageId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageDetails [duplex=" + duplex + ", pageId=" + pageId + ", volumeNumber=" + volumeNumber
                + ", contentMarkersBegin=" + contentMarkersBegin + ", markers=" + markers
                + ", contentIdentifiersBegin=" + contentIdentifiersBegin + ", identifiers=" + identifiers + "]";
    }

}
