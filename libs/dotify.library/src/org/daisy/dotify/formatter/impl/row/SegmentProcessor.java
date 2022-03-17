package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerReference;
import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.DefaultAttributeWithContext;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.text.StringTools;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.AnchorSegment;
import org.daisy.dotify.formatter.impl.segment.Evaluate;
import org.daisy.dotify.formatter.impl.segment.ExternalReferenceSegment;
import org.daisy.dotify.formatter.impl.segment.IdentifierSegment;
import org.daisy.dotify.formatter.impl.segment.LeaderSegment;
import org.daisy.dotify.formatter.impl.segment.MarkerReferenceSegment;
import org.daisy.dotify.formatter.impl.segment.MarkerSegment;
import org.daisy.dotify.formatter.impl.segment.PageNumberReference;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Segment.SegmentType;
import org.daisy.dotify.formatter.impl.segment.Style;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Write java doc.
 *
 * @author Joel Håkansson
 */
class SegmentProcessor {

    private final List<Segment> segments;
    private final CrossReferenceHandler refs;
    private final AttributeWithContext attr;

    private DefaultContext context;
    private final boolean hasSignificantContent;
    /*
     * Whether some segments are PageNumberReference or Evaluate
     */
    private final boolean hasDynamicContent;
    private final SegmentProcessorContext processorContext;

    private int segmentIndex;
    /**
     * The active (not completed) row, as a mutable object (builder).
     *
     * Is only non-null if the previous getNext() call returned an empty result, or if the next
     * result is being created in getNext().
     */
    private RowImpl.Builder currentRow;
    /*
     * Markers, anchors and identifiers that are not associated with a particular row.
     */
    private final List<Marker> groupMarkers;
    private final List<String> groupAnchors;
    private final List<String> groupIdentifiers;
    private Object externalReference = null;
    /*
     * Buffers a number of evaluated segments that follow a leader (contained in `leaderManager') in
     * a single aggregated BrailleTranslatorResult. Can only be non-null if `current' is null.
     */
    private AggregatedBrailleTranslatorResult.Builder layoutOrApplyAfterLeader;
    /*
     * Contains zero, one of two leader segments. If not empty, the first leader comes before
     * `layoutOrApplyAfterLeader' or `current'. If there is a second leader it comes after `current'
     * (`layoutOrApplyAfterLeader' is null).
     */
    private final LeaderManager leaderManager;
    private ListItem item;
    private int forceCount;
    /**
     * The number of unused columns to the left of the text block. Both margins and borders counts
     * as unused columns in this case. For left adjusted text, this value is typically equal to the
     * left margin.
     */
    private int unusedLeft;
    /**
     * The number of unused columns to the right of the text block. Both margins and borders counts
     * as unused columns in this case.
     */
    private int unusedRight;
    /**
     * True if at least one row has been finalized (flushed).
     */
    private boolean rowsFlushed;
    /*
     * Buffers a number of evaluated segments in a CurrentResult. Produces the actual rows that this
     * SegmentProcessor returns. Can only be non-null if `layoutOrApplyAfterLeader' is null.
     */
    private CurrentResult current;
    private boolean closed;
    private String blockId;
    private Function<PageNumberReference, String> pagenumResolver;
    private Function<Iterable<? extends MarkerReference>, String> markerRefResolver;
    private Function<Evaluate, String> expressionResolver;

    /**
     * @param available The total space available in rows for left margin, content, left and right
     *                  text indent, and reserved cells.
     */
    SegmentProcessor(
        String blockId,
        List<Segment> segments,
        int flowWidth,
        CrossReferenceHandler refs,
        DefaultContext context,
        int available,
        BlockMargin margins,
        FormatterCoreContext fcontext,
        RowDataProperties rdp
    ) {
        if (refs == null) {
            throw new IllegalArgumentException("must specify CrossReferenceHandler");
        }
        this.refs = refs;
        this.segments = Collections.unmodifiableList(removeStyles(segments).collect(Collectors.toList()));
        this.attr = buildAttributeWithContext(null, segments);
        this.context = context;
        this.groupMarkers = new ArrayList<>();
        this.groupAnchors = new ArrayList<>();
        this.groupIdentifiers = new ArrayList<>();
        this.externalReference = null;
        this.leaderManager = new LeaderManager();
        this.hasSignificantContent = calculateSignificantContent(this.segments, context, rdp);
        this.hasDynamicContent = this.segments.stream().anyMatch(
            s -> s.getSegmentType() == SegmentType.Evaluate ||
                 s.getSegmentType() == SegmentType.PageReference);
        this.processorContext = new SegmentProcessorContext(fcontext, rdp, margins, flowWidth, available);
        this.blockId = blockId;
        this.pagenumResolver = (rs) -> {
            Integer page = refs.getPageNumber(rs.getRefId());
            if (page == null) {
                return "??";
            } else {
                return "" + rs.getNumeralStyle().format(page);
            }
        };
        this.markerRefResolver = (ref) -> {
            for (MarkerReference r : ref) {
                String marker = refs.findMarker(getContext().getCurrentPageId(), r);
                if (!"".equals(marker)) {
                    return marker;
                }
            }
            return "";
        };
        this.expressionResolver = (e) -> e.getExpression().render(getContext());
        initFields();
    }

    SegmentProcessor(SegmentProcessor template) {
        // Refs is mutable, but for now we assume that the same context should be used.
        this.refs = template.refs;
        // Context is mutable, but for now we assume that the same context should be used.
        this.context = template.context;
        this.processorContext = template.processorContext;
        this.currentRow = template.currentRow == null ? null : new RowImpl.Builder(template.currentRow);
        this.groupAnchors = new ArrayList<>(template.groupAnchors);
        this.groupMarkers = new ArrayList<>(template.groupMarkers);
        this.groupIdentifiers = new ArrayList<>(template.groupIdentifiers);
        this.leaderManager = new LeaderManager(template.leaderManager);
        this.externalReference = template.externalReference;
        this.layoutOrApplyAfterLeader =
                template.layoutOrApplyAfterLeader == null ?
                null :
                new AggregatedBrailleTranslatorResult.Builder(template.layoutOrApplyAfterLeader);
        this.item = template.item;
        this.forceCount = template.forceCount;
        this.unusedLeft = template.unusedLeft;
        this.unusedRight = template.unusedRight;
        this.rowsFlushed = template.rowsFlushed;
        this.segments = template.segments;
        this.attr = template.attr;
        this.segmentIndex = template.segmentIndex;
        this.current = template.current != null ? copy(template.current) : null;
        this.closed = template.closed;
        this.hasSignificantContent = template.hasSignificantContent;
        this.hasDynamicContent = template.hasDynamicContent;
        this.blockId = template.blockId;
        this.pagenumResolver = template.pagenumResolver;
        // can't simply copy because getContext() of template would be used
        this.markerRefResolver = (ref) -> {
            for (MarkerReference r : ref) {
                String marker = refs.findMarker(getContext().getCurrentPageId(), r);
                if (!"".equals(marker)) {
                    return marker;
                }
            }
            return "";
        };
        this.expressionResolver = (e) -> e.getExpression().render(getContext());
    }

    /**
     * Filters the input list to remove styles (if present). Segments inside styles are inserted
     * at the current location in the list.
     *
     * @param segments segments containing styles
     * @return a stream of segments without styles
     */
    private static Stream<Segment> removeStyles(List<Segment> segments) {
        return segments.stream()
                .flatMap(
                    v -> v.getSegmentType() == SegmentType.Style ?
                        removeStyles(((Style) v).getSegments()) :
                        Stream.of(v)
                );
    }

    /**
     * <p>Builds a text attribute for the input segments with the specified name.
     * Style segments will be mapped into named text attributes which can
     * be passed to a marker processor or braille translator and thus be
     * used for applying the styling.</p>
     *
     * <p>In this process, the boundaries of styles will be modified in order to
     * ensure that markers adhere to the text in a line breaking
     * situation.</p>
     *
     * @param name the name of this text attribute (may be null)
     * @param in   the segments
     * @return a text attribute
     */
    private static DefaultAttributeWithContext buildAttributeWithContext(String name, List<Segment> in) {
        DefaultAttributeWithContext.Builder b;
        // Trim style scope
        int start = -1;
        int end = -1;
        boolean trimStart = false;
        boolean trimEnd = false;
        int i = 0;
        if (name != null) {
            // Scan segments for style scope
            for (Segment v : in) {
                // If significant content is encountered, set start and end of trim zone.
                if (
                    v.getSegmentType() == SegmentType.Text ||
                    v.getSegmentType() == SegmentType.Evaluate ||
                    v.getSegmentType() == SegmentType.PageReference ||
                    v.getSegmentType() == SegmentType.MarkerReference ||
                    v.getSegmentType() == SegmentType.Style
                ) {
                    if (start < 0) {
                        start = i;
                        end = i;
                    } else {
                        end = i;
                    }
                }
                i++;
            }
            trimStart = start > 0 && in.size() > 1;
            trimEnd = end < in.size() - 1 && in.size() > 1;
            if (trimStart) {
                b = new DefaultAttributeWithContext.Builder();
            } else {
                b = new DefaultAttributeWithContext.Builder(name);
            }
        } else {
            b = new DefaultAttributeWithContext.Builder();
        }
        i = 0;
        int sw = 0;
        int w = 0;
        for (Segment v : in) {
            if (trimStart && i == start) {
                DefaultAttributeWithContext.Builder c = b;
                b = new DefaultAttributeWithContext.Builder(name);
                b.add(c.build(sw));
                sw = 0;
            }
            if (v.getSegmentType() == SegmentType.Style) {
                Style s = ((Style) v);
                DefaultAttributeWithContext a = buildAttributeWithContext(s.getName(), s.getSegments());
                b.add(a);
                w += a.getWidth();
                sw += a.getWidth();
            } else {
                DefaultAttributeWithContext a = new DefaultAttributeWithContext.Builder().build(1);
                b.add(a);
                w++;
                sw++;
            }
            if (trimEnd && i == end) {
                DefaultAttributeWithContext.Builder c = b;
                b = new DefaultAttributeWithContext.Builder();
                b.add(c.build(sw));
                sw = 0;
            }
            i++;
        }
        return b.build(w);
    }

    private static boolean calculateSignificantContent(
        Iterable<Segment> segments,
        Context context,
        RowDataProperties rdp
    ) {
        for (Segment s : segments) {
            switch (s.getSegmentType()) {
                case Marker:
                case Anchor:
                case Identifier:
                    // continue
                    break;
                case Evaluate:
                    if (!((Evaluate) s).getExpression().render(context).isEmpty()) {
                        return true;
                    }
                    break;
                case Text:
                    if (!((TextSegment) s).getText().isEmpty()) {
                        return true;
                    }
                    break;
                case Style:
                    if (!calculateSignificantContent(((Style) s).getSegments(), context, rdp)) {
                        break;
                    }
                    return true;
                case NewLine:
                case Leader:
                case PageReference:
                case MarkerReference:
                default:
                    return true;
            }
        }
        return rdp.getUnderlineStyle() != null;
    }

    private void initFields() {
        segmentIndex = 0;
        currentRow = null;
        leaderManager.discardAllLeaders();
        layoutOrApplyAfterLeader = null;
        item = processorContext.getRowDataProps().getListItem();
        unusedLeft = processorContext.getFlowWidth();
        unusedRight = processorContext.getFlowWidth();
        rowsFlushed = false;
        current = null;
        closed = false;
        if (blockId != null && !"".equals(blockId)) {
            groupIdentifiers.add(blockId);
        }
        // reset resolved values and (re)set resolvers
        for (Segment s : segments) {
            switch (s.getSegmentType()) {
                case PageReference:
                    PageNumberReference prs = (PageNumberReference) s;
                    prs.setResolver(pagenumResolver);
                    break;
                case MarkerReference:
                    MarkerReferenceSegment mrs = (MarkerReferenceSegment) s;
                    mrs.setResolver(markerRefResolver);
                    break;
                case Evaluate:
                    Evaluate e = (Evaluate) s;
                    e.setResolver(expressionResolver);
                    break;
                default:
            }
        }
        // produce group markers and anchors
        getNext(false, LineProperties.DEFAULT);
    }

    private boolean couldTriggerNewRow() {
        if (!hasMoreSegments()) {
            //There's a lot of conditions to keep track of here, but hopefully we can simplify later on

            return
                !closed &&
                (
                    currentRow != null ||
                    (rowsFlushed && processorContext.getRowDataProps().getUnderlineStyle() != null) ||
                    leaderManager.hasLeader()
                );
        }

        return couldTriggerNewRow(segmentIndex);
    }

    private boolean couldTriggerNewRow(int index) {
        Segment s = segments.get(index);
        switch (s.getSegmentType()) {
            case Marker:
            case Anchor:
            case Identifier:
                return false;
            case Evaluate:
                return !((Evaluate) s).getExpression().render(context).isEmpty();
            case Text:
                if (((TextSegment) s).getText().isEmpty()) {
                    TextSegment ts = (TextSegment) s;
                    if (canMergeWithSegmentAtIndex(ts, index + 1)) {
                        return couldTriggerNewRow(index + 1);
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            default:
                return true;
        }
    }

    private boolean canMergeWithSegmentAtIndex(TextSegment ts, int index) {
        return    // There's a next segment
            index < segments.size()
            // and that segment is a text segment
            && segments.get(index).getSegmentType() == SegmentType.Text
            // and it has the same properties
            && ((TextSegment) segments.get(index)).getTextProperties().equals(ts.getTextProperties());
    }

    boolean hasMoreData() {
        return
            hasMoreSegments() ||
            !closed ||
            (current != null && current.hasNext());
    }

    private boolean hasMoreSegments() {
        return segmentIndex < segments.size();
    }

    /*
     * Create value for `current' if it is null.
     */
    void prepareNext() {
        if (!hasMoreData()) {
            throw new IllegalStateException();
        }
        if (current == null) {
            if (!hasMoreSegments() && !closed) {
                closed = true;
                current = new CloseResult(layoutLeader());
            } else {
                current = loadNextSegment().orElse(null);
            }
        }
    }

    boolean hasNext() {
        return current != null && current.hasNext();
    }

    public boolean hasSignificantContent() {
        return hasSignificantContent;
    }

    /*
     * If the previous getNext() call returned an empty result, the lineProps argument is expected
     * to be the same as the lineProps argument that was called the previous time.
     */
    Optional<RowImpl> getNext(LineProperties lineProps) {
        return getNext(true, lineProps);
    }

    private Optional<RowImpl> getNext(boolean produceRow, LineProperties lineProps) {
        while (true) {
            if (current != null && current.hasNext()) {
                try {
                    Optional<RowImpl> ret = current.process(lineProps);
                    if (ret.isPresent()) {
                        if (!produceRow) {
                            // there is a test below that verifies that the current segment cannot produce a row result
                            // and the segment was processed under this assumption. If a row has been produced anyway,
                            // that's an error in the code.
                            throw new RuntimeException("Error in code");
                        }
                        return ret;
                    } // else try the next segment.
                } finally {
                    if (!current.hasNext()) {
                        current = null;
                    }
                }
            } else if (hasMoreData()) {
                if (!produceRow && couldTriggerNewRow()) {
                    return Optional.empty();
                }
                prepareNext();
            } else {
                return Optional.empty();
            }
        }
    }

    /*
     * Get the next segment or segments and do one of the following things:
     * - buffer them in `layoutOrApplyAfterLeader'
     * - wrap them (text segments) in a CurrentResult
     * - apply it (anchor, marker, identifier or external-reference segment) to the active row or the block
     * - combine it (newline segment) with previously buffered content in a CurrentResult
     * - add it (leader segment) to `leaderManager' and return previously buffered content as a CurrentResult
     */
    private Optional<CurrentResult> loadNextSegment() {
        Segment s = segments.get(segmentIndex);
        segmentIndex++;
        switch (s.getSegmentType()) {
            case NewLine:
                //flush
                return Optional.of(new NewLineResult(layoutLeader()));
            case Text:
                int len = 1;
                int fromIndex = segmentIndex - 1;
                TextSegment ts = (TextSegment) s;
                while (canMergeWithSegmentAtIndex(ts, segmentIndex)) {
                    len++;
                    segmentIndex++;
                }
                return layoutTextSegment(ts, fromIndex, len);
            case Leader:
                return layoutLeaderSegment((LeaderSegment) s);
            case PageReference:
                return layoutPageSegment((PageNumberReference) s);
            case MarkerReference:
                return layoutMarkerRefSegment((MarkerReferenceSegment) s);
            case Evaluate:
                return layoutEvaluate((Evaluate) s);
            case Marker:
                applyAfterLeader((MarkerSegment) s);
                return Optional.empty();
            case Anchor:
                applyAfterLeader((AnchorSegment) s);
                return Optional.empty();
            case Identifier:
                applyAfterLeader((IdentifierSegment) s);
                return Optional.empty();
            case ExternalReference:
                externalReference = ((ExternalReferenceSegment) s).getExternalReference();
                return Optional.empty();
            default:
                return Optional.empty();
        }
    }

    /**
     * Flushes the active row and returns it.
     *
     * @return returns the completed row
     */
    private RowImpl flushCurrentRow() {
        if (!rowsFlushed) {
            // Clear group anchors and markers (since we have content, we don't need them)
            currentRow.addAnchors(0, groupAnchors);
            groupAnchors.clear();
            currentRow.addMarkers(0, groupMarkers);
            groupMarkers.clear();
            currentRow.addIdentifiers(0, groupIdentifiers);
            groupIdentifiers.clear();
            currentRow.addExternalReference(externalReference);
            externalReference = null;
        }
        RowImpl r = currentRow.build();
        rowsFlushed = true;
        //Make calculations for underlining
        int width = r.getChars().length();
        int left = r.getLeftMargin().getContent().length();
        int right = r.getRightMargin().getContent().length();
        int space = processorContext.getFlowWidth() - width - left - right;
        left += r.getAlignment().getOffset(space);
        right = processorContext.getFlowWidth() - width - left;
        unusedLeft = Math.min(unusedLeft, left);
        unusedRight = Math.min(unusedRight, right);
        currentRow = null;
        return r;
    }

    /*
     * Translate the `length' text segments starting at `fromIndex' and either wrap the result in a
     * CurrentResult or buffer it in `layoutOrApplyAfterLeader'.
     */
    private Optional<CurrentResult> layoutTextSegment(TextSegment ts, int fromIndex, int length) {
        String mode = ts.getTextProperties().getTranslationMode();
        BrailleTranslatorResult btr = null;
        if (!ts.canMakeResult()) {
            int toIndex = fromIndex + length;
            TranslatableWithContext spec = TranslatableWithContext.from(segments, fromIndex, toIndex)
                    .attributes(attr)
                    .build();
            btr = toResult(spec, mode);
            // Do not cache the braille translation of this text segment if there are preceding or
            // following segments whose values can change, because this may influence the
            // translation of the text segment.
            if (!hasDynamicContent) {
                ts.storeResult(btr);
            }
        } else {
            btr = ts.newResult();
        }
        if (leaderManager.hasLeader()) {
            layoutAfterLeader(btr);
        } else {
            CurrentResult current = new CurrentResultImpl(btr, mode);
            return Optional.of(current);
        }
        return Optional.empty();
    }

    /*
     * Add the leader segment to leader manager, and if a leader was already present, convert
     * `layoutOrApplyAfterLeader' to a CurrentResult and return it.
     */
    private Optional<CurrentResult> layoutLeaderSegment(LeaderSegment ls) {
        try {
            if (leaderManager.hasLeader()) {
                return layoutLeader();
            }
            return Optional.empty();
        } finally {
            // translate pattern
            String pattern = ls.getPattern();
            try {
                pattern = processorContext
                    .getFormatterContext()
                    .getTranslator(ls.getTextProperties().getTranslationMode())
                    .translate(Translatable.text(pattern).build()).getTranslatedRemainder();
            } catch (TranslationException e) {
                throw new RuntimeException(e);
            }
            // add to leader manager
            leaderManager.addLeader(
                new Leader.Builder()
                          .pattern(pattern)
                          .position(ls.getPosition())
                          .align(ls.getAlignment())
                          .build());
        }
    }

    private Optional<CurrentResult> layoutPageSegment(PageNumberReference rs) {
        // This is done to reset the state of the PageNumberReference, i.e. to "unfreeze" its
        // value. It is safe to do this because by the time we get here the translator has not had
        // the chance to resolve() the segment yet (the segment has only been available as a
        // FollowingText). Still, it would be nicer if deep copies would be made of the segments
        // when needed, or if the segments would be completely stateless.
        rs.setResolver(pagenumResolver);
        //TODO: translate references using custom language?
        TranslatableWithContext spec;
        spec = TranslatableWithContext.from(segments, segmentIndex - 1)
                .attributes(attr)
                .build();
        if (leaderManager.hasLeader()) {
            layoutAfterLeader(spec, null);
        } else {
            String mode = null; // use default translator to translate list label
            BrailleTranslatorResult btr = toResult(spec, null);
            CurrentResult current = new CurrentResultImpl(btr, mode);
            return Optional.of(current);
        }
        return Optional.empty();
    }

    private Optional<CurrentResult> layoutMarkerRefSegment(MarkerReferenceSegment rs) {
        // This is done to reset the state of the MarkerReference, i.e. to "unfreeze" its
        // value. It is safe to do this because by the time we get here the translator has not had
        // the chance to resolve() the segment yet (the segment has only been available as a
        // FollowingText). Still, it would be nicer if deep copies would be made of the segments
        // when needed, or if the segments would be completely stateless.
        rs.setResolver(markerRefResolver);
        if (!rs.peek().isEmpty()) { // Don't create a new row if the evaluated reference is empty
            TranslatableWithContext spec;
            spec = TranslatableWithContext.from(segments, segmentIndex - 1)
                    .attributes(attr)
                    .build();
            if (leaderManager.hasLeader()) {
                layoutAfterLeader(spec, null);
            } else {
                String mode = null; // use default translator to translate list label
                BrailleTranslatorResult btr = toResult(spec, null);
                if (btr.hasNext()) { // Don't create a new row if the evaluated reference is empty
                                     // after applying the style
                    CurrentResult current = new CurrentResultImpl(btr, mode);
                    return Optional.of(current);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<CurrentResult> layoutEvaluate(Evaluate e) {
        // This is done to reset the state of the Evaluate, i.e. to "unfreeze" its value. It is safe
        // to do this because by the time we get here the translator has not had the chance to
        // resolve() the segment yet (the segment has only been available as a FollowingText).
        // Still, it would be nicer if deep copies would be made of the segments when needed, or if
        // the segments would be completely stateless.
        e.setResolver(expressionResolver);
        if (!e.peek().isEmpty()) { // Don't create a new row if the evaluated expression is empty
            // Note: this could be handled more generally (also for regular text) in layout().
            TranslatableWithContext spec = TranslatableWithContext.from(segments, segmentIndex - 1)
                    .attributes(attr)
                    .build();
            if (leaderManager.hasLeader()) {
                layoutAfterLeader(spec, null);
            } else {
                String mode = null; // use default translator to translate list label
                BrailleTranslatorResult btr = toResult(spec, mode);
                CurrentResult current = new CurrentResultImpl(btr, mode);
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    private void layoutAfterLeader(TranslatableWithContext spec, String mode) {
        layoutAfterLeader(toResult(spec, mode));
    }

    /*
     * Buffer an evaluated segment (BrailleTranslatorResult) in `layoutOrApplyAfterLeader'.
     */
    private void layoutAfterLeader(BrailleTranslatorResult result) {
        if (leaderManager.hasLeader()) {
            if (layoutOrApplyAfterLeader == null) {
                layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
            }
            layoutOrApplyAfterLeader.add(result);
        } else {
            // layoutAfterLeader() should only be called after having checked that there is a
            // leader.
            throw new RuntimeException("Error in code.");
        }
    }

    /*
     * Buffer marker segment in `layoutOrApplyAfterLeader', or if no leader is pending apply it to
     * the active row or the block.
     */
    private void applyAfterLeader(MarkerSegment marker) {
        if (leaderManager.hasLeader()) {
            if (layoutOrApplyAfterLeader == null) {
                layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
            }
            layoutOrApplyAfterLeader.add(marker);
        } else {
            if (currentRow == null) {
                groupMarkers.add(marker);
            } else {
                currentRow.addMarker(marker);
            }
        }
    }

    /*
     * Buffer anchor segment in `layoutOrApplyAfterLeader', or if no leader is pending apply it to
     * the active row or the block.
     */
    private void applyAfterLeader(final AnchorSegment anchor) {
        if (leaderManager.hasLeader()) {
            if (layoutOrApplyAfterLeader == null) {
                layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
            }
            layoutOrApplyAfterLeader.add(anchor);
        } else {
            if (currentRow == null) {
                groupAnchors.add(anchor.getReferenceID());
            } else {
                currentRow.addAnchor(anchor.getReferenceID());
            }
        }
    }

    /*
     * Buffer identifier segment in `layoutOrApplyAfterLeader', or if no leader is pending apply it
     * to the active row or the block.
     */
    private void applyAfterLeader(final IdentifierSegment identifier) {
        if (leaderManager.hasLeader()) {
            if (layoutOrApplyAfterLeader == null) {
                layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
            }
            layoutOrApplyAfterLeader.add(identifier);
        } else {
            if (currentRow == null) {
                groupIdentifiers.add(identifier.getName());
            } else {
                currentRow.addIdentifier(identifier.getName());
            }
        }
    }

    /*
     * If a leader segment is pending (leader manager is not empty), convert
     * `layoutOrApplyAfterLeader' to a CurrentResult and return it.
     */
    private Optional<CurrentResult> layoutLeader() {
        if (leaderManager.hasLeader()) {
            BrailleTranslatorResult btr;
            String mode = null; // use default translator to translate list label
            if (layoutOrApplyAfterLeader == null) {
                btr = toResult("");
            } else {
                btr = layoutOrApplyAfterLeader.build();
                layoutOrApplyAfterLeader = null;
            }
            CurrentResult current = new CurrentResultImpl(btr, mode);
            return Optional.of(current);
        }
        return Optional.empty();
    }

    private BrailleTranslatorResult toResult(String c) {
        return toResult(
            Translatable.text(
                processorContext.getFormatterContext().getConfiguration().isMarkingCapitalLetters() ?
                c :
                c.toLowerCase()
            ).build(),
            null
        );
    }

    private BrailleTranslatorResult toResult(Translatable spec, String mode) {
        try {
            return processorContext.getFormatterContext().getTranslator(mode).translate(spec);
        } catch (TranslationException e) {
            throw new RuntimeException(e);
        }
    }

    private BrailleTranslatorResult toResult(TranslatableWithContext spec, String mode) {
        try {
            return processorContext.getFormatterContext().getTranslator(mode).translate(spec);
        } catch (TranslationException e) {
            throw new RuntimeException(e);
        }
    }

    void reset() {
        groupAnchors.clear();
        groupMarkers.clear();
        groupIdentifiers.clear();
        externalReference = null;
        initFields();
    }

    /**
     * Get the markers that are not associated with a particular row.
     */
    List<Marker> getGroupMarkers() {
        return groupMarkers;
    }

    /**
     * Get the anchors that are not associated with a particular row.
     */
    List<String> getGroupAnchors() {
        return groupAnchors;
    }

    /**
     * Get the identifiers that are not associated with a particular row.
     */
    List<String> getGroupIdentifiers() {
        return groupIdentifiers;
    }

    void setContext(DefaultContext context) {
        this.context = context;
    }

    private DefaultContext getContext() {
        return context;
    }

    int getForceCount() {
        return forceCount;
    }

    /**
     * Returns true if there is an active (not completed) row in the
     * processor.
     *
     * @return true if there is an active row
     */
    private boolean hasCurrentRow() {
        return currentRow != null;
    }

    /**
     * Starts a new active row. Note that if {@link #hasCurrentRow()} returns
     * true, {@link #flushCurrentRow()} should be called first.
     *
     * @param left  the left margin
     * @param right the right margin
     */
    private void newCurrentRow(MarginProperties left, MarginProperties right) {
        currentRow = processorContext.getRowDataProps().configureNewEmptyRowBuilder(left, right);
    }

    /**
     * Adds to the running force count value.
     *
     * @param value the value to add
     */
    private void addToForceCount(double value) {
        forceCount += value;
    }

    /**
     * Returns true if the block processor has a pending list item.
     *
     * @return true if the block processor has a pending list item, false otherwise
     */
    private boolean hasListItem() {
        return item != null;
    }

    /**
     * Discards the pending list item.
     */
    private void discardListItem() {
        item = null;
    }

    /**
     * Gets the pending list item, or null if {@link #hasListItem()} returns false.
     *
     * @return returns the pending list item, or null
     */
    private ListItem getListItem() {
        Objects.requireNonNull(item);
        return item;
    }

    /* ========================= */
    /*       Inner classes       */
    /* ========================= */

    private interface CurrentResult {
        boolean hasNext();
        Optional<RowImpl> process(LineProperties lineProps);
    }

    private CurrentResult copy(CurrentResult cr) {
        return cr instanceof CurrentResultImpl
            ? new CurrentResultImpl((CurrentResultImpl) cr)
            : cr instanceof NewLineResult
                ? new NewLineResult((NewLineResult) cr)
                : new CloseResult((CloseResult) cr);
    }

    private static final Pattern softHyphenPattern = Pattern.compile("\u00ad");
    private static final Pattern trailingWsBraillePattern = Pattern.compile("[\\s\u2800]+\\z");

    private class CurrentResultImpl implements CurrentResult {

        private final BrailleTranslatorResult btr;
        /* mode to translate list label */
        private final String mode;
        private boolean first;

        CurrentResultImpl(BrailleTranslatorResult btr, String mode) {
            this.btr = btr;
            this.mode = mode;
            this.first = true;
        }

        private CurrentResultImpl(CurrentResultImpl template) {
            this.btr = template.btr.copy();
            this.mode = template.mode;
            this.first = template.first;
        }

        @Override
        public boolean hasNext() {
            return first || btr.hasNext();
        }

        @Override
        public Optional<RowImpl> process(LineProperties lineProps) {
            if (first) {
                first = false;
                return processFirst(lineProps);
            }
            try {
                if (btr.hasNext()) { //LayoutTools.length(chars.toString())>0
                    if (hasCurrentRow()) {
                        return Optional.of(flushCurrentRow());
                    }
                    return startNewRow(
                        btr,
                        "",
                        processorContext.getRowDataProps().getBlockIndent()
                            + processorContext.getRowDataProps().getTextIndent(),
                        processorContext.getRowDataProps().getRightTextIndent(),
                        lineProps
                    );
                }
            } finally {
                if (!btr.hasNext() && btr.supportsMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK)) {
                    addToForceCount(btr.getMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK));
                }
            }
            return Optional.empty();
        }

        private Optional<RowImpl> processFirst(LineProperties lineProps) {
            // process first row, is it a new block or should we continue the current row?
            if (!hasCurrentRow()) {
                // add to left margin
                if (hasListItem()) { //currentListType!=BlockProperties.ListType.NONE) {
                    ListItem item = getListItem();
                    String listLabel;
                    try {
                        listLabel = processorContext.getFormatterContext().getTranslator(mode).translate(
                            Translatable.text(
                                processorContext.getFormatterContext().getConfiguration().isMarkingCapitalLetters() ?
                                item.getLabel() :
                                item.getLabel().toLowerCase()
                            ).build()
                        ).getTranslatedRemainder();
                    } catch (TranslationException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if (item.getType() == FormattingTypes.ListStyle.PL) {
                            return startNewRow(
                                btr,
                                listLabel,
                                processorContext.getRowDataProps().getBlockIndentParent(),
                                processorContext.getRowDataProps().getRightTextIndent(),
                                lineProps
                            );
                        } else {
                            return startNewRow(
                                btr,
                                listLabel,
                                processorContext.getRowDataProps().getBlockIndent()
                                    + processorContext.getRowDataProps().getFirstLineIndent(),
                                processorContext.getRowDataProps().getRightTextIndent(),
                                lineProps
                            );
                        }
                    } finally {
                        discardListItem();
                    }
                } else {
                    return startNewRow(
                        btr,
                        "",
                        processorContext.getRowDataProps().getBlockIndent()
                            + processorContext.getRowDataProps().getFirstLineIndent(),
                        processorContext.getRowDataProps().getRightTextIndent(),
                        lineProps
                    );
                }
            } else {
                return continueRow(
                    new RowInfo("", processorContext.getAvailable() - lineProps.getReservedWidth()),
                    btr,
                    processorContext.getRowDataProps().getRightTextIndent(),
                    lineProps
                );
            }
        }

        /*
         * Start new active row, possibly with list label in the left margin, and fill it with
         * characters from a BrailleTranslatorResult.
         */
        private Optional<RowImpl> startNewRow(
            BrailleTranslatorResult btr,
            String listLabel,
            int leftIndent,
            int rightIndentIfNotLastRow,
            LineProperties lineProps
        ) {
            if (hasCurrentRow()) {
                // startNewRow() should only be called after having flushed the current row, or
                // after having checked that there is none.
                throw new RuntimeException("Error in code.");
            }
            newCurrentRow(processorContext.getMargins().getLeftMargin(),
                          processorContext.getMargins().getRightMargin());
            return continueRow(
                new RowInfo(
                    getLeftIndentWithLabel(listLabel, leftIndent),
                    processorContext.getAvailable() - lineProps.getReservedWidth()
                ),
                btr,
                rightIndentIfNotLastRow,
                lineProps
            );
        }

        private String getLeftIndentWithLabel(String listLabel, int indent) {
            indent = Math.max(
                // There is one known cause for this calculation to become < 0. That is when an ordered list is so long
                // that the number takes up more space than the indent reserved for it.
                // In that case it is probably best to push the content instead of failing altogether.
                indent - StringTools.length(listLabel),
                0);
            return listLabel + StringTools.fill(processorContext.getSpaceCharacter(), indent);
        }

        /*
         * Update the active row with a RowInfo (indentation and list label) and BrailleTranslatorResult.
         */
        private Optional<RowImpl> continueRow(
            RowInfo row,
            BrailleTranslatorResult btr,
            int rightIndentIfNotLastRow,
            LineProperties lineProps
        ) {
            if (!hasCurrentRow()) {
                // continueRow() should only be called after having checked that there is a current
                // row, or after having created a new one.
                throw new RuntimeException("Error in code.");
            }
            String tabSpace = ""; // leader content
            boolean hasLeader = leaderManager.hasLeader();
            int leaderPos = -1; // tab stop position

            // if a leader is pending lay it out first
            if (hasLeader) {
                int preTabPos = row.getPreTabPosition(currentRow); // start position of leader
                leaderPos = leaderManager.getLeaderPosition(
                    processorContext.getAvailable() - lineProps.getReservedWidth()
                );
                int align = leaderManager.getLeaderAlign(btr.countRemaining()); // space after leader before tab stop
                if (leaderPos - align < preTabPos) {
                    // if tab position has been passed try on a new row
                    return Optional.ofNullable(flushCurrentRow());
                } else {
                    tabSpace = leaderManager.getLeaderPattern(leaderPos - preTabPos - align); // leader length
                }
            }

            // Size of content already present in row. If called from startNewRow() currentRow does
            // not include the left text indent, otherwise it does.
            int contentLen = StringTools.length(tabSpace) + StringTools.length(currentRow.getText());
            boolean force = contentLen == 0;
            int availableIfLastRow = row.getMaxLength(currentRow) - contentLen;
            // check whether we are on the last row of the block (only matters if there is a
            // right-text-indent)
            boolean onLastRow = false;
            if (rightIndentIfNotLastRow > 0) {
                // We only support the following cases:
                // - The current result starts with a leader and fits on the last row and there are
                //   no following segments. All text, page number reference, marker reference,
                //   evaluate, marker, anchor and identifier segments after a leader are combined
                //   with the leader into a single CurrentResult, so only new line, leader or
                //   external reference segments can immediately follow. Only in case of the two
                //   latter we may be incorrectly assuming we are not on the last row (and therefore
                //   may be using less space than is available).
                if (hasLeader) {
                    if (!hasMoreSegments()) {
                        BrailleTranslatorResult btrCopy = btr.copy();
                        btrCopy.nextTranslatedRow(availableIfLastRow, force, false);
                        if (!btrCopy.hasNext()) {
                            onLastRow = true;
                        }
                    }
                // - The current result fits on the row regardless of whether it is the last row or
                //   not. We assume that we are on the last row because it doesn't matter. Only in
                //   the case the current result does not fit on a row with right text indent
                //   applied, but does fit together with all the following content on a row without
                //   right text indent applied we are incorrectly assuming we are not on the last
                //   row. This only becomes a problem as soon as right-text-indent exceeds the
                //   distance between the right end of the leader and the right edge of the box.
                } else {
                    BrailleTranslatorResult btrCopy = btr.copy();
                    String remainder = btrCopy.nextTranslatedRow(availableIfLastRow, force, false);
                    if (!btrCopy.hasNext()
                        && remainder.length() <= availableIfLastRow - rightIndentIfNotLastRow) {
                        onLastRow = true;
                    }
                }
            }
            int rightIndent = onLastRow ? 0 : rightIndentIfNotLastRow;
            // space available for putting text
            int available = availableIfLastRow - rightIndent;
            // break line
            String next = btr.nextTranslatedRow(available, force, lineProps.suppressHyphenation());
            // don't know if soft hyphens need to be replaced, but we'll keep it for now
            next = softHyphenPattern.matcher(next).replaceAll("");
            if (hasLeader) {

                // If there is a leader, insert it if the content that follows it fits on the line
                // or if there is no following content. It is an error if the leader alignment is
                // right or center and the content that follows it does not fit. If the leader
                // alignment is left just insert any text that fits. If no text fits just insert the
                // leader. It is an error if the leader does not even fit.
                switch (leaderManager.getCurrentLeader().getAlignment()) {
                case CENTER:
                case RIGHT:
                    if (available < 0 || btr.hasNext()) {
                        if (lineProps.getReservedWidth() > 0) {
                            // if width is temporarily reduced try the next line
                            return Optional.ofNullable(flushCurrentRow());
                        }
                        int rightMargin = currentRow.getRightMargin().getContent().length();
                        throw new RuntimeException(
                            "Block is too narrow to fit the leader with the text after it ("
                            + "total available width: " + (processorContext.getAvailable() + rightMargin)
                            + ", leader position: " + leaderPos
                            + ", leader alignment: " + leaderManager.getCurrentLeader().getAlignment()
                            + ", text after leader: \"" + next + btr.getTranslatedRemainder() + "\""
                            + (rightIndent > 0 ? ", right text indent: " + rightIndent : "")
                            + (rightMargin > 0 ? ", right margin: " + rightMargin : "")
                            + ")"
                        );
                    }
                    break;
                case LEFT:
                    if (available < 0) {
                        if (lineProps.getReservedWidth() > 0) {
                            // if width is temporarily reduced try the next line
                            return Optional.ofNullable(flushCurrentRow());
                        }
                        int rightMargin = currentRow.getRightMargin().getContent().length();
                        throw new RuntimeException(
                            "Block is too narrow to fit the leader ("
                            + "total available width: " + (processorContext.getAvailable() + rightMargin)
                            + ", leader position: " + leaderPos
                            + (rightIndent > 0 ? ", right text indent: " + rightIndent : "")
                            + (rightMargin > 0 ? ", right margin: " + rightMargin : "")
                            + ")"
                        );
                    }
                }
                currentRow.text(row.getLeftIndent() + currentRow.getText() + tabSpace + next);
                if (!tabSpace.isEmpty()) {
                    currentRow.leaderSpace(currentRow.getLeaderSpace() + tabSpace.length());
                }
                // tabSpace has been inserted, discard the leader now
                leaderManager.removeLeader();
            } else if (!next.isEmpty()) {

                // If there is no leader, just insert any content that fits on the line.
                currentRow.text(row.getLeftIndent() + currentRow.getText() + next);
            } else if (available < 0 || (available == 0 && contentLen == 0 && btr.hasNext())) {

                // If no text fits on the line because there is no available space, this is either
                // because the previous segment filled up the row completely, in which case the row
                // will be flushed next, or if a new row was started it's because there is no space
                // between the left and right margin edges for text and left and right text indent,
                // in which case we need to abort in order to avoid an endless loop.
                int leftMargin = currentRow.getLeftMargin().getContent().length();
                int leftIndent = row.getLeftIndent().length();
                int rightMargin = currentRow.getRightMargin().getContent().length();
                int reservedCells = lineProps.getReservedWidth();
                throw new RuntimeException(
                    "Block is too narrow to contain any text ("
                    + "total available width: " + (processorContext.getAvailable() + rightMargin)
                    + (leftMargin > 0 ? ", left margin: " + leftMargin : "")
                    + (leftIndent > 0 ? ", left text indent: " + leftIndent : "")
                    + (rightIndent > 0 ? ", right text indent: " + rightIndent : "")
                    + (rightMargin > 0 ? ", right margin: " + rightMargin : "")
                    + (reservedCells > 0 ? ", reserved cells: " + reservedCells : "")
                    + ")"
                );
            } else {
                currentRow.text(
                    row.getLeftIndent() + trailingWsBraillePattern.matcher(currentRow.getText()).replaceAll(""));
            }
            if (btr instanceof AggregatedBrailleTranslatorResult) {
                AggregatedBrailleTranslatorResult abtr = ((AggregatedBrailleTranslatorResult) btr);
                currentRow.addMarkers(abtr.getMarkers());
                currentRow.addAnchors(abtr.getAnchors());
                currentRow.addIdentifiers(abtr.getIdentifiers());
                abtr.clearPending();
            }
            // When right-text-indent was applied and the current result fitted exactly on the row
            // and trailing spaces were discarded, we don't want more content to be added to it
            // (which is possible because by cutting the trailing spaces and not applying
            // right-text-indent we could have created just enough room to fit all remaining
            // content). Hence we flush the row.
            if (rightIndentIfNotLastRow > 0
                && !onLastRow
                && !btr.hasNext()
            ) {
                return Optional.ofNullable(flushCurrentRow());
            }
            // Returning empty value means that currentRow has been updated but we don't want to
            // flush it yet because a next segment might add to the same row.
            return Optional.empty();
        }
    }

    private class NewLineResult implements CurrentResult {

        private boolean newLine;
        private Optional<CurrentResult> before;

        NewLineResult(Optional<CurrentResult> before) {
            this.before = before;
            this.newLine = true;
        }

        private NewLineResult(NewLineResult template) {
            if (template.before.isPresent()) {
                this.before = Optional.of(copy(template.before.get()));
            }
            this.newLine = template.newLine;
        }

        @Override
        public boolean hasNext() {
            return before.isPresent() && before.get().hasNext() || newLine;
        }

        @Override
        public Optional<RowImpl> process(LineProperties lineProps) {
            if (before.isPresent() && before.get().hasNext()) {
                return before.get().process(lineProps);
            } else if (newLine) {
                newLine = false;
                try {
                    if (hasCurrentRow()) {
                        return Optional.of(flushCurrentRow());
                    }
                } finally {
                    MarginProperties ret = new MarginProperties(
                        processorContext.getMargins().getLeftMargin().getContent() + StringTools.fill(
                            processorContext.getSpaceCharacter(),
                            processorContext.getRowDataProps().getTextIndent()
                        ),
                        processorContext.getMargins().getLeftMargin().isSpaceOnly()
                    );
                    newCurrentRow(ret, processorContext.getMargins().getRightMargin());
                }
            }
            return Optional.empty();
        }
    }

    private class CloseResult implements CurrentResult {

        private Optional<CurrentResult> before;
        private boolean doFlush;
        private boolean doUnderline;

        CloseResult(Optional<CurrentResult> before) {
            this.before = before;
            this.doFlush = true;
            this.doUnderline = processorContext.getRowDataProps().getUnderlineStyle() != null;
        }

        private CloseResult(CloseResult template) {
            if (template.before.isPresent()) {
                this.before = Optional.of(copy(template.before.get()));
            } else {
                this.before = Optional.empty();
            }
            this.doFlush = template.doFlush;
            this.doUnderline = template.doUnderline;
        }

        @Override
        public boolean hasNext() {
            return before.isPresent() && before.get().hasNext() || doFlush || (rowsFlushed && doUnderline);
        }

        @Override
        public Optional<RowImpl> process(LineProperties lineProps) {
            if (before.isPresent() && before.get().hasNext()) {
                return before.get().process(lineProps);
            } else if (doFlush) {
                doFlush = false;
                if (hasCurrentRow()) {
                    return Optional.of(flushCurrentRow());
                }
            } else if (rowsFlushed && doUnderline) {
                doUnderline = false;
                if (
                    unusedLeft < processorContext.getMargins().getLeftMargin().getContent().length() ||
                    unusedRight < processorContext.getMargins().getRightMargin().getContent().length()
                ) {
                    throw new RuntimeException("coding error");
                }
                return Optional.of(
                        new RowImpl.Builder(
                            StringTools.fill(
                                    processorContext.getSpaceCharacter(),
                                    unusedLeft - processorContext.getMargins().getLeftMargin().getContent().length()
                            ) +
                            StringTools.fill(
                                processorContext.getRowDataProps().getUnderlineStyle(),
                                processorContext.getFlowWidth() - unusedLeft - unusedRight
                            )
                        )
                        .leftMargin(processorContext.getMargins().getLeftMargin())
                        .rightMargin(processorContext.getMargins().getRightMargin())
                        .adjustedForMargin(true)
                        .build());
            }
            return Optional.empty();
        }
    }
}
