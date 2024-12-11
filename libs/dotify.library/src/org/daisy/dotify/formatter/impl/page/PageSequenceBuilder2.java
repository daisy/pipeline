package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.api.formatter.FallbackRule;
import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.formatter.RenameFallbackRule;
import org.daisy.dotify.api.formatter.TransitionBuilderProperties.ApplicationRange;
import org.daisy.dotify.common.splitter.SplitPoint;
import org.daisy.dotify.common.splitter.SplitPointCost;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitPointHandler;
import org.daisy.dotify.common.splitter.SplitPointSpecification;
import org.daisy.dotify.common.splitter.StandardSplitOption;
import org.daisy.dotify.common.splitter.Supplements;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.ContentCollectionImpl;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.core.TransitionContent;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.BlockLineLocation;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.PageId;
import org.daisy.dotify.formatter.impl.search.SequenceId;
import org.daisy.dotify.formatter.impl.search.TransitionProperties;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Given a {@link BlockSequence}, produces {@link org.daisy.dotify.formatter.impl.common.Page}
 * objects one by one. The pages are obtained through a {@link #nextPage(int, boolean, Optional,
 * boolean, boolean) "iterator" interface}.</p>
 *
 * <ul>
 *   <li>Performs page breaking.</li>
 *   <li>Constructs any "page-area" areas and fills it with collection items that are referenced
 *   from anchors on this page.</li>
 *   <li>Adds any header and footer lines. (This is done in {@link PageImpl}.)</li>
 *   <li>Adds "<code>margin-region</code>" columns. (This is done in {@link PageImpl}.)</li>
 *   <li>Adds any "<code>volume-transition</code>" content (<code>sequence-interrupted</code>,
 *       <code>sequence-resumed</code>, <code>any-interrupted</code> and/or <code>any-resumed</code>).
 *       <br>
 *       Note that <code>block-interrupted</code> and <code>block-resumed</code> are currently not implemented.
 *   </li>
 * </ul>
 *
 * <p>The input is a sequence of blocks, which are first converted to a sequence of {@link
 * RowGroupSequence}. If a sequence has multiple possible scenarios the best one is selected. Every
 * <code>RowGroupSequence</code> either starts on a new sheet, a new page, or on a specific position
 * on the page. A <code>RowGroupSequence</code> that does not fit on the page is broken, using
 * {@link SplitPointHandler}. The cost function takes into account the gap at the bottom of the
 * page, whether the {@link RowGroup#isBreakable() isBreakable} and {@link
 * RowGroup#getAvoidVolumeBreakAfterPriority()} constraints of the last <code>RowGroup</code> are
 * violated, and whether we're breaking inside a block that can not flow around header/footer fields
 * while the header/footer does allow it.
 *
 * <p>The computed {@link VolumeKeepPriority} of page is the maximum value (lowest priority) of all
 * {@link RowGroup}s on that page. Discarded {@link RowGroup}s (i.e. collapsed margins) are also
 * taken into account.</p>
 */

public class PageSequenceBuilder2 {
    private final FormatterContext context;
    private final PageAreaContent staticAreaContent;
    private final PageAreaProperties areaProps;

    private final ContentCollectionImpl collection;
    private BlockContext initialContext;
    private final CollectionData cd;
    private final LayoutMaster master;
    private final List<RowGroupSequence> dataGroups;
    private final FieldResolver fieldResolver;
    private final SequenceId seqId;
    private final SplitPointHandler<RowGroup, RowGroupDataSource> sph;

    private boolean force;
    private RowGroupDataSource data;

    private int keepNextSheets;
    private int pageCount = 0;
    private int dataGroupsIndex;
    private boolean nextEmpty = false;

    private BlockLineLocation cbl;
    private BlockLineLocation prevCbl; // value of cbl in effect before the last call to nextPage(),
                                       // or null if nextPage() has not been called yet

    //From view, temporary
    private final int fromIndex;
    private int toIndex;

    /**
     * @param fromIndex  "Index" of the first page of this <code>PageSequenceBuilder</code>. The
     *                   exact value does not matter for <code>PageSequenceBuilder2</code> itself, but {@link
     *                   org.daisy.dotify.formatter.impl.sheet.SheetDataSource} requires that the values returned
     *                   by {@link #getGlobalStartIndex()} and {@link #getToIndex()} match the provided value. The
     *                   value passed by {@link org.daisy.dotify.formatter.impl.sheet.SheetDataSource} is the
     *                   number of pages that either the whole body, or the pre- or post-content of the current
     *                   volume, already contains.
     * @param master     the layout master
     * @param pageOffset see the <code>pageNumberOffset</code> parameter in {@link #nextPage(int,
     *                   boolean, Optional, boolean, boolean) nextPage}.
     * @param seq        the input block sequence
     * @param context    ?
     * @param rcontext   ?
     * @param seqId      identifier/position of the block sequence
     * @param blc        The {@link BlockLineLocation} of the last line of the previous PageSequenceBuilder2, or
     *                   null if there is no previous PageSequenceBuilder2.
     */
    public PageSequenceBuilder2(
        int fromIndex,
        LayoutMaster master,
        int pageOffset,
        BlockSequence seq,
        FormatterContext context,
        DefaultContext rcontext,
        SequenceId seqId,
        BlockLineLocation blc
    ) {
        this.fromIndex = fromIndex;
        this.toIndex = fromIndex;
        this.master = master;
        this.context = context;
        this.sph = new SplitPointHandler<>();
        this.areaProps = seq.getLayoutMaster().getPageArea();
        if (this.areaProps != null) {
            this.collection = context.getCollections().get(areaProps.getCollectionId());
        } else {
            this.collection = null;
        }
        keepNextSheets = 0;

        initialContext = BlockContext.from(rcontext)
                .flowWidth(seq.getLayoutMaster().getFlowWidth())
                .formatterContext(context)
                .build();
        this.staticAreaContent = new PageAreaContent(seq.getLayoutMaster().getPageAreaBuilder(), initialContext);
        //For the scenario processing, it is assumed that all page templates
        //have margin regions that are of the same width.
        //However, it is unlikely to have a big impact on the selection.
        BlockContext bc = BlockContext.from(initialContext)
                .flowWidth(master.getFlowWidth() - master.getTemplate(1).getTotalMarginRegionWidth())
                .build();
        this.dataGroups = seq.selectScenario(master, bc, true);
        this.cd = new CollectionData(staticAreaContent, initialContext, master, collection);
        this.dataGroupsIndex = 0;
        this.seqId = seqId;
        this.cbl = blc;
        this.prevCbl = null;
        PageDetails details = new PageDetails(
            master.duplex(),
            new PageId(pageCount, getGlobalStartIndex(), seqId),
            cbl,
            pageOffset
        );
        this.fieldResolver = new FieldResolver(master, context, rcontext.getRefs(), details);
    }

    public PageSequenceBuilder2(PageSequenceBuilder2 template) {
        this.context = template.context;
        this.staticAreaContent = template.staticAreaContent;
        this.areaProps = template.areaProps;
        this.collection = template.collection;
        this.initialContext = template.initialContext;
        this.master = template.master;
        this.dataGroups = template.dataGroups;
        this.cd = template.cd;
        this.dataGroupsIndex = template.dataGroupsIndex;
        this.fieldResolver = template.fieldResolver;
        this.seqId = template.seqId;
        this.sph = template.sph;
        this.force = template.force;
        this.data = RowGroupDataSource.copyUnlessNull(template.data);
        this.keepNextSheets = template.keepNextSheets;
        this.pageCount = template.pageCount;
        this.nextEmpty = template.nextEmpty;
        this.fromIndex = template.fromIndex;
        this.toIndex = template.toIndex;
        this.cbl = template.cbl;
        this.prevCbl = template.prevCbl;
    }

    public static PageSequenceBuilder2 copyUnlessNull(PageSequenceBuilder2 template) {
        return template == null ? null : new PageSequenceBuilder2(template);
    }

    private BlockContext getContext() {
        if (data != null) {
            return data.getContext();
        } else {
            return initialContext;
        }
    }

    private void modifyContext(Consumer<? super BlockContext.Builder> modifier) {
        if (data != null) {
            data.modifyContext(modifier);
        } else {
            BlockContext.Builder b = BlockContext.from(initialContext);
            modifier.accept(b);
            initialContext = b.build();
        }
    }

    public void setCurrentVolumeNumber(int volume) {
        modifyContext(c -> c.currentVolume(volume));
    }

    /**
     * Gets a new PageId representing the next page in this sequence.
     *
     * @param offset the offset
     * @return returns the next page Id
     */
    public PageId nextPageId(int offset) {
        return new PageId(pageCount + offset, getGlobalStartIndex(), seqId);
    }

    /**
     * @return The BlockLineLocation of the last line of the page that was produced last (by this
     *         PageSequenceBuilder2 or the previous one), or null if no page has been produced yet.
     */
    public BlockLineLocation currentBlockLineLocation() {
        return cbl;
    }

    private PageImpl newPage(int pageNumberOffset) {
        PageDetails details = new PageDetails(
            master.duplex(),
            new PageId(pageCount, getGlobalStartIndex(), seqId),
            cbl,
            pageNumberOffset
        );
        PageImpl ret = new PageImpl(fieldResolver, details, master, context, staticAreaContent);
        pageCount++;
        if (keepNextSheets > 0) {
            ret.setAllowsVolumeBreak(false);
        }
        if ((!master.duplex() || pageCount % 2 == 0) && keepNextSheets > 0) {
            keepNextSheets--;
        }
        return ret;
    }

    private void newRow(PageImpl p, RowImpl row) {
        if (p.spaceAvailableInFlow() < 1) {
            throw new RuntimeException("Error in code.");
            //newPage();
        }
        p.newRow(row);
    }

    public boolean hasNext() {
        return dataGroupsIndex < dataGroups.size() || (data != null && !data.isEmpty());
    }

    /**
     * @param pageNumberOffset   Page number corresponding to the first page of this
     *                           <code>PageSequenceBuilder2</code> minus 1, if the page numbering would be continuous
     *                           between the first and the current page. This does not necessarily match the actual page
     *                           number of the produced page, because for instance when a volume break happens inside a
     *                           sequence, there can be a jump in the page numbering.
     * @param hyphenateLastLine  Whether to allow the last line on the page to end on a hyphenation
     *                           point (may be the case on the last page of the volume).
     * @param transitionContent  Content to be inserted at the top (any-resumed and/or
     *                           sequence-resumed) or at the bottom of the page (any-interrupted and/or
     *                           sequence-interrupted). any-resumed is only enabled if <code>isFirst</code> is not
     *                           <code>true</code>, sequence-resumed is only enabled if <code>wasSplitInSequence</code>
     *                           is <code>true</code>, sequence-interrupted is only enabled if the page is broken
     *                           within the sequence.
     * @param wasSplitInSequence see above
     * @param isFirst            see above
     * @return the next page
     */
    public PageImpl nextPage(
            int pageNumberOffset,
            boolean hyphenateLastLine,
            Optional<TransitionContent> transitionContent,
            boolean wasSplitInSequence,
            boolean isFirst
    ) throws PaginatorException, RestartPaginationException { // pagination must be restarted in
        // PageStructBuilder.paginateInner
        PageImpl ret = nextPageInner(
            pageNumberOffset,
            hyphenateLastLine,
            transitionContent,
            wasSplitInSequence,
            isFirst
        );
        getContext().getRefs().keepPageDetails(ret.getDetails());
        for (String id : ret.getIdentifiers()) {
            getContext().getRefs().setPageNumber(id, ret.getPageNumber());
        }
        toIndex++;
        return ret;
    }

    private PageImpl nextPageInner(
            int pageNumberOffset,
            boolean hyphenateLastLine,
            Optional<TransitionContent> transitionContent,
            boolean wasSplitInSequence,
            boolean isFirst
    ) throws PaginatorException, RestartPaginationException { // pagination must be restarted in
        // PageStructBuilder.paginateInner
        PageImpl current = newPage(pageNumberOffset);
        // whether this is the last page of the volume and a volume transition was provided
        boolean interruptContentPresent =
            context.getTransitionBuilder().getProperties().getApplicationRange() != ApplicationRange.NONE &&
            transitionContent.isPresent() &&
            transitionContent.get().getType() == TransitionContent.Type.INTERRUPT;
        if (nextEmpty) {
            // Return an empty page because the next row should be on a new sheet and we are
            // currently on the back of a sheet. (We are in duplex mode.)
            // Note that we don't update cbl here, so the page after this one will have the same
            // identity. Luckily it does not matter for SheetIdentity: there can't be two empty
            // verso pages after each other so every SheetIdentity is still unique.
            // Also note that we don't call keepTransitionProperties().
            nextEmpty = false;
            if (prevCbl != null && interruptContentPresent) {
                // By calling setNextPageInSequenceEmptyOrAbsent() we tell SheetDataSource
                // (in the next iteration) two things:
                // 1. to not use this page's currentBlockLineLocation to look up its
                //    transitionProperties because that info is not available (so could result in
                //    CrossReferenceHandler staying dirty forever) and the currentBlockLineLocation
                //    is not unique.
                // 2. If the volume must be broken on a sheet that is empty on the back, we prefer
                //    to put the transition content on the back of the sheet, not on the front.
                getContext().getRefs().setNextPageInSequenceEmptyOrAbsent(prevCbl);
                // Set prevCbl to null so that the command above can not be undone with a
                // setNextPageDetailsInSequence() in the next nextPage() call.
                prevCbl = null;
            }
            return current;
        }
        // Store a mapping from the BlockLineLocation of the last line of the page before the
        // previous page to the BlockLineLocation of the last line of the previous page. This info
        // is used (in the next iteration) in SheetDataSource to obtain info about the verso page of
        // a sheet when we are on a recto page of that sheet and we need to determine whether to
        // break the volume after the current page or the next.
        if (prevCbl != null && interruptContentPresent) {
            getContext().getRefs().setNextPageDetailsInSequence(prevCbl, current.getDetails());
        }
        prevCbl = cbl;

        // The purpose of this is to prevent supplements from combining with header/footer
        int combinableHeaderAndFooterLines = current.getPageTemplate().validateAndAnalyzeHeader()
            + current.getPageTemplate().validateAndAnalyzeFooter();
        cd.setExtraOverhead(combinableHeaderAndFooterLines);

        // At the beginning here before we start printing the page for this iteration we will set
        // the value topOfPage. This value will be changed later on when we aren't at the top of the
        // page anymore (when we have written one or more visible rows - refer to
        // RowGroupDataSource#ensureBuffer).
        modifyContext(c -> c.topOfPage(true));

        // while there are more rows in the current RowGroupSequence, or there are more RowGroupSequences
        while (dataGroupsIndex < dataGroups.size() || (data != null && !data.isEmpty())) {
            if ((data == null || data.isEmpty()) && dataGroupsIndex < dataGroups.size()) {
                // pick up next RowGroupSequence
                RowGroupSequence rgs = dataGroups.get(dataGroupsIndex);
                //TODO: This assumes that all page templates have margin regions that are of the same width
                BlockContext bc = BlockContext.from(getContext())
                        .flowWidth(
                            master.getFlowWidth() -
                            master.getTemplate(current.getPageNumber()).getTotalMarginRegionWidth()
                        )
                        .build();
                // convert to RowGroupDataSource
                data = new RowGroupDataSource(
                    master,
                    bc,
                    rgs.getBlocks(),
                    rgs.getBreakBefore(),
                    rgs.getVerticalSpacing(),
                    cd
                );
                dataGroupsIndex++;
                // perform vertical positioning by inserting empty rows
                if (((RowGroupDataSource) data).getVerticalSpacing() != null) {
                    VerticalSpacing vSpacing = ((RowGroupDataSource) data).getVerticalSpacing();
                    float size = 0;
                    for (RowGroup g : data.getRemaining()) {
                        size += g.getUnitSize();
                    }
                    int pos = calculateVerticalSpace(current, vSpacing.getBlockPosition(), (int) Math.ceil(size));
                    for (int i = 0; i < pos; i++) {
                        RowImpl ri = vSpacing.getEmptyRow();
                        newRow(current, new RowImpl(ri.getChars(), ri.getLeftMargin(), ri.getRightMargin()));
                    }
                }
                force = false;
            }
            modifyContext(
                c -> {
                    c.currentPage(current.getDetails().getPageId(), current.getDetails().getPageNumber());
                    c.flowWidth(
                        master.getFlowWidth() -
                        master.getTemplate(current.getPageNumber()).getTotalMarginRegionWidth()
                    );
                }
            );
            data.setReservedWidths(
                // This function returns the space that header or footer fields take up in the next row
                // of a given RowGroupSequence when all the rows of the sequence would be added to the
                // page. (RowGroupDataSource needs this information in order to know where to break the
                // line.)
                seq -> {
                    // This is an approximation of the vertical position of the next row measured from
                    // the top of the sequence, in terms of rows with normal row spacing. Note that it
                    // is not accurate if the sequence contains collapsing margins. It is also not
                    // totally accurate if non-integer row spacings are used.
                    int pos = seq.getGroup() == null ? 0 : (int) Math.floor(
                        seq.getGroup().stream().mapToDouble(v -> (int) v.getUnitSize()).sum());
                    return master.getFlowWidth() - fieldResolver.getWidth(current.getPageNumber(), pos);
                }
            );
            Optional<Boolean> blockBoundary = Optional.empty();
            if (!data.isEmpty()) {
                RowGroupDataSource copy = new RowGroupDataSource(data);
                // Content of sequence-interrupted or sequence-resumed (which of the two depends on
                // whether we are at the beginning or the end of the volume)
                List<RowGroup> seqTransitionText = transitionContent.isPresent() ?
                    new RowGroupDataSource(
                        master,
                        getContext(),
                        transitionContent.get().getInSequence(),
                        BreakBefore.AUTO,
                        null,
                        cd
                    ).getRemaining() :
                    Collections.emptyList();
                // Content of any-interrupted or any-resumed (which of the two depends on whether we
                // are at the beginning or the end of the volume)
                List<RowGroup> anyTransitionText;
                {
                    if (
                        transitionContent.isPresent() &&
                        !(
                            transitionContent.get().getType() == TransitionContent.Type.RESUME &&
                            isFirst
                        )
                    ) {
                        anyTransitionText = new RowGroupDataSource(
                            master,
                            getContext(),
                            transitionContent.get().getInAny(),
                            BreakBefore.AUTO,
                            null,
                            cd
                        ).getRemaining();
                    } else {
                        anyTransitionText = Collections.emptyList();
                    }
                }
                float anyHeight = height(anyTransitionText, true);
                // First find the optimal break point
                SplitPointSpecification spec;
                boolean addTransition = true;
                if (interruptContentPresent) {
                    // Subtract the height of the transition text from the available height.
                    // We need to account for the last unit size here (because this is the last unit) instead
                    // of the one below. The transition text may have a smaller row spacing than the last row of
                    // the text flow, for example:
                    // Text rows:       X-X-X--|
                    // Transition rows:      X-|
                    // This transition doesn't fit, because the last row of the text flow takes up three rows,
                    // not just one (which it would if a transition didn't follow).
                    float flowHeight =
                            current.getFlowHeight() -
                            anyHeight -
                            height(seqTransitionText, true);
                    SplitPointCost<RowGroup> cost = (SplitPointDataSource<RowGroup, ?> units, int in, int limit) -> {
                        VolumeKeepPriority volumeBreakPriority =
                            data.get(in).getAvoidVolumeBreakAfterPriority();
                        double volBreakCost = // 0-9:
                            10 - (volumeBreakPriority.orElse(10));
                        // not breakable gets "series" 21
                        // breakable, but not last gets "series" 11-20
                        // breakable and last gets "series" 1-10
                        return (data.get(in).isBreakable() ?
                            //prefer new block, then lower volume priority cost
                            (data.get(in).isLastRowGroupInBlock() ? 1 : 11) + volBreakCost
                            : 21 // because 11 + 9 = 20
                        ) * limit - in;
                    };
                    // Find break point with full height available, i.e. without sequence-interrupted subtracted
                    spec = sph.find(
                        current.getFlowHeight() - anyHeight,
                        copy,
                        cost,
                        force ? StandardSplitOption.ALLOW_FORCE : null
                    );
                    SplitPoint<RowGroup, RowGroupDataSource> x = sph.split(spec, copy);
                    // If the tail is empty, there's no need for a transition.
                    // If there isn't a boundary between blocks available to break at, don't insert the text.
                    // There must be enough space available after this position for sequence-interrupted.
                    blockBoundary = Optional.of(hasBlockInScope(x.getHead(), flowHeight));
                    if (!x.getTail().isEmpty() && blockBoundary.get()) {
                        // Find the best break point with the new limit
                        spec = sph.find(
                            flowHeight,
                            copy,
                            cost,
                            transitionContent.isPresent() ? StandardSplitOption.NO_LAST_UNIT_SIZE : null,
                            force ? StandardSplitOption.ALLOW_FORCE : null
                        );
                    } else {
                        addTransition = false;
                    }
                } else {
                    SplitPointCost<RowGroup> cost = (SplitPointDataSource<RowGroup, ?> units, int in, int limit) -> {
                        // This cost function used to take into account a "variableWidthCost"
                        // related to whether space is taken up on the row by header or footer
                        // fields and the RowGroup can not be fit into the available space
                        // (does not support variable width). We know that if this is the case
                        // the row is blank (RowGroupProvider inserts them) so it is fine to
                        // break there, but through the "variableWidthCost" penalty we gave the
                        // algorithm the opportunity to break before the block
                        //
                        // However, because reservedWidths is not accurate yet at this point, it
                        // shouldn't be used to make decisions about break points.
                        return (units.get(in).isBreakable() ? 1 : 2) * limit - in;
                    };
                    float seqHeight = 0;
                    if (wasSplitInSequence) {
                        seqHeight = height(seqTransitionText, false);
                    }
                    // Either RESUME, or no transition on this page.
                    float flowHeight = current.getFlowHeight() - anyHeight - seqHeight;
                    spec = sph.find(flowHeight, copy, cost, force ? StandardSplitOption.ALLOW_FORCE : null);
                }
                // The optimal break point is found. Now we do the actual split.
                // Now apply the information to the live data
                data.setAllowHyphenateLastLine(hyphenateLastLine);
                if (combinableHeaderAndFooterLines > 0) {
                    // Make yet another copy to determine which rows are discarded, in order
                    // to provide accurate `reservedWidth' information for the final split.
                    // Note that this accurate information should ideally be made available
                    // for the find() call. The found split point may not be optimal because
                    // it is based on wrong information.
                    copy = new RowGroupDataSource(data);
                    Set<BlockLineLocation> discarded = new HashSet<>(); {
                        for (RowGroup r : sph.split(spec, copy).getDiscarded()) {
                            LineProperties p = r.getLineProperties();
                            if (p != null) {
                                BlockLineLocation l = p.getBlockLineLocation();
                                if (l != null) {
                                    discarded.add(l);
                                }
                            }
                        }
                    }
                    if (!discarded.isEmpty()) {
                        data.setReservedWidths(
                            seq -> {
                                int pos = seq.getGroup() == null ? 0 : (int) Math.floor(
                                    seq.getGroup().stream().mapToDouble(r -> {
                                            // filter out rows that will be discarded by the SplitPointHandler
                                            // (FIXME: Note that changes in line breaking could in theory change
                                            // the number of rows that a  block spans, resulting in
                                            // BlockLineLocation that are off. For now we ignore this fact or
                                            // assume it will not have an effect on the outcome of the
                                            // reservedWidths function.)
                                            LineProperties p = r.getLineProperties();
                                            if (p != null) {
                                                BlockLineLocation l = p.getBlockLineLocation();
                                                if (l != null && discarded.contains(l)) {
                                                    return 0;
                                                }
                                            }
                                            return (int) r.getUnitSize();
                                        }
                                    ).sum()
                                );
                                return master.getFlowWidth() - fieldResolver.getWidth(current.getPageNumber(), pos);
                            }
                        );
                    }
                }
                SplitPoint<RowGroup, RowGroupDataSource> res = sph.split(spec, data);
                data.setAllowHyphenateLastLine(true);
                if (res.getHead().size() == 0 && force) {
                    if (firstUnitHasSupplements(data) && hasPageAreaCollection()) {
                        reassignCollection();
                    } else {
                        throw new RuntimeException("A layout unit was too big for the page.");
                    }
                }
                // The supplements are added to the page area
                for (RowGroup rg : res.getSupplements()) {
                    current.addToPageArea(rg.getRows());
                }
                force = res.getHead().size() == 0;
                data = res.getTail();
                List<RowGroup> head;
                int firstPageContentRow = 0;
                // If there is a sequence-interrupted or sequence-resumed, it is added to the end of
                // current page or the beginning of the next respectively.
                if (addTransition && transitionContent.isPresent()) {
                    if (transitionContent.get().getType() == TransitionContent.Type.INTERRUPT) {
                        head = new ArrayList<>(res.getHead());
                        head.addAll(seqTransitionText);
                    } else if (
                        transitionContent.get().getType() == TransitionContent.Type.RESUME &&
                        wasSplitInSequence
                    ) {
                        head = new ArrayList<>(seqTransitionText);
                        head.addAll(res.getHead());
                        firstPageContentRow += seqTransitionText.size();
                    } else {
                        head = res.getHead();
                    }
                } else {
                    head = res.getHead();
                }
                // The same for any-interrupted and any-resumed.
                //TODO: combine this if statement with the one above
                if (!anyTransitionText.isEmpty()) {
                    switch (transitionContent.get().getType()) {
                        case INTERRUPT:
                            head.addAll(anyTransitionText);
                            break;
                        case RESUME:
                            // Adding to the top of the list isn't very efficient.
                            // When combined with the if statement above, this isn't necessary.
                            head.addAll(0, anyTransitionText);
                            firstPageContentRow += anyTransitionText.size();
                            break;
                    }
                }
                //TODO: Get last row
                if (!head.isEmpty()) {
                    int s = head.size();
                    RowGroup gr = head.get(s - 1);
                    cbl = gr.getLineProperties().getBlockLineLocation();
                }
                // Add the body rows to the page.
                if (firstPageContentRow > 0) {
                    addRows(head.subList(0, firstPageContentRow), current, false);
                    if (!current.getDetails().getMarkers().isEmpty()) {
                        // this means that there were markers in any-resumed or sequence-resumed,
                        // which is disallowed by the OBFL parser (so should in theory not happen)
                        throw new RuntimeException("Markers in any-resumed or sequence-resumed are not supported");
                    }
                    addRows(head.subList(firstPageContentRow, head.size()), current, true);
                } else {
                    addRows(head, current, false);
                }
                // The VolumeKeepPriority of the page is the maximum value (lowest priority) of all
                // RowGroups. Discarded RowGroups (i.e. collapsed margins) are also taken into
                // account.
                current.setAvoidVolumeBreakAfter(
                    getVolumeKeepPriority(
                        res.getDiscarded(),
                        getVolumeKeepPriority(res.getHead(), VolumeKeepPriority.empty())
                    )
                );
                // Store info about this volume transition for use in the next iteration (used in SheetDataSource).
                // No need to do this unless there is an active transition builder.
                if (interruptContentPresent) {
                    // Determine whether there is a block boundary on the page, with enough space
                    // available after this point for sequence-interrupted and any-interrupted.
                    boolean hasBlockBoundary =
                        blockBoundary.isPresent() ?
                        blockBoundary.get() :
                        res.getHead().stream().filter(r -> r.isLastRowGroupInBlock()).findFirst().isPresent();
                    getContext().getRefs().keepTransitionProperties(
                        current.getDetails().getPageLocation(),
                        new TransitionProperties(
                            current.getAvoidVolumeBreakAfter(),
                            hasBlockBoundary
                        )
                    );
                }
                // Discard collapsed margins, but retain their properties (identifiers, markers,
                // keep-with-next-sheets, keep-with-previous-sheets).
                for (RowGroup rg : res.getDiscarded()) {
                    addProperties(current, rg);
                }
                // If the space needed for the footnotes exceeds max-height, we need to use the
                // fallback. This will result in a RestartPaginationException, i.e. the pagination
                // should be restarted from the beginning (start of current iteration).
                if (hasPageAreaCollection() && current.pageAreaSpaceNeeded() > master.getPageArea().getMaxHeight()) {
                    reassignCollection();
                }
                // We are finished with the page if either the block was split (tail not empty), or
                // if the next block has break-before="page" or break-before="sheet". In case of
                // break-before="sheet" we insert an empty page if needed. If there is no next block
                // we are also finished (the while loop will end).
                if (!data.isEmpty()) {
                    if (current.hasRows()) {
                        return current;
                    }
                } else if (current != null && dataGroupsIndex < dataGroups.size()) {
                    BreakBefore nextStart = dataGroups.get(dataGroupsIndex).getBreakBefore();
                    switch (nextStart) {
                        case SHEET:
                            // next row starts on new sheet
                            if (master.duplex()) {
                                if (pageCount % 2 == 1) { // we are on a recto page
                                    if (current.hasRows()) {
                                        // indicate that the next page (the verso page) should be empty
                                        nextEmpty = true;
                                        return current;
                                    } else {
                                        break; // we are already at the beginning of a recto page
                                    }
                                } else { // we are on a verso page
                                    return current;
                                }
                            }

                            // Copied from the statement below to ensure that it is clear that we have a fall
                            // through behavior, this kind of programing could be error prone because the unclear
                            // case could introduce unwanted behavior.
                            if (current.hasRows()) {
                                return current;
                            }
                            break; // we are already at the beginning of a page

                        case PAGE:
                            // next row starts on new page
                            if (current.hasRows()) {
                                return current;
                            }
                            break; // we are already at the beginning of a page
                        case AUTO:
                        default:
                    }
                }
            }
        }

        return current;
    }

    /**
     * Returns true if there is a block boundary before or at the specified limit.
     *
     * @param groups the data
     * @param limit  the size limit
     * @return true if there is a block boundary within the limit
     */
    private static boolean hasBlockInScope(List<RowGroup> groups, double limit) {
        // TODO: In Java 9, use takeWhile
        //return groups.stream().limit((int)Math.ceil(limit))
        // .filter(r->r.isLastRowGroupInBlock()).findFirst().isPresent();
        double h = 0;
        Iterator<RowGroup> rg = groups.iterator();
        RowGroup r;
        while (rg.hasNext()) {
            r = rg.next();
            h += rg.hasNext() ? r.getUnitSize() : r.getLastUnitSize();
            if (h > limit) {
                // we've passed the limit
                return false;
            } else if (r.isLastRowGroupInBlock()) {
                return true;
            }
        }
        return false;
    }

    private static float height(List<RowGroup> rg, boolean useLastUnitSize) {
        if (rg.isEmpty()) {
            return 0;
        } else {
            float ret = 0;
            Iterator<RowGroup> ri = rg.iterator();
            while (ri.hasNext()) {
                RowGroup r = ri.next();
                ret += useLastUnitSize && !ri.hasNext() ? r.getLastUnitSize() : r.getUnitSize();
            }
            return ret;
        }
    }

    /**
     * @param resetPageContent Whether to reset the start of the page-content scope of page
     *        <code>p</code> to the first row from <code>head</code>.
     */
    private void addRows(List<RowGroup> head, PageImpl p, boolean resetPageContent) {
        int i = head.size();
        for (RowGroup rg : head) {
            i--;
            addProperties(p, rg);

            List<RowImpl> rows = rg.getRows();
            int j = rows.size();
            for (RowImpl r : rows) {
                if (resetPageContent) {
                    p.getDetails().startsContentMarkers();
                    resetPageContent = false;
                }
                j--;
                if ((i == 0 && j == 0)) {
                    // clone the row as not to append the margins twice
                    RowImpl.Builder b = new RowImpl.Builder(r);
                    // this is the last row; set row spacing to 1 because this is how sph treated it
                    b.rowSpacing(null);
                    p.newRow(b.build());
                } else {
                    p.newRow(r);
                }
            }
        }
    }

    private VolumeKeepPriority getVolumeKeepPriority(List<RowGroup> list, VolumeKeepPriority def) {
        if (!list.isEmpty()) {
            if (context.getTransitionBuilder().getProperties().getApplicationRange() == ApplicationRange.NONE) {
                return list.get(list.size() - 1).getAvoidVolumeBreakAfterPriority();
            } else {
                // We want the lowest priority to maximize the chance that this page is used when
                // finding the break point.
                return list.stream().map(v -> v.getAvoidVolumeBreakAfterPriority())
                    .min(VolumeKeepPriority.naturalOrder())
                    .orElse(VolumeKeepPriority.empty());
            }
        } else {
            return def;
        }
    }

    private boolean firstUnitHasSupplements(SplitPointDataSource<?, ?> spd) {
        return !spd.isEmpty() && !spd.get(0).getSupplementaryIDs().isEmpty();
    }

    private boolean hasPageAreaCollection() {
        return master.getPageArea() != null && collection != null;
    }

    private void addProperties(PageImpl p, RowGroup rg) {
        p.addIdentifiers(rg);
        p.addMarkers(rg);
        keepNextSheets = Math.max(rg.getKeepWithNextSheets(), keepNextSheets);
        if (keepNextSheets > 0) {
            p.setAllowsVolumeBreak(false);
        }
        p.setKeepWithPreviousSheets(rg.getKeepWithPreviousSheets());
    }

    private void reassignCollection() throws PaginatorException {
        //reassign collection
        if (areaProps != null) {
            int i = 0;
            for (FallbackRule r : areaProps.getFallbackRules()) {
                i++;
                if (r instanceof RenameFallbackRule) {
                    ContentCollectionImpl reassigned = context.getCollections().remove(r.applyToCollection());
                    if (context.getCollections().put(((RenameFallbackRule) r).getToCollection(), reassigned) != null) {
                        throw new PaginatorException(
                            "Fallback id already in use:" + ((RenameFallbackRule) r).getToCollection()
                        );
                    }
                } else {
                    throw new PaginatorException("Unknown fallback rule: " + r);
                }
            }
            if (i == 0) {
                throw new PaginatorException(
                    "Failed to fit collection '" + areaProps.getCollectionId() +
                    "' within the page-area boundaries, and no fallback was defined."
                );
            }
        }
        throw new RestartPaginationException();
    }

    static class CollectionData implements Supplements<RowGroup> {
        private final BlockContext c;
        private final double overhead;
        private final LayoutMaster master;
        private final ContentCollectionImpl collection;
        private double extraOverhead = 0;

        private CollectionData(
                PageAreaContent staticAreaContent,
                BlockContext c,
                LayoutMaster master,
                ContentCollectionImpl collection
        ) {
            this.c = c;
            this.master = master;
            this.collection = collection;

            this.overhead = PageImpl.rowsNeeded(staticAreaContent.getBefore(), master.getRowSpacing()) +
                PageImpl.rowsNeeded(staticAreaContent.getAfter(), master.getRowSpacing());
        }

        private void setExtraOverhead(double extra) {
            this.extraOverhead = extra;
        }

        @Override
        public double getOverhead() {
            return overhead + extraOverhead;
        }

        @Override
        public RowGroup get(String id) {
            if (collection != null) {
                RowGroup.Builder b = new RowGroup.Builder(master.getRowSpacing()).mergeable(false);
                for (Block g : collection.getBlocks(id)) {
                    AbstractBlockContentManager bcm = g.getBlockContentManager(c);
                    b.addAll(bcm.getCollapsiblePreContentRows());
                    b.addAll(bcm.getInnerPreContentRows());
                    Optional<RowImpl> r;
                    while ((r = bcm.getNext()).isPresent()) {
                        b.add(r.get());
                    }
                    b.addAll(bcm.getPostContentRows());
                    b.addAll(bcm.getSkippablePostContentRows());
                }
                return b.build();
            } else {
                return null;
            }
        }

    }

    private int calculateVerticalSpace(PageImpl pa, BlockPosition p, int blockSpace) {
        if (p != null) {
            int pos = p.getPosition().makeAbsolute(pa.getFlowHeight());
            int t = pos - (int) Math.ceil(pa.currentPosition());
            if (t > 0) {
                int advance = 0;
                switch (p.getAlignment()) {
                    case BEFORE:
                        advance = t - blockSpace;
                        break;
                    case CENTER:
                        advance = t - blockSpace / 2;
                        break;
                    case AFTER:
                        advance = t;
                        break;
                }
                return (int) Math.floor(advance / master.getRowSpacing());
            }
        }
        return 0;
    }

    public int getSizeLast() {
        return getSizeLast(fromIndex);
    }

    /**
     * Returns the number of supplied pages since <code>fromIndex</code> ({@link #getToIndex()} -
     * <code>fromIndex</code>), rounded to an even number if duplex, i.e. in the case of duplex it
     * represents twice the number of sheets needed to contain the supplied pages since
     * <code>fromIndex</code> (assuming that the first page after <code>fromIndex</code> starts on
     * the front side of the first sheet).
     *
     * @param fromIndex a page index
     * @return a number of pages
     */
    public int getSizeLast(int fromIndex) {
        int size = getToIndex() - fromIndex;
        if (master.duplex() && (size % 2) == 1) {
            return size + 1;
        } else {
            return size;
        }
    }

    /**
     * Returns the number of supplied pages.
     *
     * @return a number of pages
     */
    public int size() {
        return getToIndex() - fromIndex;
    }

    /**
     * Returns the provided value of <code>fromIndex</code> in {@link #PageSequenceBuilder2(int,
     * LayoutMaster, int, BlockSequence, FormatterContext, DefaultContext, SequenceId,
     * BlockLineLocation)}.
     *
     * @return a page index
     */
    public int getGlobalStartIndex() {
        return fromIndex;
    }

    /**
     * Returns the index of the page that will be (or "would" be) supplied next, provided that
     * {@link #getGlobalStartIndex()} is the index of the first page minus 1.
     *
     * @return a page index
     */
    public int getToIndex() {
        return toIndex;
    }

}
