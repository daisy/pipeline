package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.SequenceProperties.SequenceBreakBefore;
import org.daisy.dotify.common.splitter.SplitPoint;
import org.daisy.dotify.common.splitter.SplitPointCost;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitPointHandler;
import org.daisy.dotify.common.splitter.SplitPointSpecification;
import org.daisy.dotify.common.splitter.StandardSplitOption;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.datatype.VolumeKeepPriority;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.PageImpl;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.AnchorData;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.Space;
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

/**
 * Provides contents in volumes.
 *  
 * @author Joel Håkansson
 *
 */
public class VolumeProvider {
	private static final Logger logger = Logger.getLogger(VolumeProvider.class.getCanonicalName());
	private static final int DEFAULT_SPLITTER_MAX = 50;
	private final List<BlockSequence> blocks;
	private final CrossReferenceHandler crh;
	private SheetGroupManager groups;
	private final SplitPointHandler<Sheet, SheetDataSource> volSplitter;

	private int pageIndex = 0;
	private int currentVolumeNumber=0;
	private boolean init = false;
	private int j = 1;
	
	private final SplitterLimit splitterLimit;
    private final Stack<VolumeTemplate> volumeTemplates;
    private final LazyFormatterContext context;

	/**
	 * Creates a new volume provider with the specifed parameters
	 * @param blocks the block sequences
	 * @param volumeTemplates volume templates
	 * @param context the formatter context
	 * @param crh the cross reference handler
	 */
	VolumeProvider(List<BlockSequence> blocks, Stack<VolumeTemplate> volumeTemplates, LazyFormatterContext context, CrossReferenceHandler crh) {
		this.blocks = blocks;
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
		this.crh = crh;
		this.volSplitter = new SplitPointHandler<>();
	}
		
	/**
	 * Resets the volume provider to its initial state (with some information preserved). 
	 * @throws RestartPaginationException
	 */
	void prepare() {
		if (!init) {
			groups = new SheetGroupManager(splitterLimit);
			// make a preliminary calculation based on a contents only
			Iterable<SheetDataSource> allUnits = prepareToPaginateWithVolumeGroups(blocks, new DefaultContext.Builder(crh).space(Space.BODY).build());
			int volCount = 0;
			for (SheetDataSource data : allUnits) {
				SheetGroup g = groups.add();
				g.setUnits(data);
				g.getSplitter().updateSheetCount(data.getRemaining().size(), data.getRemaining().size());
				volCount += g.getSplitter().getVolumeCount();
			}
			crh.setVolumeCount(volCount);
			//if there is an error, we won't have a proper initialization and have to retry from the beginning
			init = true;
		}
		Iterable<SheetDataSource> allUnits = prepareToPaginateWithVolumeGroups(blocks, new DefaultContext.Builder(crh).space(Space.BODY).build());
		int i=0;
		for (SheetDataSource unit : allUnits) {
			groups.atIndex(i).setUnits(unit);
			i++;
		}
		pageIndex = 0;
		currentVolumeNumber=0;

		groups.resetAll();
	}
	
	/**
	 * @return returns the next volume
	 * @throws RestartPaginationException if pagination should be restarted
	 */
	VolumeImpl nextVolume() {
		currentVolumeNumber++;
		VolumeImpl volume = new VolumeImpl(crh.getOverhead(currentVolumeNumber));
		ArrayList<AnchorData> ad = new ArrayList<>();
		volume.setPreVolData(updateVolumeContents(currentVolumeNumber, ad, true));
		volume.setBody(nextBodyContents(volume.getOverhead().total(), ad));
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Sheets  in volume " + currentVolumeNumber + ": " + (volume.getVolumeSize()) + 
					", content:" + volume.getBodySize() +
					", overhead:" + volume.getOverhead());
		}
		volume.setPostVolData(updateVolumeContents(currentVolumeNumber, ad, false));
		crh.setSheetsInVolume(currentVolumeNumber, volume.getBodySize() + volume.getOverhead().total());
		//crh.setPagesInVolume(i, value);
		crh.setAnchorData(currentVolumeNumber, ad);
		crh.setOverhead(currentVolumeNumber, volume.getOverhead());
		return volume;

	}
	
	/**
	 * Gets the contents of the next volume
	 * @param overhead the number of sheets in this volume that's not part of the main body of text
	 * @param ad the anchor data
	 * @return returns the contents of the next volume
	 */
	private SectionBuilder nextBodyContents(final int overhead, ArrayList<AnchorData> ad) {
		groups.currentGroup().setOverheadCount(groups.currentGroup().getOverheadCount() + overhead);
		final int splitterMax = splitterLimit.getSplitterLimit(currentVolumeNumber);
		final int targetSheetsInVolume = (groups.lastInGroup()?splitterMax:groups.sheetsInCurrentVolume());
		//Not using lambda for now, because it's noticeably slower.
		SplitPointCost<Sheet> cost = new SplitPointCost<Sheet>(){
			@Override
			public double getCost(SplitPointDataSource<Sheet, ?> units, int index, int breakpoint) {
				int contentSheetTarget = targetSheetsInVolume - overhead;
				Sheet lastSheet = units.get(index);
				double priorityPenalty = 0;
				int sheetCount = index + 1;
				// Calculates a maximum offset based on the maximum possible number of sheets
				double range = splitterMax * 0.4;
				if (!units.isEmpty()) {
					VolumeKeepPriority avoid = lastSheet.getAvoidVolumeBreakAfterPriority();
					if (avoid.hasValue()) {
						// Reverses 1-9 to 9-1 with bounds control and normalizes that to [1/9, 1]
						double normalized = ((10 - avoid.getValue())/9d);
						// Calculates a number of sheets that a high priority can beat
						priorityPenalty = range * normalized;
					}
				}
				// sets the preferred value to targetSheetsInVolume, where cost will be 0
				// including a small preference for bigger volumes
				double distancePenalty = Math.abs(contentSheetTarget - sheetCount) + (contentSheetTarget-sheetCount)*0.001;
				int unbreakablePenalty = lastSheet.isBreakable()?0:100;
				return distancePenalty + priorityPenalty + unbreakablePenalty;
			}};
		SplitPoint<Sheet, SheetDataSource> sp;

		crh.setReadOnly();
		SheetDataSource data = groups.currentGroup().getUnits();
		SheetDataSource copySource = new SheetDataSource(data);
		SplitPointSpecification spec = volSplitter.find(splitterMax-overhead, 
				copySource, 
				cost, StandardSplitOption.ALLOW_FORCE);
		crh.setReadWrite();
		sp = volSplitter.split(spec, groups.currentGroup().getUnits());
		/*
			sp = volSplitter.split(splitterMax-overhead, 
					groups.currentGroup().getUnits(),
					cost, StandardSplitOption.ALLOW_FORCE);
		*/
		groups.currentGroup().setUnits(sp.getTail());
		List<Sheet> contents = sp.getHead();
		int pageCount = Sheet.countPages(contents);
		crh.commitPageDetails();
		crh.setVolumeScope(currentVolumeNumber, pageIndex, pageIndex+pageCount);

		pageIndex += pageCount;
		SectionBuilder sb = new SectionBuilder();
		for (Sheet sheet : contents) {
			for (PageImpl p : sheet.getPages()) {
				for (String id : p.getIdentifiers()) {
					crh.setVolumeNumber(id, currentVolumeNumber);
				}
				if (p.getAnchors().size()>0) {
					ad.add(new AnchorData(p.getAnchors(), p.getPageNumber()));
				}
			}
			sb.addSheet(sheet);
		}
		groups.currentGroup().setSheetCount(groups.currentGroup().getSheetCount() + contents.size());
		groups.nextVolume();
		return sb;
	}
	
	private SectionBuilder updateVolumeContents(int volumeNumber, ArrayList<AnchorData> ad, boolean pre) {
		DefaultContext c = new DefaultContext.Builder(crh)
						.currentVolume(volumeNumber)
						.space(pre?Space.PRE_CONTENT:Space.POST_CONTENT)
						.build();
		try {
			ArrayList<BlockSequence> ib = new ArrayList<>();
			for (VolumeTemplate t : volumeTemplates) {
				if (t.appliesTo(c)) {
					for (VolumeSequence seq : (pre?t.getPreVolumeContent():t.getPostVolumeContent())) {
						BlockSequence s = seq.getBlockSequence(context.getFormatterContext(), c, crh);
						if (s!=null) {
							ib.add(s);
						}
					}
					break;
				}
			}
			List<Sheet> ret = prepareToPaginate(ib, c, null).getRemaining();
			SectionBuilder sb = new SectionBuilder();
			for (Sheet ps : ret) {
				for (PageImpl p : ps.getPages()) {
					if (p.getAnchors().size()>0) {
						ad.add(new AnchorData(p.getAnchors(), p.getPageNumber()));
					}
				}
				sb.addSheet(ps);
			}
			return sb;
		} catch (PaginatorException e) {
			return null;
		}
	}
	
	private SheetDataSource prepareToPaginate(List<BlockSequence> fs, DefaultContext rcontext, Integer volumeGroup) throws PaginatorException {
		return prepareToPaginate(new PageCounter(), rcontext, volumeGroup, fs);
	}
	
	private Iterable<SheetDataSource> prepareToPaginateWithVolumeGroups(List<BlockSequence> fs, DefaultContext rcontext) {
		List<List<BlockSequence>> volGroups = new ArrayList<>();
		List<BlockSequence> currentGroup = new ArrayList<>();
		volGroups.add(currentGroup);
		for (BlockSequence bs : fs) {
			if (bs.getSequenceProperties().getBreakBeforeType()==SequenceBreakBefore.VOLUME) {
				currentGroup = new ArrayList<>();
				volGroups.add(currentGroup);
			}
			currentGroup.add(bs);
		}
		PageCounter pageCounter = new PageCounter();
        crh.resetUniqueChecks();
		return new Iterable<SheetDataSource>(){
			@Override
			public Iterator<SheetDataSource> iterator() {
				try {
					return prepareToPaginateWithVolumeGroups(pageCounter, rcontext, volGroups).iterator();
				} catch (PaginatorException e) {
					throw new RuntimeException(e);
				}
			}};
	}

	private List<SheetDataSource> prepareToPaginateWithVolumeGroups(PageCounter pageCounter, DefaultContext rcontext, Iterable<List<BlockSequence>> volGroups) throws PaginatorException {
		List<SheetDataSource> ret = new ArrayList<>();
		int i = 0;
		for (List<BlockSequence> glist : volGroups) {
			ret.add(prepareToPaginate(pageCounter, rcontext, i++, glist));
		}
		return ret;
	}
	
	private SheetDataSource prepareToPaginate(PageCounter pageCounter, DefaultContext rcontext, Integer volumeGroup, List<BlockSequence> seqs) throws PaginatorException {
		return new SheetDataSource(pageCounter, context.getFormatterContext(), rcontext, volumeGroup, seqs);
	}
	
	/**
	 * Informs the volume provider that the caller has finished requesting volumes.
	 * <b>Note: only use after all volumes have been calculated.</b>
	 * @return returns true if the volumes can be accepted, false otherwise  
	 */
	boolean done() {
		if (groups.hasNext()) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("There is more content (sheets: " + groups.countRemainingSheets() + ", pages: " + groups.countRemainingPages() + ")");
			}
		}
		// this changes the value of groups.getVolumeCount() to the newly computed
		// required number of volume based on groups.countTotalSheets()
		groups.updateAll();
		crh.commitBreakable();
		crh.commitTransitionProperties();
		crh.trimPageDetails();
		crh.setVolumeCount(groups.getVolumeCount());
		crh.setSheetsInDocument(groups.countTotalSheets());
		//crh.setPagesInDocument(value);
		if (!crh.isDirty() && !groups.hasNext()) {
			return true;
		} else {
			crh.setDirty(false);
			logger.info("Things didn't add up, running another iteration (" + j + ")");
		}
		j++;
		return false;
	}

}