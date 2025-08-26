package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.SequenceProperties.SequenceBreakBefore;
import org.daisy.dotify.common.splitter.SplitPoint;
import org.daisy.dotify.common.splitter.SplitPointCost;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitPointHandler;
import org.daisy.dotify.common.splitter.SplitPointSpecification;
import org.daisy.dotify.common.splitter.StandardSplitOption;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.PageImpl;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.AnchorData;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.Space;
import org.daisy.dotify.formatter.impl.search.VolumeData;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;
import org.daisy.dotify.formatter.impl.sheet.PageCounter;
import org.daisy.dotify.formatter.impl.sheet.SectionBuilder;
import org.daisy.dotify.formatter.impl.sheet.Sheet;
import org.daisy.dotify.formatter.impl.sheet.SheetDataSource;
import org.daisy.dotify.formatter.impl.sheet.SheetGroup;
import org.daisy.dotify.formatter.impl.sheet.SheetGroupManager;
import org.daisy.dotify.formatter.impl.sheet.SplitterLimit;
import org.daisy.dotify.formatter.impl.sheet.VolumeImpl;
import org.daisy.dotify.formatter.impl.volume.VolumeSequence;
import org.daisy.dotify.formatter.impl.volume.VolumeTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Given a list of {@link BlockSequence}s, produces {@link
 * org.daisy.dotify.formatter.impl.common.Volume} objects one by one. The volumes are obtained
 * through a {@link #nextVolume() "iterator" interface}.</p>
 *
 * <p>The input is a list of {@link BlockSequence}s, which are first converted to a sequence of
 * "volume groups", where every group is a list of {@link BlockSequence}s, of which the first one
 * has a hard volume break (<code>break-before="volume"</code>). For every volume group a {@link
 * SheetDataSource} is then created, which is wrapped in a {@link SheetGroup}. All groups are
 * managed in a {@link SheetGroupManager}. {@link SheetDataSource}s that do not fit in a volume are
 * broken, using {@link SplitPointHandler}. The cost function takes into account how much the total
 * size of a volume deviates from the target size, the <code>volume-break-priority</code> of the
 * last sheet, and whether the {@link Sheet#isBreakable() isBreakable} constraint of the last sheet
 * is violated.</p>
 *
 * <p>Pre- and post-content is added to every volume based on the provided {@link
 * VolumeTemplate}s.</p>
 *
 * <p>One {@link PageCounter} is created for the body of the whole document, and one for every pre-
 * or post-content of every volume.</p>
 *
 * @author Joel Håkansson
 */
public class VolumeProvider {
    private static final Logger logger = Logger.getLogger(VolumeProvider.class.getCanonicalName());
    private static final int DEFAULT_SPLITTER_MAX = 50;
    private final List<BlockSequence> blocks;
    private final CrossReferenceHandler crh;
    private SheetGroupManager groups;
    private final SplitPointHandler<Sheet, SheetDataSource> volSplitter;

    private int pageIndex = 0;
    private int currentVolumeNumber = 0;
    private boolean init = false;
    private int j = 1;

    private final SplitterLimit splitterLimit;
    private final Stack<VolumeTemplate> volumeTemplates;
    private final LazyFormatterContext context;

    /**
     * Creates a new volume provider with the specifed parameters.
     *
     * @param blocks          the block sequences
     * @param volumeTemplates volume templates
     * @param context         the formatter context
     */
    VolumeProvider(List<BlockSequence> blocks, Stack<VolumeTemplate> volumeTemplates, LazyFormatterContext context) {
        this.blocks = blocks;
        this.crh = new CrossReferenceHandler();
        this.splitterLimit = volumeNumber -> {
            final DefaultContext c = new DefaultContext.Builder(crh)
                    .currentVolume(volumeNumber)
                    .build();
            Optional<VolumeTemplate> ot = volumeTemplates.stream().filter(t -> t.appliesTo(c)).findFirst();
            if (ot.isPresent()) {
                return ot.get().getVolumeMaxSize();
            } else {
                logger.fine("Found no applicable volume template.");
                return DEFAULT_SPLITTER_MAX;
            }
        };
        this.volumeTemplates = volumeTemplates;
        this.context = context;
        this.volSplitter = new SplitPointHandler<>();
    }

    /**
     * Resets the volume provider to its initial state (with some information preserved).
     *
     * @throws RestartPaginationException
     */
    void prepare() {
        if (!init) {
            groups = new SheetGroupManager();
            // make a preliminary calculation based on a contents only
            Iterable<SheetDataSource> allUnits = prepareToPaginateWithVolumeGroups(
                blocks,
                new DefaultContext.Builder(crh).space(Space.BODY).build()
            );
            int volCount = 0; // initial estimate of volume count
            for (SheetDataSource data : allUnits) {
                groups.add().setUnits(data);
                volCount += (1 + (data.getRemaining().size() - 1)
                                 / splitterLimit.getSplitterLimit(volCount + 1));
            }
            crh.setVolumeCount(volCount);
            //if there is an error, we won't have a proper initialization and have to retry from the beginning
            init = true;
        }
        Iterable<SheetDataSource> allUnits = prepareToPaginateWithVolumeGroups(
            blocks,
            new DefaultContext.Builder(crh).space(Space.BODY).build()
        );
        int i = 0;
        for (SheetDataSource unit : allUnits) {
            groups.atIndex(i).setUnits(unit);
            i++;
        }
        pageIndex = 0;
        currentVolumeNumber = 0;

        groups.resetAll();
    }

    /**
     * @return returns the next volume
     * @throws RestartPaginationException if pagination should be restarted
     */
    VolumeImpl nextVolume() {
        currentVolumeNumber++;
        VolumeImpl volume = new VolumeImpl(crh.getOverhead(currentVolumeNumber));
        List<AnchorData> ad = new ArrayList<>();
        volume.setPreVolData(updateVolumeContents(currentVolumeNumber, ad, true));
        volume.setBody(nextBodyContents(currentVolumeNumber, volume.getOverhead().total(), ad));
        volume.setPostVolData(updateVolumeContents(currentVolumeNumber, ad, false));
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Sheets in volume " + currentVolumeNumber + ": " + (volume.getVolumeSize()) +
                    " (content:" + volume.getBodySize() +
                    ", overhead (allocated):" + volume.getOverhead().total() + ")");
        }
        crh.setSheetsInVolume(currentVolumeNumber, volume.getVolumeSize());
        //crh.setPagesInVolume(i, value);
        crh.setAnchorData(currentVolumeNumber, ad);
        crh.setOverhead(currentVolumeNumber, volume.getOverhead());
        return volume;

    }

    /**
     * Gets the contents of the next volume.
     *
     * @param overhead the number of sheets in this volume that's not part of the main body of text
     * @param ad       the anchor data
     * @return returns the contents of the next volume
     */
    private SectionBuilder nextBodyContents(int volumeNumber, final int overhead, List<AnchorData> ad) {
        int pagesPerSheet = context.getFormatterContext().getConfiguration().getPagesPerSheet();
        groups.currentGroup().setOverheadCount(groups.currentGroup().getOverheadCount() + overhead);
        final int targetSheetsInVolume = pagesPerSheet > 2
            // round to previous multiple of pagesPerSheet/2, because a higher target would automatically
            // result in actual values that exceed the limit
            ? splitterLimit.getSplitterLimit(volumeNumber) * 2 / pagesPerSheet * pagesPerSheet / 2
            : splitterLimit.getSplitterLimit(volumeNumber);
        //Not using lambda for now, because it's noticeably slower.
        SplitPointCost<Sheet> cost = new SplitPointCost<Sheet>() {
            @Override
            public double getCost(SplitPointDataSource<Sheet, ?> units, int index, int breakpoint) {
                int contentSheetTarget = targetSheetsInVolume - overhead;
                Sheet lastSheet = units.get(index);
                double priorityPenalty = 0;
                int sheetCount = index + 1;
                // Calculates a maximum offset based on the maximum possible number of sheets
                double range = targetSheetsInVolume * 0.4;
                if (!units.isEmpty()) {
                    VolumeKeepPriority avoid = lastSheet.getAvoidVolumeBreakAfterPriority();
                    if (avoid.hasValue()) {
                        // Reverses 1-9 to 9-1 with bounds control and normalizes that to [1/9, 1]
                        double normalized = ((10 - avoid.getValue()) / 9d);
                        // Calculates a number of sheets that a high priority can beat
                        priorityPenalty = range * normalized;
                    }
                }
                // sets the preferred value to targetSheetsInVolume, where cost will be 0
                // including a small preference for bigger volumes
                double distancePenalty = Math.abs(contentSheetTarget - sheetCount) +
                        (contentSheetTarget - sheetCount) * 0.001;
                // also take into account empty pages on the last sheet (only in case a sheet can
                // hold more than 2 pages)
                if (pagesPerSheet > 2) {
                    distancePenalty += 0.5 * ((2 * (sheetCount + overhead)) % pagesPerSheet);
                }
                int unbreakablePenalty = lastSheet.isBreakable() ? 0 : 100;
                return distancePenalty + priorityPenalty + unbreakablePenalty;
            }
        };
        SplitPoint<Sheet, SheetDataSource> sp;

        // The data is consumed two times. Once to find the optimal break point ("find" function),
        // and once to do the actual split at that position ("split" function). We only record the
        // changes made to the CrossReferenceHandler during the second pass. Note that although no
        // changes are made to the CrossReferenceHandler, it can still become dirty if some info
        // that is requested is not available.
        crh.setReadOnly();
        SheetDataSource data = groups.currentGroup().getUnits();
        data.setCurrentVolumeNumber(volumeNumber);
        SheetDataSource copySource = new SheetDataSource(data);
        SplitPointSpecification spec = volSplitter.find(targetSheetsInVolume - overhead,
                copySource,
                cost, StandardSplitOption.ALLOW_FORCE);
        crh.setReadWrite();
        sp = volSplitter.split(spec, data);
        /*
            sp = volSplitter.split(targetSheetsInVolume-overhead,
                    groups.currentGroup().getUnits(),
                    cost, StandardSplitOption.ALLOW_FORCE);
        */
        data = sp.getTail();
        List<Sheet> contents = sp.getHead();
        // pad with empty sheets if needed
        if (pagesPerSheet == 4 && !data.isEmpty()) {
            int emptySheetIndex = 0;
            contents = new ArrayList<>(contents);
            while ((contents.size() + overhead) % 2 > 0) {
                contents.add(data.getEmptySheet(emptySheetIndex++));
            }
            if (emptySheetIndex > 0) {
                data = data.split(emptySheetIndex).tail();
            }
        }
        groups.currentGroup().setUnits(data);
        int pageCount = Sheet.countPages(contents);
        crh.commitPageDetails();
        crh.setVolumeScope(volumeNumber, pageIndex, pageIndex + pageCount);

        pageIndex += pageCount;
        SectionBuilder sb = new SectionBuilder();
        boolean atFirstPageOfContents = true;
        for (Sheet sheet : contents) {
            for (PageImpl p : sheet.getPages()) {
                if (atFirstPageOfContents) {
                    crh.setPageNumberOfFirstContentPageOfVolume(volumeNumber, p.getPageNumber());
                    List<String> contentIdentifiers = p.getContentIdentifiers();
                    for (String id : p.getIdentifiers()) {
                        boolean atStartOfPage = !contentIdentifiers.contains(id);
                        crh.setVolumeData(id, new VolumeData(volumeNumber, atStartOfPage));
                    }
                } else {
                    for (String id : p.getIdentifiers()) {
                        crh.setVolumeData(id, new VolumeData(volumeNumber, false));
                    }
                }
                if (p.getAnchors().size() > 0) {
                    ad.add(new AnchorData(p.getAnchors(), p.getPageNumber()));
                }
                atFirstPageOfContents = false;
            }
            sb.addSheet(sheet);
        }
        groups.currentGroup().setSheetCount(groups.currentGroup().getSheetCount() + contents.size());
        groups.nextVolume();
        return sb;
    }

    private SectionBuilder updateVolumeContents(int volumeNumber, List<AnchorData> ad, boolean pre) {
        DefaultContext c = new DefaultContext.Builder(crh)
                .currentVolume(volumeNumber)
                .space(pre ? Space.PRE_CONTENT : Space.POST_CONTENT)
                .build();
        try {
            List<BlockSequence> ib = new ArrayList<>();
            for (VolumeTemplate t : volumeTemplates) {
                if (t.appliesTo(c)) {
                    for (VolumeSequence seq : (pre ? t.getPreVolumeContent() : t.getPostVolumeContent())) {
                        BlockSequence s = seq.getBlockSequence(context.getFormatterContext(), c, crh);
                        if (s != null) {
                            ib.add(s);
                        }
                    }
                    break;
                }
            }
            List<Sheet> ret = prepareToPaginatePrePostVolumeContent(ib, c).getRemaining();
            SectionBuilder sb = new SectionBuilder();
            for (Sheet ps : ret) {
                for (PageImpl p : ps.getPages()) {
                    for (String id : p.getIdentifiers()) {
                        crh.setVolumeData(id, new VolumeData(volumeNumber, false));
                    }
                    if (p.getAnchors().size() > 0) {
                        ad.add(new AnchorData(p.getAnchors(), p.getPageNumber()));
                    }
                }
                sb.addSheet(ps);
            }
            return sb;
        } catch (PaginatorException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a list of {@link BlockSequence}s from a pre- or post-content to a {@link SheetDataSource}.
     */
    private SheetDataSource prepareToPaginatePrePostVolumeContent(
        List<BlockSequence> fs,
        DefaultContext rcontext
    ) throws PaginatorException {
        return prepareToPaginate(new PageCounter(), rcontext, null, fs);
    }

    /**
     * Process hard volume breaks.
     *
     * <p>Convert a list of {@link BlockSequence}s to a sequence of {@link SheetDataSource}. At
     * every <code>BlockSequence</code> with a hard volume break
     * (<code>break-before="volume"</code>) a new <code>SheetDataSource</code> is started.
     */
    private Iterable<SheetDataSource> prepareToPaginateWithVolumeGroups(
        List<BlockSequence> fs,
        DefaultContext rcontext
    ) {
        List<List<BlockSequence>> volGroups = new ArrayList<>();
        List<BlockSequence> currentGroup = new ArrayList<>();
        volGroups.add(currentGroup);
        for (BlockSequence bs : fs) {
            if (bs.getSequenceProperties().getBreakBeforeType() == SequenceBreakBefore.VOLUME) {
                currentGroup = new ArrayList<>();
                volGroups.add(currentGroup);
            }
            currentGroup.add(bs);
        }
        PageCounter pageCounter = new PageCounter();
        crh.resetUniqueChecks();
        return new Iterable<SheetDataSource>() {
            @Override
            public Iterator<SheetDataSource> iterator() {
                try {
                    return prepareToPaginateWithVolumeGroups(pageCounter, rcontext, volGroups).iterator();
                } catch (PaginatorException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Convert a list of {@link BlockSequence}s, grouped in "volume groups" that start with a hard
     * volume break, to a list of {@link SheetDataSource}.
     */
    private List<SheetDataSource> prepareToPaginateWithVolumeGroups(
        PageCounter pageCounter,
        DefaultContext rcontext,
        Iterable<List<BlockSequence>> volGroups
    ) throws PaginatorException {
        List<SheetDataSource> ret = new ArrayList<>();
        int i = 0;
        for (List<BlockSequence> glist : volGroups) {
            ret.add(prepareToPaginate(pageCounter, rcontext, i++, glist));
        }
        return ret;
    }

    /**
     * Convert a list of {@link BlockSequence}s to a single {@link SheetDataSource}.
     */
    private SheetDataSource prepareToPaginate(
        PageCounter pageCounter,
        DefaultContext rcontext,
        Integer volumeGroup,
        List<BlockSequence> seqs
    ) throws PaginatorException {
        return new SheetDataSource(pageCounter, context.getFormatterContext(), rcontext, volumeGroup, seqs);
    }

    /**
     * Informs the volume provider that the caller has finished requesting volumes.
     * <b>Note: only use after all volumes have been calculated.</b>
     *
     * @return returns true if the volumes can be accepted, false otherwise
     */
    boolean done() {
        crh.commitBreakable();
        crh.commitTransitionProperties();
        crh.trimPageDetails();
        crh.setVolumeCount(groups.getVolumeCount());
        crh.setSheetsInDocument(groups.countTotalSheets());
        //crh.setPagesInDocument(value);
        if (!crh.isDirty()) {
            return true;
        } else {
            crh.setDirty(false);
            logger.fine("Things didn't add up, running another iteration (" + j + ")");
        }
        j++;
        return false;
    }

    boolean hasNext() {
        return groups.hasNext();
    }

}
