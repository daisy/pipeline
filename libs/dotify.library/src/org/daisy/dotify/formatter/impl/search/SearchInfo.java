package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerReference;
import org.daisy.dotify.api.formatter.MarkerReference.MarkerSearchDirection;
import org.daisy.dotify.api.formatter.MarkerReference.MarkerSearchScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * TODO: Write java doc.
 */
class SearchInfo {

    private final Map<DocumentSpace, DocumentSpaceData> spaces;
    private final Map<String, PageDetails> uncommitted;
    private boolean dirty;

    SearchInfo() {
        this.spaces = new HashMap<>();
        this.uncommitted = new HashMap<>();
        this.dirty = false;
    }

    private static String toKey(PageDetails value) {
        return value.getSequenceId().getSpace() + "-" + value.getPageId();
    }

    void keepPageDetails(PageDetails value) {
        if (value.getPageId().getPageIndex() < 0) {
            throw new IllegalArgumentException("Negative page id not allowed.");
        }
        uncommitted.put(toKey(value), value);
    }

    void commitPageDetails() {
        for (Entry<String, PageDetails> entry : uncommitted.entrySet()) {
            PageDetails value = entry.getValue();
            DocumentSpaceData data = getViewForSpace(value.getSequenceId().getSpace());
            while (value.getPageId().getPageIndex() >= data.pageDetails.size()) {
                data.pageDetails.add(null);
            }
            PageDetails old = data.pageDetails.set(value.getPageId().getPageIndex(), value);
            // Only check the previous value if dirty isn't already true
            if (!dirty && !value.equals(old)) {
                dirty = true;
            }
        }
        uncommitted.clear();
    }

    View<PageDetails> getPageView(DocumentSpace space) {
        return new View<PageDetails>(
                getViewForSpace(space).pageDetails,
                0,
                getViewForSpace(space).pageDetails.size()
        );
    }

    View<PageDetails> getContentsInVolume(int volumeNumber, DocumentSpace space) {
        return getViewForSpace(space).volumeViews.get(volumeNumber);
    }

    View<PageDetails> getContentsInSequence(SequenceId seqId) {
        return getViewForSpace(seqId.getSpace()).sequenceViews.get(seqId);
    }

    void setSequenceScope(SequenceId sequenceId, int fromIndex, int toIndex) {
        View<PageDetails> pw = new View<PageDetails>(
            getViewForSpace(sequenceId.getSpace()).pageDetails,
            fromIndex,
            toIndex
        );
        getViewForSpace(sequenceId.getSpace()).sequenceViews.put(sequenceId, pw);
    }

    void setVolumeScope(int volumeNumber, int fromIndex, int toIndex) {
        setVolumeScope(volumeNumber, fromIndex, toIndex, DocumentSpace.BODY);
    }

    void setVolumeScope(int volumeNumber, int fromIndex, int toIndex, DocumentSpace space) {
        View<PageDetails> pw = new View<PageDetails>(getViewForSpace(space).pageDetails, fromIndex, toIndex);
        for (PageDetails p : pw.getItems()) {
            p.setVolumeNumber(volumeNumber);
        }
        getViewForSpace(space).volumeViews.put(volumeNumber, pw);
    }

    DocumentSpaceData getViewForSpace(DocumentSpace space) {
        DocumentSpaceData ret = spaces.get(space);
        if (ret == null) {
            ret = new DocumentSpaceData();
            spaces.put(space, ret);
        }
        return ret;
    }

    PageDetails getPageInSequenceWithOffset(PageDetails base, int offset, boolean adjustOutOfBounds) {
        return offset == 0 ? base :
                base.getPageInScope(getContentsInSequence(base.getSequenceId()), offset, adjustOutOfBounds);
    }

    PageDetails getPageInDocumentWithOffset(PageDetails base, int offset, boolean adjustOutOfBounds) {
        if (offset == 0) {
            return base;
        } else {
            //Keep while moving: getPageInScope(base.getSequenceParent().getParent().getPageView()...
            return base.getPageInScope(getPageView(base.getSequenceId().getSpace()), offset, adjustOutOfBounds);
        }
    }

    PageDetails getPageInVolumeWithOffset(PageDetails base, int offset, boolean adjustOutOfBounds) {
        if (offset == 0) {
            return base;
        } else {
            //Keep while moving: base.getPageInScope(
            //  base.getSequenceParent().getParent().getContentsInVolume(base.getVolumeNumber()),
            //  offset,
            //  adjustOutOfBounds
            //);
            return base.getPageInScope(
                getContentsInVolume(base.getVolumeNumber(), base.getSequenceId().getSpace()),
                offset,
                adjustOutOfBounds
            );
        }
    }

    boolean isWithinVolumeSpreadScope(PageDetails base, int offset) {
        if (offset == 0) {
            return true;
        } else {
            PageDetails n = getPageInVolumeWithOffset(base, offset, false);
            return base.isWithinSpreadScope(offset, n);
        }
    }

    /*
     * This method is unused at the moment, but could be activated if additional scopes are added to the API,
     * namely SPREAD_WITHIN_DOCUMENT
     */
    boolean isWithinDocumentSpreadScope(PageDetails base, int offset) {
        if (offset == 0) {
            return true;
        } else {
            PageDetails n = getPageInDocumentWithOffset(base, offset, false);
            return base.isWithinSpreadScope(offset, n);
        }
    }

    boolean shouldAdjustOutOfBounds(PageDetails base, MarkerReference markerRef) {
        if (markerRef.getSearchDirection() == MarkerSearchDirection.FORWARD && markerRef.getOffset() >= 0 ||
                markerRef.getSearchDirection() == MarkerSearchDirection.BACKWARD && markerRef.getOffset() <= 0) {
            return false;
        } else {
            switch (markerRef.getSearchScope()) {
                case PAGE_CONTENT:
                case PAGE:
                    return false;
                case SEQUENCE:
                case VOLUME:
                case DOCUMENT:
                    return true;
                case SPREAD_CONTENT:
                case SPREAD:
                    //return  isWithinSequenceSpreadScope(markerRef.getOffset());
                    //return  isWithinDocumentSpreadScope(markerRef.getOffset());
                    return isWithinVolumeSpreadScope(base, markerRef.getOffset());
                case SHEET:
                    return base.isWithinSheetScope(markerRef.getOffset()) &&
                            markerRef.getSearchDirection() == MarkerSearchDirection.BACKWARD;
                default:
                    throw new RuntimeException("Error in code. Missing implementation for value: " +
                            markerRef.getSearchScope());
            }
        }
    }

    String findMarker(final PageDetails page, final MarkerReference markerRef) {
        PageDetails currentPage = page;
        while (currentPage != null) {
            if (
                markerRef.getSearchScope() == MarkerSearchScope.VOLUME
            ) {
                throw new RuntimeException("Marker reference scope not implemented: " + markerRef.getSearchScope());
            }
            int dir = 1;
            int index = 0;
            int count = 0;
            List<Marker> m;
            boolean skipLeading = false;
            if (markerRef.getSearchScope() == MarkerSearchScope.PAGE_CONTENT) {
                skipLeading = true;
            } else if (markerRef.getSearchScope() == MarkerSearchScope.SPREAD_CONTENT) {
                PageDetails prevPageInVolume = getPageInVolumeWithOffset(currentPage, -1, false);
                if (prevPageInVolume == null || !currentPage.isWithinSpreadScope(-1, prevPageInVolume)) {
                    skipLeading = true;
                }
            }
            if (skipLeading) {
                m = currentPage.getContentMarkers();
            } else {
                m = currentPage.getMarkers();
            }
            if (markerRef.getSearchDirection() == MarkerSearchDirection.BACKWARD) {
                dir = -1;
                index = m.size() - 1;
            }
            while (count < m.size()) {
                Marker m2 = m.get(index);
                if (m2.getName().equals(markerRef.getName())) {
                    return m2.getValue();
                }
                index += dir;
                count++;
            }
            if (markerRef.getSearchScope() == MarkerSearchScope.DOCUMENT) {
                currentPage = currentPage.getPageInScope(
                    getPageView(currentPage.getSequenceId().getSpace()),
                    dir,
                    false
                );
            } else if (
                markerRef.getSearchScope() == MarkerSearchScope.SEQUENCE ||
                markerRef.getSearchScope() == MarkerSearchScope.SHEET && currentPage.isWithinSheetScope(dir) //||
                //markerRef.getSearchScope() == MarkerSearchScope.SPREAD && page.isWithinSequenceSpreadScope(dir)
            ) {
                //Keep while moving: next = page.getPageInScope(page.getSequenceParent(), dir, false);
                currentPage = currentPage.getPageInScope(
                    getContentsInSequence(currentPage.getSequenceId()),
                    dir,
                    false
                );
            } else if (
                (
                    markerRef.getSearchScope() == MarkerSearchScope.SPREAD ||
                    markerRef.getSearchScope() == MarkerSearchScope.SPREAD_CONTENT
                ) &&
                isWithinVolumeSpreadScope(currentPage, dir)
            ) {
                currentPage = getPageInVolumeWithOffset(currentPage, dir, false);
            } else {
                currentPage = null;
            }
        }
        return "";
    }

    private Optional<PageDetails> getPageDetails(PageId p) {
        DocumentSpaceData data = getViewForSpace(p.getSequenceId().getSpace());
        if (p.getPageIndex() < data.pageDetails.size()) {
            return Optional.ofNullable(data.pageDetails.get(p.getPageIndex()));
        } else {
            return Optional.empty();
        }
    }

    String findStartAndMarker(PageId id, MarkerReference ref) {
        return getPageDetails(id)
                .map(p -> {
                    PageDetails start;
                    if (ref.getSearchScope() == MarkerSearchScope.SPREAD ||
                            ref.getSearchScope() == MarkerSearchScope.SPREAD_CONTENT) {
                        start = getPageInVolumeWithOffset(p, ref.getOffset(), shouldAdjustOutOfBounds(p, ref));
                    } else {
                        //Keep while moving: start = p.getPageInScope(
                        //  p.getSequenceParent(),
                        //  f2.getOffset(),
                        //  shouldAdjustOutOfBounds(p, ref)
                        //);
                        start = p.getPageInScope(getContentsInSequence(
                            p.getSequenceId()),
                            ref.getOffset(),
                            shouldAdjustOutOfBounds(p, ref)
                        );
                    }
                    return findMarker(start, ref);
                })
                .orElse("");
    }

    boolean isDirty() {
        return dirty;
    }

    void setDirty(boolean value) {
        this.dirty = value;
    }
}
