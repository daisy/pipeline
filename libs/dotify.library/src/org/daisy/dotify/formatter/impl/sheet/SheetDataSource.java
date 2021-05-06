package org.daisy.dotify.formatter.impl.sheet;

import org.daisy.dotify.api.formatter.TransitionBuilderProperties.ApplicationRange;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.splitter.DefaultSplitResult;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitResult;
import org.daisy.dotify.common.splitter.Supplements;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.TransitionContent;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.PageImpl;
import org.daisy.dotify.formatter.impl.page.PageSequenceBuilder2;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.BlockLineLocation;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.DocumentSpace;
import org.daisy.dotify.formatter.impl.search.SequenceId;
import org.daisy.dotify.formatter.impl.search.SheetIdentity;
import org.daisy.dotify.formatter.impl.search.TransitionProperties;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Provides a data source for {@link Sheet}s. Given a list of {@link BlockSequence}s, sheets are
 * produced one by one.
 *
 * <p>The input is:</p>
 * <ul>
 *   <li>The list of {@link BlockSequence}s contained in a volume group or in the pre- or post-content
 *       of a volume. A volume group is a set of block sequences starting with a hard volume break
 *       (<code>break-before="volume"</code>).</li>
 *   <li>A {@link PageCounter}</li>
 *   <li>The index of the volume group (0-based) or null if the input comes from pre- or post-content.</li>
 * </ul>
 *
 * <p>The computed {@link VolumeKeepPriority} of a sheet is the priority of the back side page, or
 * the priority of the front side page if that value is higher (lower priority) and if
 * <code>&lt;volume-transition range="sheet"/&gt;</code>.</p>
 *
 * @author Joel Håkansson
 */
public class SheetDataSource implements SplitPointDataSource<Sheet, SheetDataSource> {
    //Global state
    private final PageCounter pageCounter;
    private final FormatterContext context;
    //Input data
    private DefaultContext rcontext;
    private final Integer volumeGroup;
    private final List<BlockSequence> seqsIterator;
    //Local state
    private int seqsIndex;
    private SequenceId seqId;
    private PageSequenceBuilder2 psb;
    private int psbCurStartIndex; // index of first page of current psb in current volume
    private SectionProperties sectionProperties;
    private int sheetIndex; // sheets created from current sequence or being created
    private Deque<SheetIdentity> previousSheets; // all sheets created from the current sequence
    private int pageIndex;
    private String counter;
    private int initialPageOffset;
    private boolean volBreakAllowed;
    private boolean updateCounter;
    private boolean allowsSplit;
    private boolean isFirst;
    private boolean wasSplitInsideSequence;
    private boolean volumeEnded;
    private List<Sheet> sheetBuffer; // output buffer

    public SheetDataSource(
        PageCounter pageCounter,
        FormatterContext context,
        DefaultContext rcontext,
        Integer volumeGroup,
        List<BlockSequence> seqsIterator
    ) {
        this.pageCounter = pageCounter;
        this.context = context;
        this.rcontext = rcontext;
        this.volumeGroup = volumeGroup;
        this.seqsIterator = seqsIterator;
        this.sheetBuffer = new ArrayList<>();
        this.volBreakAllowed = true;
        this.seqsIndex = 0;
        this.seqId = null;
        this.psb = null;
        this.psbCurStartIndex = 0;
        this.sectionProperties = null;
        this.sheetIndex = 0;
        this.previousSheets = new LinkedList<>();
        this.pageIndex = 0;
        this.counter = null;
        this.initialPageOffset = 0;
        this.updateCounter = false;
        this.allowsSplit = true;
        this.isFirst = true;
        this.wasSplitInsideSequence = false;
        this.volumeEnded = false;
    }

    public SheetDataSource(SheetDataSource template) {
        this(template, 0, false);
    }

    /**
     * Creates a new instance with the specified data source as template.
     *
     * @param template the template
     * @param offset   the sheetBuffer offset (items before the offset are discarded)
     * @param tail     true if the purpose of this instance is to become the template's
     *                 tail. This information is required because the page number counter
     *                 must be the same instance in this case. The reverse is true in other
     *                 cases.
     */
    private SheetDataSource(SheetDataSource template, int offset, boolean tail) {
        this.pageCounter = tail ? template.pageCounter : new PageCounter(template.pageCounter);
        this.context = template.context;
        this.rcontext = template.rcontext;
        this.volumeGroup = template.volumeGroup;
        this.seqsIterator = template.seqsIterator;
        this.seqsIndex = template.seqsIndex;
        this.seqId = template.seqId;
        this.psb = tail ? template.psb : PageSequenceBuilder2.copyUnlessNull(template.psb);
        this.psbCurStartIndex = template.psbCurStartIndex;
        this.sectionProperties = template.sectionProperties;
        this.sheetIndex = template.sheetIndex;
        this.pageIndex = template.pageIndex;
        this.previousSheets = new LinkedList<>(template.previousSheets);
        if (template.sheetBuffer.size() > offset) {
            this.sheetBuffer = new ArrayList<>(template.sheetBuffer.subList(offset, template.sheetBuffer.size()));
        } else {
            this.sheetBuffer = new ArrayList<>();
        }
        this.volBreakAllowed = template.volBreakAllowed;
        this.counter = template.counter;
        this.initialPageOffset = template.initialPageOffset;
        this.updateCounter = tail || template.updateCounter;
        this.allowsSplit = true;
        this.isFirst = template.isFirst;
        this.wasSplitInsideSequence = template.wasSplitInsideSequence;
        this.volumeEnded = false;
    }

    @Override
    public Sheet get(int index) throws RestartPaginationException {
        if (!ensureBuffer(index + 1)) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return sheetBuffer.get(index);
    }

    @Override
    public List<Sheet> getRemaining() throws RestartPaginationException {
        ensureBuffer(-1);
        return sheetBuffer;
    }

    @Override
    public boolean hasElementAt(int index) throws RestartPaginationException {
        return ensureBuffer(index + 1);
    }

    @Override
    public int getSize(int limit) throws RestartPaginationException {
        if (!ensureBuffer(limit - 1)) {
            //we have buffered all elements
            return sheetBuffer.size();
        } else {
            return limit;
        }
    }

    @Override
    public boolean isEmpty() {
        return seqsIndex >= seqsIterator.size() && sheetBuffer.isEmpty() && (psb == null || !psb.hasNext());
    }

    @Override
    public Supplements<Sheet> getSupplements() {
        return null;
    }

    public void setCurrentVolumeNumber(int volume) {
        rcontext = DefaultContext.from(rcontext).currentVolume(volume).build();
        if (psb != null) {
            psb.setCurrentVolumeNumber(volume);
        }
    }

    /**
     * Ensures that there are at least index elements in the buffer.
     * When index is -1 this method always returns false.
     *
     * @param index the index (or -1 to get all remaining elements)
     * @return returns true if the index element was available, false otherwise
     */
    private boolean ensureBuffer(int index) {
        Sheet.Builder s = null; // sheet currently being built
        SheetIdentity si = null; // sheet currently being built
        while (index < 0 || sheetBuffer.size() < index) {
            // this happens when a new volume is started
            if (updateCounter) {
                if (counter != null) {
                    initialPageOffset = rcontext.getRefs().getPageNumberOffset(counter) - psb.size();
                } else {
                    initialPageOffset = pageCounter.getDefaultPageOffset() - psb.size();
                }
                // psbCurStartIndex (index of first page of current psb in current volume)
                // is changed because we started a new volume
                psbCurStartIndex = psb.getToIndex();
                updateCounter = false;
            }
            if (psb == null || !psb.hasNext()) {
                if (s != null) {
                    //Last page in the sequence doesn't need volume keep priority
                    sheetBuffer.add(s.build());
                    previousSheets.push(si);
                    s = null;
                    continue;
                }
                if (seqsIndex >= seqsIterator.size()) {
                    // cannot ensure buffer, return false
                    return false;
                }
                // init new sequence
                BlockSequence bs = seqsIterator.get(seqsIndex);
                seqsIndex++;
                counter = bs.getSequenceProperties().getPageCounterName().orElse(null);
                if (bs.getInitialPageNumber() != null) {
                    initialPageOffset = bs.getInitialPageNumber() - 1;
                } else if (counter != null) {
                    initialPageOffset = Optional
                        .ofNullable(rcontext.getRefs().getPageNumberOffset(counter))
                        .orElse(0);
                } else {
                    initialPageOffset = pageCounter.getDefaultPageOffset();
                }
                seqId = new SequenceId(
                    seqsIndex,
                    new DocumentSpace(rcontext.getSpace(), rcontext.getCurrentVolume()),
                    volumeGroup
                );
                psbCurStartIndex = pageCounter.getPageCount();
                psb = new PageSequenceBuilder2(
                    psbCurStartIndex,
                    bs.getLayoutMaster(),
                    initialPageOffset,
                    bs,
                    context,
                    rcontext,
                    seqId,
                    psb != null ? psb.currentBlockLineLocation() : null
                );
                sectionProperties = bs.getLayoutMaster().newSectionProperties();
                s = null;
                si = null;
                sheetIndex = 0;
                pageIndex = 0;
                if (!previousSheets.isEmpty()) {
                    previousSheets = new LinkedList<>();
                }
            }
            int currentSize = sheetBuffer.size();
            while (psb.hasNext() && currentSize == sheetBuffer.size()) {
                if (!sectionProperties.duplex() || pageIndex % 2 == 0 || volumeEnded || s == null) {
                    if (s != null) {
                        sheetBuffer.add(s.build());
                        previousSheets.push(si);
                        s = null;
                        if (volumeEnded) {
                            pageIndex += pageIndex % 2 == 1 ? 1 : 0;
                        }
                        continue;
                    } else if (volumeEnded) {
                        throw new AssertionError("Error in code.");
                    }
                    volBreakAllowed = true;
                    s = new Sheet.Builder(sectionProperties);
                    si = new SheetIdentity(
                        psb.currentBlockLineLocation(),
                        rcontext.getSpace(),
                        rcontext.getCurrentVolume(),
                        volumeGroup
                    );
                    sheetIndex++;
                }

                TransitionContent transition = null;
                if (context.getTransitionBuilder().getProperties().getApplicationRange() != ApplicationRange.NONE) {
                    if (!allowsSplit && index - 1 == sheetBuffer.size()) {
                        if ((!sectionProperties.duplex() || pageIndex % 2 == 1)) {
                            transition = context.getTransitionBuilder().getInterruptTransition();
                        } else if (
                            context.getTransitionBuilder()
                                    .getProperties().getApplicationRange() == ApplicationRange.SHEET
                        ) {
                            // This id is the same id as the one created below in the call to nextPage
                            BlockLineLocation thisPageId = psb.currentBlockLineLocation();
                            // This gets the page location for the next page in this sequence (if any)
                            Optional<BlockLineLocation> nextPageId = thisPageId != null
                                ? rcontext.getRefs().getNextPageLocationInSequence(thisPageId)
                                : Optional.empty();
                            if (nextPageId.isPresent()) {
                                // there is a next page in this sequence and a volume break is preferred on this page
                                Optional<TransitionProperties> st1 = rcontext.getRefs().getTransitionProperties(
                                    thisPageId
                                );
                                TransitionProperties p = st1.orElse(TransitionProperties.empty());
                                double v1 = p.getVolumeKeepPriority()
                                    .orElse(10) + (p.hasBlockBoundary() ? 0.5 : 0);
                                Optional<TransitionProperties> st2 = rcontext.getRefs().getTransitionProperties(
                                    nextPageId.get()
                                );
                                p = st2.orElse(TransitionProperties.empty());
                                double v2 = p.getVolumeKeepPriority()
                                    .orElse(10) + (p.hasBlockBoundary() ? 0.5 : 0);

                                if (v1 > v2 || !st1.isPresent()) {
                                    // this page is preferable to break on
                                    // or, if st1 isn't present the lookup has been marked as dirty. This will cause
                                    // a value to be recorded for this position
                                    // (st1 will be present for the next iteration)
                                    // and there will be at least one more iteration to get it right.

                                    //break here
                                    transition = context.getTransitionBuilder().getInterruptTransition();
                                }
                            }
                        }
                        volumeEnded = transition != null;
                    } else if (sheetBuffer.size() == 0 && (!sectionProperties.duplex() || pageIndex % 2 == 0)) {
                        transition = context.getTransitionBuilder().getResumeTransition();
                    }
                }
                // The last line may be hyphenated when
                // - the configuration allows it, or
                // - we are not on the last sheet of the volume, or
                // - we are in duplex mode and the current page index is even
                //   (so we're not on the last page of the sheet)
                boolean hyphenateLastLine =
                        context.getConfiguration().allowsEndingVolumeOnHyphen()
                                || sheetBuffer.size() != index - 1
                                || (sectionProperties.duplex() && pageIndex % 2 == 0);

                PageImpl p = psb.nextPage(
                    initialPageOffset,
                    hyphenateLastLine,
                    Optional.ofNullable(transition),
                    wasSplitInsideSequence,
                    isFirst
                );
                pageCounter.increasePageCount();
                VolumeKeepPriority vpx = p.getAvoidVolumeBreakAfter();
                if (context.getTransitionBuilder().getProperties().getApplicationRange() == ApplicationRange.SHEET) {
                    Sheet sx = s.build();
                    if (!sx.getPages().isEmpty()) {
                        VolumeKeepPriority vp = sx.getAvoidVolumeBreakAfterPriority();
                        if (vp.orElse(10) > vpx.orElse(10)) {
                            vpx = vp;
                        }
                    }
                }
                s.avoidVolumeBreakAfterPriority(vpx);
                if (!psb.hasNext()) {
                    s.avoidVolumeBreakAfterPriority(VolumeKeepPriority.empty());
                    //Don't get or store this value in crh as it is transient and not a property of the sheet context
                    s.breakable(true);
                } else {
                    boolean br = rcontext.getRefs().getBreakable(si);
                    //TODO: the following is a low effort way of giving existing uses of non-breakable units
                    //      a high priority, but it probably shouldn't be done this way
                    if (!br) {
                        s.avoidVolumeBreakAfterPriority(VolumeKeepPriority.of(1));
                    }
                    s.breakable(br);
                }
                keepWithPreviousSheets(previousSheets, p.keepPreviousSheets(), rcontext);
                volBreakAllowed &= p.allowsVolumeBreak();
                if (!sectionProperties.duplex() || pageIndex % 2 == 1 || volumeEnded) {
                    rcontext.getRefs().keepBreakable(si, volBreakAllowed);
                }
                s.add(p);
                pageIndex++;
            }
            if (!psb.hasNext() || volumeEnded) {
                if (!psb.hasNext()) {
                    rcontext.getRefs().setSequenceScope(seqId, psb.getGlobalStartIndex(), psb.getToIndex());
                }

                // page number of the last page returned by psb
                int lastPageNumber = initialPageOffset;      // page number corresponding to the first page
                                                             // returned by psb, minus 1

                // index of first page of psb in current volume
                lastPageNumber += psbCurStartIndex;

                // value of psbCurStartIndex when psb was created
                lastPageNumber -= psb.getGlobalStartIndex();

                // number of supplied pages since psbCurStartIndex, rounded to an even number if duplex
                lastPageNumber += psb.getSizeLast(psbCurStartIndex);

                if (counter != null) {
                    rcontext.getRefs().setPageNumberOffset(counter, lastPageNumber);
                } else {
                    pageCounter.setDefaultPageOffset(lastPageNumber);
                }
            }
        }
        return true;
    }

    /**
     * Keep the sheet that is currently being built together with the <code>n</code> previous
     * sheets.
     *
     * @param previousSheets All the sheets previously created from the current block sequence,
     *                       starting with the most recent one (the one before the sheet we are
     *                       currently building).
     * @param n              The number of sheets to keep together, excluding the one currently
     *                       being built.
     * @param rcontext       The current {@link DefaultContext}.
     */
    private static void keepWithPreviousSheets(Iterable<SheetIdentity> previousSheets, int n, DefaultContext rcontext) {
        for (SheetIdentity s : previousSheets) {
            if (n > 0) {
                rcontext.getRefs().keepBreakable(s, false);
                n--;
            } else {
                break;
            }
        }
    }

    @Override
    public SplitResult<Sheet, SheetDataSource> split(int atIndex) {
        if (!allowsSplit) {
            throw new IllegalStateException();
        }
        allowsSplit = false;
        return SplitPointDataSource.super.split(atIndex);
    }

    // this happens when a new volume is started
    @Override
    public SplitResult<Sheet, SheetDataSource> splitInRange(int atIndex) {
        if (!ensureBuffer(atIndex)) {
            throw new IndexOutOfBoundsException("" + atIndex);
        }
        int lastPageNumber = initialPageOffset +
                psbCurStartIndex -
                psb.getGlobalStartIndex() +
                psb.getSizeLast(psbCurStartIndex);

        if (counter != null) {
            rcontext.getRefs().setPageNumberOffset(counter, lastPageNumber);
        } else {
            pageCounter.setDefaultPageOffset(lastPageNumber);
        }
        wasSplitInsideSequence = psb.hasNext();
        isFirst = false;
        if (atIndex == 0) {
            return new DefaultSplitResult<Sheet, SheetDataSource>(
                Collections.emptyList(),
                new SheetDataSource(this, atIndex, true)
            );
        } else {
            return new DefaultSplitResult<Sheet, SheetDataSource>(
                sheetBuffer.subList(0, atIndex),
                new SheetDataSource(this, atIndex, true)
            );
        }
    }

    @Override
    public SheetDataSource createEmpty() {
        return new SheetDataSource(pageCounter, context, rcontext, volumeGroup, Collections.emptyList());
    }

    @Override
    public SheetDataSource getDataSource() {
        return this;
    }

}
