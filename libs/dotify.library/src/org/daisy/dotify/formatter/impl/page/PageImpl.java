package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.MarginRegion;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerIndicator;
import org.daisy.dotify.api.formatter.MarkerIndicatorRegion;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.formatter.impl.common.Page;
import org.daisy.dotify.formatter.impl.core.BorderManager;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.HeightCalculator;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PageTemplate;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.row.MarginProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: scope spread is currently implemented using document wide scope, i.e. across volume boundaries.
// This is wrong, but is better than the previous sequence scope.

/**
 * <p>Provides a {@link Page} object.</p>
 *
 * @author Joel Håkansson
 */
public class PageImpl implements Page {

    private static final Logger logger = Logger.getLogger(PageImpl.class.getCanonicalName());

    private final FieldResolver fieldResolver;
    private final PageDetails details;
    private final LayoutMaster master;
    private final FormatterContext fcontext;
    private final PageAreaContent pageAreaTemplate;
    private final List<RowImpl> pageArea;
    private final List<String> anchors;
    private final List<Marker> markersForNextMarkerIndicator;
    private final int flowHeight;
    private final PageTemplate template;
    private final BorderManager finalRows;

    private boolean hasRows;
    private boolean isVolBreakAllowed;
    private int keepPreviousSheets;
    private VolumeKeepPriority volumeBreakAfterPriority;
    private final BrailleTranslator filter;

    private int renderedHeaderRows;
    private int renderedFooterRows;
    private boolean topPageAreaProcessed;
    private boolean bottomPageAreaProcessed;

    public PageImpl(
        FieldResolver fieldResolver,
        PageDetails details,
        LayoutMaster master,
        FormatterContext fcontext,
        PageAreaContent pageAreaTemplate
    ) {
        this.fieldResolver = fieldResolver;
        this.details = details;
        this.master = master;
        this.fcontext = fcontext;
        this.pageAreaTemplate = pageAreaTemplate;

        this.pageArea = new ArrayList<>();
        this.anchors = new ArrayList<>();
        this.template = master.getTemplate(details.getPageNumber());
        this.flowHeight = master.getFlowHeight(template);
        this.isVolBreakAllowed = true;
        this.keepPreviousSheets = 0;
        this.volumeBreakAfterPriority = VolumeKeepPriority.empty();
        if (master.duplex() && details.getPageId().getOrdinal() % 2 == 1) {
            this.finalRows = new BorderManager(master, fcontext, master.getOuterMargin(), master.getInnerMargin());
        } else {
            this.finalRows = new BorderManager(master, fcontext, master.getInnerMargin(), master.getOuterMargin());
        }
        this.hasRows = false;
        this.filter = fcontext.getDefaultTranslator();
        this.renderedHeaderRows = 0;
        this.renderedFooterRows = 0;
        this.topPageAreaProcessed = false;
        this.bottomPageAreaProcessed = false;
        this.markersForNextMarkerIndicator = hasMarkerIndicatorRegion(template) ? new ArrayList<>() : null;
    }

    void addToPageArea(List<RowImpl> block) {
        if (hasRows) {
            throw new IllegalStateException("Page area must be added before adding rows.");
        }
        pageArea.addAll(block);
    }

    void newRow(RowImpl r) {
        boolean rowAdded = false;
        hasRows = true;
        while (renderedHeaderRows < template.getHeader().size()) {
            FieldList fields = template.getHeader().get(renderedHeaderRows);
            renderedHeaderRows++;
            if (fields.getFields().stream().anyMatch(v -> v instanceof NoField)) {
                // render content row combined with header fields
                RowImpl r2 = fieldResolver.renderField(getDetails(), fields, filter, Optional.of(r));
                finalRows.addRow(r2.shouldAdjustForMargin() ? addMarginRegion(r2) : r2);
                addRowDetails(r);
                rowAdded = true;
                break;
            } else {
                // render header fields
                finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
            }
        }
        if (!rowAdded && renderedHeaderRows >= template.getHeader().size()) {
            if (!topPageAreaProcessed) {
                addTopPageArea();
                getDetails().startsContentMarkers();
                // TODO: When called here, the only identifiers that will be considered to come
                // before text content on this page are the identifiers from skippable RowGroup
                // (read: empty blocks) that were discarded (in PageSequenceBuilder2). But
                // identifiers attached to the start of the first following RowGroup with text
                // content (and any RowGroup in between) should be part of this as well.
                getDetails().startsContentIdentifiers();
                topPageAreaProcessed = true;
            }
            if (hasBodyRowsLeft()) {
                // render content row
                finalRows.addRow(r.shouldAdjustForMargin() ? addMarginRegion(r) : r);
                rowAdded = true;
            } else {
                if (!bottomPageAreaProcessed) {
                    addBottomPageArea();
                    bottomPageAreaProcessed = true;
                }
                while (renderedFooterRows < template.getFooter().size()) {
                    FieldList fields = template.getFooter().get(renderedFooterRows);
                    renderedFooterRows++;
                    if (fields.getFields().stream().anyMatch(v -> v instanceof NoField)) {
                        // render content row combined with footer fields
                        RowImpl r2 = fieldResolver.renderField(getDetails(), fields, filter, Optional.of(r));
                        finalRows.addRow(r2.shouldAdjustForMargin() ? addMarginRegion(r2) : r2);
                        rowAdded = true;
                        break;
                    } else {
                        // render footer fields
                        finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
                    }
                }
            }
            addRowDetails(r);
        }
        if (rowAdded && markersForNextMarkerIndicator != null) {
            markersForNextMarkerIndicator.clear();
        }
    }

    @FunctionalInterface
    interface MarkerRef {
        boolean hasMarkerWithName(String name);
    }

    private RowImpl addMarginRegion(final RowImpl r) {
        RowImpl.Builder b = new RowImpl.Builder(r);
        MarkerRef rf = name -> {
            if (r.hasMarkerWithName(name)) {
                return true;
            }
            if (markersForNextMarkerIndicator != null) {
                for (Marker m : markersForNextMarkerIndicator) {
                    if (m.getName().equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        };
        MarginProperties margin = r.getLeftMargin();
        for (MarginRegion mr : template.getLeftMarginRegion()) {
            margin = getMarginRegionValue(mr, rf, false).append(margin);
        }
        b.leftMargin(margin);
        margin = r.getRightMargin();
        for (MarginRegion mr : template.getRightMarginRegion()) {
            margin = margin.append(getMarginRegionValue(mr, rf, true));
        }
        b.rightMargin(margin);
        return b.build();
    }

    private MarginProperties getMarginRegionValue(
        MarginRegion mr,
        MarkerRef r,
        boolean rightSide
    ) throws PaginatorException {
        int w = mr.getWidth();
        if (mr instanceof MarkerIndicatorRegion) {
            String ret = "";
            MarkerIndicator marker = firstMarkerForRow(r, (MarkerIndicatorRegion) mr);
            if (marker != null) {
                try {
                    String indicator = marker.getIndicator();
                    if (!fcontext.getConfiguration().isMarkingCapitalLetters()) {
                        indicator = indicator.toLowerCase();
                    }
                    Translatable.Builder translatable = Translatable.text(indicator);
                    if (marker.getTextStyle() != null) {
                        translatable = translatable.attributes(
                            new DefaultTextAttribute.Builder(marker.getTextStyle()).build(indicator.length()));
                    }
                    ret = fcontext.getDefaultTranslator().translate(translatable.build()).getTranslatedRemainder();
                } catch (TranslationException e) {
                    throw new PaginatorException("Failed to translate: " + ret, e);
                }
            }
            boolean spaceOnly = ret.length() == 0;
            if (ret.length() < w) {
                StringBuilder sb = new StringBuilder();
                if (rightSide) {
                    while (sb.length() < w - ret.length()) {
                        sb.append(fcontext.getSpaceCharacter());
                    }
                    sb.append(ret);
                } else {
                    sb.append(ret);
                    while (sb.length() < w) {
                        sb.append(fcontext.getSpaceCharacter());
                    }
                }
                ret = sb.toString();
            } else if (ret.length() > w) {
                if (fcontext.getConfiguration().isAllowingTextOverflowTrimming()) {
                    String trimmed = ret.substring(0, mr.getWidth());
                    logger.log(Level.WARNING, "Cannot fit \"" + ret + "\" into a margin-region of size " + mr.getWidth()
                               + ", trimming to \"" + trimmed + "\"");
                    ret = trimmed;
                } else {
                    throw new PaginatorException(
                        "Cannot fit \"" + ret + "\" into a margin-region of size " + mr.getWidth());
                }
            }
            return new MarginProperties(ret, spaceOnly);
        } else {
            throw new PaginatorException("Unsupported margin-region type: " + mr.getClass().getName());
        }
    }

    private MarkerIndicator firstMarkerForRow(MarkerRef r, MarkerIndicatorRegion mrr) {
        return mrr.getIndicators().stream()
                .filter(mi -> r.hasMarkerWithName(mi.getName()))
                .findFirst().orElse(null);
    }

    private void addRowDetails(RowImpl r) {
        getDetails().getMarkers().addAll(r.getMarkers());
        anchors.addAll(r.getAnchors());
        getDetails().getIdentifiers().addAll(r.getIdentifiers());
    }

    /**
     * Add markers from a {@link RowGroup}, that is, markers that are not attached to a {@link
     * RowImpl}. The markers are added to the {@link PageDetails} object, for resolving {@link
     * org.daisy.dotify.api.formatter.MarkerReferenceField} and {@link
     * org.daisy.dotify.formatter.impl.segment.MarkerReferenceSegment}, and they are also used for
     * resolving {@link MarkerIndicatorRegion} on the first following (content) row.
     */
    void addMarkers(RowGroup m) {
        getDetails().getMarkers().addAll(m.getMarkers());
        if (markersForNextMarkerIndicator != null) {
            markersForNextMarkerIndicator.addAll(m.getMarkers());
        }
    }

    public List<String> getAnchors() {
        return anchors;
    }

    /**
     * Add identifiers from a {@link RowGroup}, that is, identifiers that are not attached to a
     * {@link RowImpl}.
     */
    void addIdentifiers(RowGroup ids) {
        getDetails().getIdentifiers().addAll(ids.getIdentifiers());
    }

    public List<String> getIdentifiers() {
        return getDetails().getIdentifiers();
    }

    /**
     * Get identifiers for this page excluding identifiers before text content.
     *
     * @return Content identifiers excluding identifiers before text content.
     */
    public List<String> getContentIdentifiers() {
        return getDetails().getContentIdentifiers();
    }

    /**
     * Gets the page space needed to render the rows.
     *
     * @param rows       the rows to render
     * @param defSpacing a value &gt;= 1.0
     * @return returns the space, in rows
     */
    static float rowsNeeded(Collection<? extends Row> rows, float defSpacing) {
        HeightCalculator c = new HeightCalculator(defSpacing);
        c.addRows(rows);
        return c.getCurrentHeight();
    }

    // vertical position of the next body row, measured from the top edge of the page body area
    float currentPosition() {
        float pos = finalRows.getOffsetHeight();
        float headerHeight = template.getHeaderHeight();
        if (pos < headerHeight) {
            if (hasTopPageArea()) {
                return pageAreaSpaceNeeded();
            } else {
                return 0;
            }
        } else {
            return pos - headerHeight;
        }
    }

    // space available for body rows
    // - this excludes space used for page-area
    float spaceAvailableInFlow() {
        return getFlowHeight() + template.getHeaderHeight() - pageAreaSpaceNeeded() - finalRows.getOffsetHeight();
    }

    private float staticAreaSpaceNeeded() {
        return rowsNeeded(pageAreaTemplate.getBefore(), master.getRowSpacing()) +
                rowsNeeded(pageAreaTemplate.getAfter(), master.getRowSpacing());
    }

    float pageAreaSpaceNeeded() {
        return (!pageArea.isEmpty() ? staticAreaSpaceNeeded() + rowsNeeded(pageArea, master.getRowSpacing()) : 0);
    }

    private boolean hasTopPageArea() {
        return master.getPageArea() != null
                && master.getPageArea().getAlignment() == PageAreaProperties.Alignment.TOP
                && !pageArea.isEmpty();
    }

    private boolean hasBottomPageArea() {
        return master.getPageArea() != null
                && master.getPageArea().getAlignment() == PageAreaProperties.Alignment.BOTTOM
                && !pageArea.isEmpty();
    }

    private void addTopPageArea() {
        if (hasTopPageArea()) {
            finalRows.addAll(pageAreaTemplate.getBefore());
            finalRows.addAll(pageArea);
            finalRows.addAll(pageAreaTemplate.getAfter());
        }
    }

    private void addBottomPageArea() {
        if (hasBottomPageArea()) {
            finalRows.addAll(pageAreaTemplate.getBefore());
            finalRows.addAll(pageArea);
            finalRows.addAll(pageAreaTemplate.getAfter());
        }
    }

    private boolean addHeaderIfNotAdded() {
        if (!hasRows) { // the header hasn't been added yet
            //add the header
            for (FieldList fields : template.getHeader()) {
                finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
            }
            //add top page area
            addTopPageArea();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds the footer area if not already added. Note that this area also
     * contains the page area if aligned to be the bottom.
     */
    private void addBottomPageAreaAndFooterIfNotAdded() {
        if (
            !template.getFooter().isEmpty() ||
            finalRows.hasBorder() ||
            (
                master.getPageArea() != null &&
                master.getPageArea().getAlignment() == PageAreaProperties.Alignment.BOTTOM &&
                !pageArea.isEmpty()
            )
        ) {
            while (hasBodyRowsLeft()) {
                finalRows.addRow(new RowImpl());
            }
            if (!bottomPageAreaProcessed) {
                addBottomPageArea();
                bottomPageAreaProcessed = true;
            }
            while (renderedFooterRows < template.getFooter().size()) {
                FieldList fields = template.getFooter().get(renderedFooterRows);
                renderedFooterRows++;
                finalRows.addRow(fieldResolver.renderField(getDetails(), fields, filter, Optional.empty()));
            }
        }
    }

    private boolean hasBodyRowsLeft() {
        float headerHeight = template.getHeaderHeight();
        float areaSize = (
            master.getPageArea() != null &&
            master.getPageArea().getAlignment() == PageAreaProperties.Alignment.BOTTOM ? pageAreaSpaceNeeded() : 0
        );
        return Math.ceil(finalRows.getOffsetHeight() + areaSize) <
            getFlowHeight() + headerHeight - template.validateAndAnalyzeHeader() - template.validateAndAnalyzeFooter();
    }

    /*
     * The assumption is made that by now all pages have been added to the parent sequence and volume scopes
     * have been set on the page struct.
     */
    @Override
    public List<Row> getRows() {
        try {
            if (!finalRows.isClosed()) {
                addHeaderIfNotAdded();
                addBottomPageAreaAndFooterIfNotAdded();
            }
            return finalRows.getRows();
        } catch (PaginatorException e) {
            throw new RuntimeException("Pagination failed.", e);
        }
    }


    /**
     * Get the page number, one based.
     *
     * @return returns the page number
     */
    public int getPageNumber() {
        return details.getPageNumber();
    }

    /**
     * Gets the flow height for this page, i.e. the number of rows available for text flow.
     *
     * @return returns the flow height
     */
    int getFlowHeight() {
        return flowHeight;
    }

    void setKeepWithPreviousSheets(int value) {
        keepPreviousSheets = Math.max(value, keepPreviousSheets);
    }

    void setAllowsVolumeBreak(boolean value) {
        this.isVolBreakAllowed = value;
    }

    public boolean allowsVolumeBreak() {
        return isVolBreakAllowed;
    }

    public int keepPreviousSheets() {
        return keepPreviousSheets;
    }

    PageTemplate getPageTemplate() {
        return template;
    }

    public VolumeKeepPriority getAvoidVolumeBreakAfter() {
        return volumeBreakAfterPriority;
    }

    void setAvoidVolumeBreakAfter(VolumeKeepPriority value) {
        this.volumeBreakAfterPriority = value;
    }

    public PageDetails getDetails() {
        return details;
    }

    boolean hasRows() {
        return hasRows;
    }
    
    private static boolean hasMarkerIndicatorRegion(PageTemplate template) {
        for (MarginRegion mr : template.getLeftMarginRegion()) {
            if (mr instanceof MarkerIndicatorRegion) {
                return true;
            }
        }
        return false;
    }
}
