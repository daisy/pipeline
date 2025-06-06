package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * TODO: Write java doc.
 */
public class CrossReferenceHandler {
    private final LookupHandler<String, Integer> pageRefs;
    private final LookupHandler<String, VolumeData> volumeRefs;
    private final LookupHandler<Integer, Iterable<AnchorData>> anchorRefs;
    private final LookupHandler<String, Integer> variables;
    private final LookupHandler<SheetIdentity, Boolean> breakable;
    private final LookupHandler<BlockAddress, Integer> rowCount;
    private final LookupHandler<BlockAddress, List<String>> groupAnchors;
    private final LookupHandler<BlockAddress, List<Marker>> groupMarkers;
    private final LookupHandler<BlockAddress, List<String>> groupIdentifiers;
    private final LookupHandler<BlockLineLocation, TransitionProperties> transitionProperties;
    private final Map<BlockLineLocation, PageDetails> nextPageDetails;
    private final LookupHandler<Integer, Overhead> volumeOverhead;
    private final Map<String, Integer> counters;
    private final SearchInfo searchInfo;
    private static final String VOLUMES_KEY = "volumes";
    private static final String SHEETS_IN_VOLUME = "sheets-in-volume-";
    private static final String SHEETS_IN_DOCUMENT = "sheets-in-document";
    private static final String PAGES_IN_VOLUME = "pages-in-volume-";
    private static final String PAGES_IN_DOCUMENT = "pages-in-document";
    private static final String PAGE_NUMBER_OF_FIRST_CONTENT_PAGE_OF_VOLUME =
            "page-number-of-first-content-page-of-volume-";
    private Set<String> pageIds;
    private boolean readOnly = false;
    private static final Overhead INITIAL_OVERHEAD = new Overhead(0, 0);

    public CrossReferenceHandler() {
        this.pageRefs = new LookupHandler<>();
        this.volumeRefs = new LookupHandler<>();
        this.anchorRefs = new LookupHandler<>();
        this.variables = new LookupHandler<>();
        this.breakable = new LookupHandler<>();
        this.rowCount = new LookupHandler<>();
        this.groupAnchors = new LookupHandler<>();
        this.groupMarkers = new LookupHandler<>();
        this.groupIdentifiers = new LookupHandler<>();
        this.transitionProperties = new LookupHandler<>();
        this.nextPageDetails = new HashMap<>();
        this.volumeOverhead = new LookupHandler<>();
        this.counters = new HashMap<>();
        this.searchInfo = new SearchInfo();
        this.pageIds = new HashSet<>();
    }

    public void setReadOnly() {
        readOnly = true;
    }

    public void setReadWrite() {
        readOnly = false;
    }

    /**
     * Gets the volume data for the specified identifier.
     *
     * @param refid the identifier to get the volume for
     * @return returns the volume number, one-based
     */
    public VolumeData getVolumeData(String refid) {
        return volumeRefs.get(refid, null, readOnly);
    }

    public void setVolumeData(String refid, VolumeData volumeData) {
        if (readOnly) {
            return;
        }
        volumeRefs.put(refid, volumeData);
    }

    /**
     * Gets the page number for the specified identifier.
     *
     * @param refid the identifier to get the page for
     * @return returns the page number, one-based
     */
    public Integer getPageNumber(String refid) {
        return pageRefs.get(refid, null, readOnly);
    }

    public void setPageNumber(String refid, int page) {
        if (readOnly) {
            return;
        }
        if (!pageIds.add(refid)) {
            throw new IllegalArgumentException("Identifier not unique: " + refid);
        }
        pageRefs.put(refid, page);
    }

    public Iterable<AnchorData> getAnchorData(int volume) {
        return anchorRefs.get(volume, null, readOnly);
    }

    public void setAnchorData(int volume, Iterable<AnchorData> data) {
        if (readOnly) {
            return;
        }
        anchorRefs.put(volume, data);
    }

    public void setVolumeCount(int volumes) {
        if (readOnly) {
            return;
        }
        variables.put(VOLUMES_KEY, volumes);
    }

    public void setSheetsInVolume(int volume, int value) {
        if (readOnly) {
            return;
        }
        variables.put(SHEETS_IN_VOLUME + volume, value);
    }

    public void setSheetsInDocument(int value) {
        if (readOnly) {
            return;
        }
        variables.put(SHEETS_IN_DOCUMENT, value);
    }

    public void setPageNumberOfFirstContentPageOfVolume(int volume, int value) {
        if (readOnly) {
            return;
        }
        variables.put(PAGE_NUMBER_OF_FIRST_CONTENT_PAGE_OF_VOLUME + volume, value);
    }

    public void keepBreakable(SheetIdentity ident, boolean value) {
        if (readOnly) {
            return;
        }
        breakable.keep(ident, value);
    }

    public void commitBreakable() {
        if (readOnly) {
            return;
        }
        breakable.commit();
    }

    public void keepTransitionProperties(BlockLineLocation id, TransitionProperties value) {
        if (readOnly) {
            return;
        }
        transitionProperties.keep(id, value);
    }

    public void commitTransitionProperties() {
        if (readOnly) {
            return;
        }
        transitionProperties.commit();
    }

    public void setRowCount(BlockAddress blockId, int value) {
        if (readOnly) {
            return;
        }
        // The row count is used by the page break algorithm in the next iteration to
        // determine/estimate how many widow lines a block will have. The actual row count can
        // differ however, for instance due to changes in hyphenation (hyphenation may be suppressed
        // on the last line of the page). This could lead to an endless alternation between two
        // renderings. This is why we're storing the *minimum* value. Note that it could result in
        // page breaks that happen one row too early because the row count was underestimated.
        Integer prevValue = rowCount.get(blockId, null, true);
        if (prevValue != null) {
            value = Math.min(prevValue, value);
        }
        rowCount.put(blockId, value);
    }

    public void trimPageDetails() {
        if (readOnly) {
            return;
        }
        //TODO: implement
    }

    public void setGroupAnchors(BlockAddress blockId, List<String> anchors) {
        if (readOnly) {
            return;
        }
        groupAnchors.put(blockId, anchors.isEmpty() ? Collections.emptyList() : new ArrayList<>(anchors));
    }

    public void setGroupMarkers(BlockAddress blockId, List<Marker> markers) {
        if (readOnly) {
            return;
        }
        groupMarkers.put(blockId, markers.isEmpty() ? Collections.emptyList() : new ArrayList<>(markers));
    }

    public void setGroupIdentifiers(BlockAddress blockId, List<String> identifiers) {
        if (readOnly) {
            return;
        }
        groupIdentifiers.put(blockId, identifiers.isEmpty() ? Collections.emptyList() : new ArrayList<>(identifiers));
    }

    public Overhead getOverhead(int volumeNumber) {
        if (volumeNumber < 1) {
            throw new IndexOutOfBoundsException("Volume must be greater than or equal to 1");
        }
        return volumeOverhead.get(volumeNumber, INITIAL_OVERHEAD, readOnly);
    }

    public void setOverhead(int volumeNumber, Overhead overhead) {
        if (readOnly) {
            return;
        }
        volumeOverhead.put(volumeNumber, overhead);
    }

    public Integer getPageNumberOffset(String key) {
        return counters.get(key);
    }

    public void setPageNumberOffset(String key, Integer value) {
        if (readOnly) {
            return;
        }
        counters.put(key, value);
    }

    /**
     * Gets the number of volumes.
     *
     * @return returns the number of volumes
     */
    public int getVolumeCount() {
        return variables.get(VOLUMES_KEY, 1, readOnly);
    }

    public int getSheetsInVolume(int volume) {
        return variables.get(SHEETS_IN_VOLUME + volume, 0, readOnly);
    }

    public int getSheetsInDocument() {
        return variables.get(SHEETS_IN_DOCUMENT, 0, readOnly);
    }

    public int getPagesInVolume(int volume) {
        return variables.get(PAGES_IN_VOLUME + volume, 0, readOnly);
    }

    public int getPageNumberOfFirstContentPageOfVolume(int volume) {
        return variables.get(PAGE_NUMBER_OF_FIRST_CONTENT_PAGE_OF_VOLUME + volume, 0, readOnly);
    }

    public int getPagesInDocument() {
        return variables.get(PAGES_IN_DOCUMENT, 0, readOnly);
    }

    public boolean getBreakable(SheetIdentity ident) {
        return breakable.get(ident, true, readOnly);
    }

    public Optional<TransitionProperties> getTransitionProperties(BlockLineLocation id) {
        return Optional.ofNullable(transitionProperties.get(id, null, readOnly));
    }

    public List<String> getGroupAnchors(BlockAddress blockId) {
        return groupAnchors.get(blockId, Collections.emptyList(), readOnly);
    }

    public List<Marker> getGroupMarkers(BlockAddress blockId) {
        return groupMarkers.get(blockId, Collections.emptyList(), readOnly);
    }

    public List<String> getGroupIdentifiers(BlockAddress blockId) {
        return groupIdentifiers.get(blockId, Collections.emptyList(), readOnly);
    }

    public int getRowCount(BlockAddress blockId) {
        return rowCount.get(blockId, Integer.MAX_VALUE, readOnly);
    }

    public void keepPageDetails(PageDetails value) {
        if (readOnly) {
            return;
        }
        searchInfo.keepPageDetails(value);
    }

    public void commitPageDetails() {
        if (readOnly) {
            return;
        }
        searchInfo.commitPageDetails();
    }

    /**
     * Sets the sequence scope for the purpose of finding markers in a specific sequence.
     *
     * @param sequenceId the sequence id
     * @param fromIndex  the start index
     * @param toIndex    the end index
     */
    public void setSequenceScope(SequenceId sequenceId, int fromIndex, int toIndex) {
        if (readOnly) {
            return;
        }
        searchInfo.setSequenceScope(sequenceId, fromIndex, toIndex);
    }

    /**
     * Sets the volume scope for the purpose of finding markers in a specific volume.
     *
     * @param volumeNumber the volume number
     * @param fromIndex    the start index
     * @param toIndex      the end index
     */
    public void setVolumeScope(int volumeNumber, int fromIndex, int toIndex) {
        if (readOnly) {
            return;
        }
        searchInfo.setVolumeScope(volumeNumber, fromIndex, toIndex);
    }

    /**
     * <p>Finds a marker value starting from the page with the supplied id.</p>
     * <p>To find markers, the following methods must be used to register
     * data needed by this method:</p>
     * <ul><li>{@link #keepPageDetails(PageDetails)}</li>
     * <li>{@link #commitPageDetails()}</li>
     * <li>{@link #setSequenceScope(SequenceId, int, int)}</li>
     * <li>{@link #setVolumeScope(int, int, int)}</li></ul>
     *
     * @param id   the page id of the page where the search originates.
     *             Note that this page is not necessarily the first page
     *             searched (depending on the value of
     *             {@link MarkerReference#getOffset()}).
     * @param spec the search specification
     * @return returns the marker value, or an empty string if not found
     */
    public String findMarker(PageId id, MarkerReference spec) {
        return searchInfo.findStartAndMarker(id, spec);
    }

    public Optional<BlockLineLocation> getNextPageLocationInSequence(BlockLineLocation id) {
        PageDetails details = nextPageDetails.get(id);
        return details != null ? Optional.of(details.getPageLocation()) : Optional.empty();
    }

    public void setNextPageDetailsInSequence(BlockLineLocation id, PageDetails details) {
        if (readOnly) {
            return;
        }
        Objects.requireNonNull(details);
        nextPageDetails.put(id, details);
    }

    /**
     * Indicate that a page is the last one in the sequence or that the next page is empty.
     *
     * @param id The BlockLineLocation that identifies the page.
     */
    public void setNextPageInSequenceEmptyOrAbsent(BlockLineLocation id) {
        nextPageDetails.remove(id);
    }

    /**
     * Returns true if some information has been changed since last use.
     *
     * @return true if some information has been changed, false otherwise
     */
    public boolean isDirty() {
        //TODO: fix dirty flag for anchors/markers
        return
            pageRefs.isDirty() ||
            volumeRefs.isDirty() ||
            anchorRefs.isDirty() ||
            variables.isDirty() ||
            breakable.isDirty() ||
            volumeOverhead.isDirty() ||
            searchInfo.isDirty() ||
            transitionProperties.isDirty();
        //|| groupAnchors.isDirty()
        //|| groupMarkers.isDirty() || rowCount.isDirty()
    }

    /**
     * Sets the dirty flag on all tracked data. This is typically used to reset
     * the value of the flag when rendering another pass. However, by setting this
     * value to true, it can be used to disable tracking for the remainder of
     * the rendering.
     *
     * @param value the value
     */
    public void setDirty(boolean value) {
        if (readOnly) {
            return;
        }
        pageRefs.setDirty(value);
        volumeRefs.setDirty(value);
        anchorRefs.setDirty(value);
        variables.setDirty(value);
        breakable.setDirty(value);
        searchInfo.setDirty(value);
        transitionProperties.setDirty(value);
        //TODO: fix dirty flag for anchors/markers
        //rowCount.setDirty(value);
        //groupAnchors.setDirty(value);
        //groupMarkers.setDirty(value);
        volumeOverhead.setDirty(value);
        counters.clear();
    }

    public void resetUniqueChecks() {
        if (readOnly) {
            return;
        }
        pageIds = new HashSet<>();
    }

}
