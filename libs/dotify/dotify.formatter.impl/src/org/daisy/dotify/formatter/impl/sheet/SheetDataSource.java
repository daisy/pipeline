package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.TransitionBuilderProperties.ApplicationRange;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.splitter.DefaultSplitResult;
import org.daisy.dotify.common.splitter.SplitPointDataSource;
import org.daisy.dotify.common.splitter.SplitResult;
import org.daisy.dotify.common.splitter.Supplements;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.TransitionContent;
import org.daisy.dotify.formatter.impl.datatype.VolumeKeepPriority;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.PageImpl;
import org.daisy.dotify.formatter.impl.page.PageSequenceBuilder2;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.DocumentSpace;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.PageId;
import org.daisy.dotify.formatter.impl.search.SheetIdentity;
import org.daisy.dotify.formatter.impl.search.TransitionProperties;

/**
 * Provides a data source for sheets. Given a list of 
 * BlockSequences, sheets are produced one by one.
 * 
 * @author Joel Håkansson
 */
public class SheetDataSource implements SplitPointDataSource<Sheet, SheetDataSource> {
	//Global state
	private final PageCounter pageCounter;
	private final FormatterContext context;
	//Input data
	private final DefaultContext rcontext;
	private final Integer volumeGroup;
	private final List<BlockSequence> seqsIterator;
	private final int sheetOffset;
	//Local state
	private int seqsIndex;
	private PageSequenceBuilder2 psb;
	private SectionProperties sectionProperties;
	private int sheetIndex;
	private int pageIndex;
	private String counter;
	private int initialPageOffset;
	private boolean volBreakAllowed;
	private boolean updateCounter;
	private boolean allowsSplit;
	private boolean wasSplitInsideSequence;
	private boolean volumeEnded;
	//Output buffer
	private List<Sheet> sheetBuffer;

	public SheetDataSource(PageCounter pageCounter, FormatterContext context, DefaultContext rcontext, Integer volumeGroup, List<BlockSequence> seqsIterator) {
		this.pageCounter = pageCounter;
		this.context = context;
		this.rcontext = rcontext;
		this.volumeGroup = volumeGroup;
		this.seqsIterator = seqsIterator;
		this.sheetBuffer = new ArrayList<>();
		this.volBreakAllowed = true;
		this.sheetOffset = 0;
		this.seqsIndex = 0;
		this.psb = null;
		this.sectionProperties = null;
		this.sheetIndex = 0;
		this.pageIndex = 0;
		this.counter = null;
		this.initialPageOffset = 0;
		this.updateCounter = false;
		this.allowsSplit = true;
		this.wasSplitInsideSequence = false;
		this.volumeEnded = false;
	}
	
	public SheetDataSource(SheetDataSource template) {
		this(template, 0, false);
	}

	/**
	 * Creates a new instance with the specified data source as template
	 * @param template the template
	 * @param offset the sheetBuffer offset (items before the offset are discarded)
	 * @param tail true if the purpose of this instance is to become the template's
	 * 		tail. This information is required because the page number counter
	 * 		must be the same instance in this case. The reverse is true in other
	 * 		cases.
	 */
	private SheetDataSource(SheetDataSource template, int offset, boolean tail) {
		this.pageCounter = tail?template.pageCounter:new PageCounter(template.pageCounter);
		this.context = template.context;
		this.rcontext = template.rcontext;
		this.volumeGroup = template.volumeGroup;
		this.seqsIterator = template.seqsIterator;
		this.seqsIndex = template.seqsIndex;
		this.psb = tail?template.psb:PageSequenceBuilder2.copyUnlessNull(template.psb);
		this.sectionProperties = template.sectionProperties;
		this.sheetOffset = template.sheetOffset+offset;
		this.sheetIndex = template.sheetIndex;
		this.pageIndex = template.pageIndex;
		if (template.sheetBuffer.size()>offset) {
			this.sheetBuffer = new ArrayList<>(template.sheetBuffer.subList(offset, template.sheetBuffer.size()));
		} else {
			this.sheetBuffer = new ArrayList<>();
		}
		this.volBreakAllowed = template.volBreakAllowed;
		this.counter = template.counter;
		this.initialPageOffset = template.initialPageOffset;
		this.updateCounter = tail?true:template.updateCounter;
		this.allowsSplit = true;
		this.wasSplitInsideSequence = template.wasSplitInsideSequence;
		this.volumeEnded = false;
	}
	
	@Override
	public Sheet get(int index) throws RestartPaginationException {
		if (!ensureBuffer(index+1)) {
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
		return ensureBuffer(index+1);
	}

	@Override
	public int getSize(int limit)  throws RestartPaginationException {
		if (!ensureBuffer(limit-1))  {
			//we have buffered all elements
			return sheetBuffer.size();
		} else {
			return limit;
		}
	}

	@Override
	public boolean isEmpty() {
		return seqsIndex>=seqsIterator.size() && sheetBuffer.isEmpty() && (psb==null || !psb.hasNext());
	}

	@Override
	public Supplements<Sheet> getSupplements() {
		return null;
	}
	
	/**
	 * Ensures that there are at least index elements in the buffer.
	 * When index is -1 this method always returns false.
	 * @param index the index (or -1 to get all remaining elements)
	 * @return returns true if the index element was available, false otherwise
	 */
	private boolean ensureBuffer(int index) {
		Sheet.Builder s = null;
		SheetIdentity si = null;
		while (index<0 || sheetBuffer.size()<index) {
			if (updateCounter) { 
				if(counter!=null) {
					initialPageOffset = rcontext.getRefs().getPageNumberOffset(counter) - psb.size();
				} else {
					initialPageOffset = pageCounter.getDefaultPageOffset() - psb.size();
				}
				updateCounter = false;
			}
			if (psb==null || !psb.hasNext()) {
				if (s!=null) {
					//Last page in the sequence doesn't need volume keep priority
					sheetBuffer.add(s.build());
					s=null;
					continue;
				}
				if (seqsIndex>=seqsIterator.size()) {
					// cannot ensure buffer, return false
					return false;
				}
				// init new sequence
				BlockSequence bs = seqsIterator.get(seqsIndex);
				seqsIndex++;
				counter = bs.getSequenceProperties().getPageCounterName().orElse(null);
				if (bs.getInitialPageNumber()!=null) {
					 initialPageOffset = bs.getInitialPageNumber() - 1;
				} else if (counter!=null) {
					initialPageOffset = Optional.ofNullable(rcontext.getRefs().getPageNumberOffset(counter)).orElse(0);
				} else {
					 initialPageOffset = pageCounter.getDefaultPageOffset();
				}
				psb = new PageSequenceBuilder2(pageCounter.getPageCount(), bs.getLayoutMaster(), initialPageOffset, bs, context, rcontext, seqsIndex);
				sectionProperties = bs.getLayoutMaster().newSectionProperties();
				s = null;
				si = null;
				sheetIndex = 0;
				pageIndex = 0;
			}
			int currentSize = sheetBuffer.size();
			while (psb.hasNext() && currentSize == sheetBuffer.size()) {
				if (!sectionProperties.duplex() || pageIndex % 2 == 0 || volumeEnded || s==null) {
					if (s!=null) {
						Sheet r = s.build();
						sheetBuffer.add(r);
						s = null;
						if (volumeEnded) {
							pageIndex += pageIndex%2==1?1:0;
						}
						continue;
					} else if (volumeEnded) {
						throw new AssertionError("Error in code.");
					}
					volBreakAllowed = true;
					s = new Sheet.Builder(sectionProperties);
					si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume(), volumeGroup, sheetBuffer.size()+sheetOffset);
					sheetIndex++;
				}

				TransitionContent transition = null;
				if (context.getTransitionBuilder().getProperties().getApplicationRange()!=ApplicationRange.NONE) {
					if (!allowsSplit && index-1==sheetBuffer.size()) {
						if ((!sectionProperties.duplex() || pageIndex % 2 == 1)) {
							transition = context.getTransitionBuilder().getInterruptTransition();
						} else if (context.getTransitionBuilder().getProperties().getApplicationRange()==ApplicationRange.SHEET) {
							// This id is the same id as the one created below in the call to nextPage
							PageId thisPageId = psb.nextPageId(0);
							// This gets the page details for the next page in this sequence (if any)
							Optional<PageDetails> next = rcontext.getRefs().findNextPageInSequence(thisPageId);
							// If there is a page details in this sequence and volume break is preferred on this page
							if (next.isPresent()) {
								TransitionProperties st = rcontext.getRefs().getTransitionProperties(thisPageId);
								double v1 = st.getVolumeKeepPriority().orElse(10) + (st.hasBlockBoundary()?0.5:0);
								st = rcontext.getRefs().getTransitionProperties(next.get().getPageId());
								double v2 = st.getVolumeKeepPriority().orElse(10) + (st.hasBlockBoundary()?0.5:0);
								if (v1>v2) {
									//break here
									transition = context.getTransitionBuilder().getInterruptTransition();
								}
							}
						}
						volumeEnded = transition!=null;
					} else if (wasSplitInsideSequence && sheetBuffer.size()==0  && (!sectionProperties.duplex() || pageIndex % 2 == 0)) {
						transition = context.getTransitionBuilder().getResumeTransition();
					}
				}
				boolean hyphenateLastLine = 
						!(	!context.getConfiguration().allowsEndingVolumeOnHyphen() 
								&& sheetBuffer.size()==index-1 
								&& (!sectionProperties.duplex() || pageIndex % 2 == 1));
				
				PageImpl p = psb.nextPage(initialPageOffset, hyphenateLastLine, Optional.ofNullable(transition));
				pageCounter.increasePageCount();
				VolumeKeepPriority vpx = p.getAvoidVolumeBreakAfter();
				if (context.getTransitionBuilder().getProperties().getApplicationRange()==ApplicationRange.SHEET) {
					Sheet sx = s.build();
					if (!sx.getPages().isEmpty()) {
						VolumeKeepPriority vp = sx.getAvoidVolumeBreakAfterPriority();
						if (vp.orElse(10)>vpx.orElse(10)) {
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
					//TODO: the following is a low effort way of giving existing uses of non-breakable units a high priority, but it probably shouldn't be done this way
					if (!br) {
						s.avoidVolumeBreakAfterPriority(VolumeKeepPriority.of(1));
					}
					s.breakable(br);
				}

				setPreviousSheet(si.getSheetIndex()-1, Math.min(p.keepPreviousSheets(), sheetIndex-1), rcontext);
				volBreakAllowed &= p.allowsVolumeBreak();
				if (!sectionProperties.duplex() || pageIndex % 2 == 1) {
					rcontext.getRefs().keepBreakable(si, volBreakAllowed);
				}
				s.add(p);
				pageIndex++;
			}
			if (!psb.hasNext()||volumeEnded) {
				if (!psb.hasNext()) {
					rcontext.getRefs().setSequenceScope(new DocumentSpace(rcontext.getSpace(), rcontext.getCurrentVolume()), seqsIndex, psb.getGlobalStartIndex(), psb.getToIndex());
				}
				if (counter!=null) {
					rcontext.getRefs().setPageNumberOffset(counter, initialPageOffset + psb.getSizeLast());
				} else {
					pageCounter.setDefaultPageOffset(initialPageOffset + psb.getSizeLast());
				}
			}
		}
		return true;
	}

	private void setPreviousSheet(int start, int p, DefaultContext rcontext) {
		int i = 0;
		//TODO: simplify this?
		for (int x = start; i < p && x > 0; x--) {
			SheetIdentity si = new SheetIdentity(rcontext.getSpace(), rcontext.getCurrentVolume(), volumeGroup, x);
			rcontext.getRefs().keepBreakable(si, false);
			i++;
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

	@Override
	public SplitResult<Sheet, SheetDataSource> splitInRange(int atIndex) {
		if (!ensureBuffer(atIndex)) {
			throw new IndexOutOfBoundsException("" + atIndex);
		}
		if (counter!=null) {
			rcontext.getRefs().setPageNumberOffset(counter, initialPageOffset + psb.getSizeLast());
		} else {
			pageCounter.setDefaultPageOffset(initialPageOffset + psb.getSizeLast());
		}
		wasSplitInsideSequence = psb.hasNext();
		if (atIndex==0) {
			return new DefaultSplitResult<Sheet, SheetDataSource>(Collections.emptyList(), new SheetDataSource(this, atIndex, true));
		} else {
			return new DefaultSplitResult<Sheet, SheetDataSource>(sheetBuffer.subList(0, atIndex), new SheetDataSource(this, atIndex, true));
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
