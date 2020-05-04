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
    private final boolean significantContent;
    private final SegmentProcessorContext processorContext;

    private int segmentIndex;
    /**
     * The active (not completed) row, as a mutable object (builder).
     */
    private RowImpl.Builder currentRow;
    /*
     * Markers, anchors and identifiers that are not associated with a particular row.
     */
    private final ArrayList<Marker> groupMarkers;
    private final ArrayList<String> groupAnchors;
    private final ArrayList<String> groupIdentifiers;
    private Object externalReference = null;
    /*
     * Buffers a number of evaluated segments that follow a leader (contained in `leaderManager') in
     * a single aggregated BrailleTranslatorResult. Can only be non-null if `current' is null.
     */
    private AggregatedBrailleTranslatorResult.Builder layoutOrApplyAfterLeader;
    /*
     * Mode to be used to translate the segments contained in layoutOrApplyAfterLeader.
     */
    private String currentLeaderMode;
    private boolean seenSegmentAfterLeader;
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
    private Function<MarkerReference, String> markerRefResolver;
    private Function<Evaluate, String> expressionResolver;

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
        this.significantContent = calculateSignificantContent(this.segments, context, rdp);
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
        this.markerRefResolver = (ref) -> refs.findMarker(getContext().getCurrentPageId(), ref);
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
        this.currentLeaderMode = template.currentLeaderMode;
        this.seenSegmentAfterLeader = template.seenSegmentAfterLeader;
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
        this.significantContent = template.significantContent;
        this.blockId = template.blockId;
        this.pagenumResolver = template.pagenumResolver;
        // can't simply copy because getContext() of template would be used
        this.markerRefResolver = (ref) -> refs.findMarker(getContext().getCurrentPageId(), ref);
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
        currentLeaderMode = null;
        seenSegmentAfterLeader = false;
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
        return significantContent;
    }

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
            ts.storeResult(btr);
        } else {
            btr = ts.newResult();
        }
        if (leaderManager.hasLeader()) {
            layoutAfterLeader(btr, mode);
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
            leaderManager.addLeader(ls);
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
            String mode = null;
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
                String mode = null;
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
                String mode = null;
                BrailleTranslatorResult btr = toResult(spec, mode);
                CurrentResult current = new CurrentResultImpl(btr, mode);
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    private void layoutAfterLeader(TranslatableWithContext spec, String mode) {
        layoutAfterLeader(toResult(spec, mode), mode);
    }

    /*
     * Buffer an evaluated segment (BrailleTranslatorResult) in `layoutOrApplyAfterLeader'.
     */
    private void layoutAfterLeader(BrailleTranslatorResult result, String mode) {
        if (leaderManager.hasLeader()) {
            if (layoutOrApplyAfterLeader == null) {
                layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
                // use the mode of the first following segment to translate the leader pattern (or
                // the mode of the first preceding segment)
                if (!seenSegmentAfterLeader) {
                    currentLeaderMode = mode;
                    seenSegmentAfterLeader = true;
                }
            }
            layoutOrApplyAfterLeader.add(result);
        } else {
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
            String mode;
            if (layoutOrApplyAfterLeader == null) {
                btr = toResult("");
                mode = null;
            } else {
                btr = layoutOrApplyAfterLeader.build();
                mode = currentLeaderMode;
                layoutOrApplyAfterLeader = null;
                seenSegmentAfterLeader = false;
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
                        processorContext.getRowDataProps().getBlockIndent()
                            + processorContext.getRowDataProps().getTextIndent(),
                        processorContext.getRowDataProps().getRightTextIndent(),
                        mode,
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
                                processorContext.getRowDataProps().getBlockIndentParent()
                                    + processorContext.getRowDataProps().getTextIndent(),
                                processorContext.getRowDataProps().getRightTextIndent(),
                                mode,
                                lineProps
                            );
                        } else {
                            return startNewRow(
                                btr,
                                listLabel,
                                processorContext.getRowDataProps().getBlockIndent()
                                    + processorContext.getRowDataProps().getFirstLineIndent(),
                                processorContext.getRowDataProps().getBlockIndent()
                                    + processorContext.getRowDataProps().getTextIndent(),
                                processorContext.getRowDataProps().getRightTextIndent(),
                                mode,
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
                        processorContext.getRowDataProps().getBlockIndent()
                            + processorContext.getRowDataProps().getTextIndent(),
                        processorContext.getRowDataProps().getRightTextIndent(),
                        mode,
                        lineProps
                    );
                }
            } else {
                return continueRow(
                    new RowInfo("", processorContext.getAvailable() - lineProps.getReservedWidth()),
                    btr,
                    processorContext.getRowDataProps().getBlockIndent()
                        + processorContext.getRowDataProps().getTextIndent(),
                    processorContext.getRowDataProps().getRightTextIndent(),
                    mode,
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
            int nextRowLeftIndent,
            int rightIndentIfNotLastRow,
            String mode,
            LineProperties lineProps
        ) {
            if (hasCurrentRow()) {
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
                nextRowLeftIndent,
                rightIndentIfNotLastRow,
                mode,
                lineProps
            );
        }

        private String getLeftIndent(int indent) {
            return getLeftIndentWithLabel("", indent);
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
            int nextRowIndent,
            int rightIndentIfNotLastRow,
            String mode,
            LineProperties lineProps
        ) {
            RowImpl ret = null;
            // [margin][preContent][preTabText][tab][postTabText]
            //      preContentPos ^
            String tabSpace = "";
            boolean rightAlignedLeader = false;

            // if a leader is pending lay it out first
            if (leaderManager.hasLeader()) {
                rightAlignedLeader = leaderManager.getCurrentLeader().getAlignment() == Leader.Alignment.RIGHT;
                int preTabPos = row.getPreTabPosition(currentRow);
                int leaderPos = leaderManager.getLeaderPosition(
                    processorContext.getAvailable() - lineProps.getReservedWidth()
                );
                int offset = leaderPos - preTabPos;
                int align = leaderManager.getLeaderAlign(btr.countRemaining());

                if (
                    preTabPos > leaderPos ||
                    offset - align < 0
                ) { // if tab position has been passed or if text does not fit within row, try on a new row
                    MarginProperties leftMargin = currentRow.getLeftMargin();
                    if (hasCurrentRow()) {
                        ret = flushCurrentRow();
                    }
                    newCurrentRow(leftMargin, processorContext.getMargins().getRightMargin());
                    row = new RowInfo(
                        getLeftIndent(nextRowIndent),
                        processorContext.getAvailable() - lineProps.getReservedWidth()
                    );
                    //update offset
                    offset = leaderPos - row.getPreTabPosition(currentRow);
                }
                try {
                    tabSpace = leaderManager.getLeaderPattern(
                        processorContext.getFormatterContext().getTranslator(mode),
                        offset - align
                    );
                } finally {
                    // always discard leader
                    leaderManager.removeLeader();
                }
            }

            // get next row from BrailleTranslatorResult
            int contentLen = StringTools.length(tabSpace) + StringTools.length(currentRow.getText());
            boolean force = contentLen == 0;
            int availableIfLastRow = row.getMaxLength(currentRow) - contentLen;
            String next = null;
            boolean onLastRow = false;
            // This implementation does not make use the full available space for the last line
            // unless a right aligned leader is present.
            if (rightAlignedLeader
                // If a right aligned leader is present and there are more segments, they are either:
                // - newlines: this means we can not be on the last line
                // - leaders: we may be on last line but this function will be called again
                // - external references: may be on last line; currently not supported
                && !hasMoreSegments()) {
                BrailleTranslatorResult btrCopy = btr.copy();
                btrCopy.nextTranslatedRow(availableIfLastRow, force, false);
                if (!btrCopy.hasNext()) {
                    onLastRow = true;
                }
            }
            int available = availableIfLastRow;
            if (!onLastRow) {
                available -= rightIndentIfNotLastRow;
            }
            // break line
            next = btr.nextTranslatedRow(available, force, lineProps.suppressHyphenation());
            // don't know if soft hyphens need to be replaced, but we'll keep it for now
            next = softHyphenPattern.matcher(next).replaceAll("");
            if ("".equals(next) && "".equals(tabSpace)) {
                currentRow.text(
                    row.getLeftIndent() + trailingWsBraillePattern.matcher(currentRow.getText()).replaceAll(""));
            } else {
                currentRow.text(row.getLeftIndent() + currentRow.getText() + tabSpace + next);
                currentRow.leaderSpace(currentRow.getLeaderSpace() + tabSpace.length());
            }
            if (btr instanceof AggregatedBrailleTranslatorResult) {
                AggregatedBrailleTranslatorResult abtr = ((AggregatedBrailleTranslatorResult) btr);
                currentRow.addMarkers(abtr.getMarkers());
                currentRow.addAnchors(abtr.getAnchors());
                currentRow.addIdentifiers(abtr.getIdentifiers());
                abtr.clearPending();
            }
            return Optional.ofNullable(ret);
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
