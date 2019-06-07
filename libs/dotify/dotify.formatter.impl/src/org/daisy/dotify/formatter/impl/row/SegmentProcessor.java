package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.DefaultAttributeWithContext;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.RowImpl.Builder;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.AnchorSegment;
import org.daisy.dotify.formatter.impl.segment.Evaluate;
import org.daisy.dotify.formatter.impl.segment.IdentifierSegment;
import org.daisy.dotify.formatter.impl.segment.LeaderSegment;
import org.daisy.dotify.formatter.impl.segment.MarkerSegment;
import org.daisy.dotify.formatter.impl.segment.PageNumberReference;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Segment.SegmentType;
import org.daisy.dotify.formatter.impl.segment.Style;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

class SegmentProcessor implements SegmentProcessing {
	private final List<Segment> segments;
	private final CrossReferenceHandler refs;
	private final AttributeWithContext attr;

	private Context context;
	private final boolean significantContent;
	private final SegmentProcessorContext spc;

	private int segmentIndex;
	private RowImpl.Builder currentRow;
	private final ArrayList<Marker> groupMarkers;
	private final ArrayList<String> groupAnchors;
	private final ArrayList<String> groupIdentifiers;
	private AggregatedBrailleTranslatorResult.Builder layoutOrApplyAfterLeader;
	private String currentLeaderMode;
	private boolean seenSegmentAfterLeader;
	private final LeaderManager leaderManager;
	private ListItem item;
	private int forceCount;
	private int minLeft;
	private int minRight;
	private boolean empty;
	private CurrentResult cr;
	private boolean closed;
	private String blockId;

	SegmentProcessor(String blockId, List<Segment> segments, int flowWidth, CrossReferenceHandler refs, Context context, int available, BlockMargin margins, FormatterCoreContext fcontext, RowDataProperties rdp) {
		this.refs = refs;
		this.segments = Collections.unmodifiableList(removeStyles(segments).collect(Collectors.toList()));
		this.attr = buildAttributeWithContext(null, segments);
		this.context = context;
		this.groupMarkers = new ArrayList<>();
		this.groupAnchors = new ArrayList<>();
		this.groupIdentifiers = new ArrayList<>();
		this.leaderManager = new LeaderManager();
		this.significantContent = calculateSignificantContent(this.segments, context, rdp);
		this.spc = new SegmentProcessorContext(fcontext, rdp, margins, flowWidth, available);
		this.blockId = blockId;
		initFields();
	}
	
	SegmentProcessor(SegmentProcessor template) {
		// Refs is mutable, but for now we assume that the same context should be used.
		this.refs = template.refs;
		// Context is mutable, but for now we assume that the same context should be used.
		this.context = template.context;
		this.spc = template.spc;
		this.currentRow = template.currentRow==null?null:new RowImpl.Builder(template.currentRow);
		this.groupAnchors = new ArrayList<>(template.groupAnchors);
		this.groupMarkers = new ArrayList<>(template.groupMarkers);
		this.groupIdentifiers = new ArrayList<>(template.groupIdentifiers);
		this.leaderManager = new LeaderManager(template.leaderManager);
		this.layoutOrApplyAfterLeader = template.layoutOrApplyAfterLeader==null?null:new AggregatedBrailleTranslatorResult.Builder(template.layoutOrApplyAfterLeader);
		this.currentLeaderMode = template.currentLeaderMode;
		this.seenSegmentAfterLeader = template.seenSegmentAfterLeader;
		this.item = template.item;
		this.forceCount = template.forceCount;
		this.minLeft = template.minLeft;
		this.minRight = template.minRight;
		this.empty  = template.empty;
		this.segments = template.segments;
		this.attr = template.attr;
		this.segmentIndex = template.segmentIndex;
		this.cr = template.cr!=null?template.cr.copy():null;
		this.closed = template.closed;
		this.significantContent = template.significantContent;
		this.blockId = template.blockId;
	}
	
	/**
	 * Filters the input list to remove styles (if present). Segments inside styles are inserted
	 * at the current location in the list.
	 * @param segments segments containing styles
	 * @return a stream of segments without styles
	 */
	private static Stream<Segment> removeStyles(List<Segment> segments) {
		return segments.stream()
				.flatMap(v->v.getSegmentType()==SegmentType.Style?removeStyles(((Style)v).getSegments()):Stream.of(v));
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
	 * @param in the segments
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
		if (name!=null) {
			// Scan segments for style scope
			for (Segment v : in) {
				// If significant content is encountered, set start and end of trim zone.
				if (v.getSegmentType()==SegmentType.Text || v.getSegmentType()==SegmentType.Evaluate || v.getSegmentType()==SegmentType.Reference || v.getSegmentType()==SegmentType.Style) {
					if (start<0) {
						start = i;
						end = i;
					} else {
						end = i;
					}
				}
				i++;
			}
			trimStart = start>0 && in.size()>1;
			trimEnd = end<in.size()-1 && in.size()>1;
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
			if (trimStart && i==start) {
				DefaultAttributeWithContext.Builder c = b;
				b = new DefaultAttributeWithContext.Builder(name);
				b.add(c.build(sw));
				sw = 0;
			}
			if (v.getSegmentType()==SegmentType.Style) {
				Style s = ((Style)v);
				DefaultAttributeWithContext a = buildAttributeWithContext(s.getName(), s.getSegments());
				b.add(a);
				w += a.getWidth();
				sw += a.getWidth();
			} else {
				DefaultAttributeWithContext a = new DefaultAttributeWithContext.Builder().build(1);
				b.add(a);
				w ++;
				sw ++;
			}
			if (trimEnd && i==end) {
				DefaultAttributeWithContext.Builder c = b;
				b = new DefaultAttributeWithContext.Builder();
				b.add(c.build(sw));
				sw = 0;
			}
			i++;
		}
		return b.build(w);
	}
	
	private static boolean calculateSignificantContent(Iterable<Segment> segments, Context context, RowDataProperties rdp) {
		for (Segment s : segments) {
			switch (s.getSegmentType()) {
				case Marker:
				case Anchor:
				case Identifier:
					// continue
					break;
				case Evaluate:
					if (!((Evaluate)s).getExpression().render(context).isEmpty()) {
						return true;
					}
					break;
				case Text:
					if (!((TextSegment)s).getText().isEmpty()) {
						return true;
					}
					break;
				case Style:
					if (!calculateSignificantContent(((Style)s).getSegments(), context, rdp)) {
						break;
					}
				case NewLine:
				case Leader:
				case Reference:
				default:
					return true;
			}
		}
		return rdp.getUnderlineStyle()!=null;
	}

	private void initFields() {
		segmentIndex = 0;
		currentRow = null;
		leaderManager.discardAllLeaders();
		layoutOrApplyAfterLeader = null;
		currentLeaderMode = null;
		seenSegmentAfterLeader = false;
		item = spc.getRdp().getListItem();
		minLeft = spc.getFlowWidth();
		minRight = spc.getFlowWidth();
		empty = true;
		cr = null;
		closed = false;
		if (blockId != null && !"".equals(blockId)) {
			groupIdentifiers.add(blockId);
		}
		// produce group markers and anchors
		getNext(false, LineProperties.DEFAULT);
	}

	private boolean couldTriggerNewRow() {
		if (!hasSegments()) {
			//There's a lot of conditions to keep track of here, but hopefully we can simplify later on
			return !closed && (currentRow!=null || !empty && spc.getRdp().getUnderlineStyle()!=null || leaderManager.hasLeader());
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
				return !((Evaluate)s).getExpression().render(context).isEmpty();
			case Text:
				if (((TextSegment)s).getText().isEmpty()) {
					TextSegment ts = (TextSegment)s;
					if (canMergeWithSegmentAtIndex(ts, index+1)) {
						return couldTriggerNewRow(index+1);
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
		return	// There's a next segment
				index<segments.size()  
				// and that segment is a text segment
				&& segments.get(index).getSegmentType()==SegmentType.Text 
				// and it has the same properties
				&& ((TextSegment)segments.get(index)).getTextProperties().equals(ts.getTextProperties());
	}

	boolean hasMoreData() {
		return hasSegments() || !closed || cr!=null && cr.hasNext(this);
	}
	
	private boolean hasSegments() {
		return segmentIndex<segments.size();
	}

	void prepareNext() {
		if (!hasMoreData()) {
			throw new IllegalStateException();
		}
		if (cr == null) {
			if (!hasSegments() && !closed) {
				closed = true;
				cr = new CloseResult(spc, layoutLeader());
			} else {
				cr = loadNextSegment().orElse(null);
			}
		}
	}
	
	boolean hasNext() {
		return cr!=null && cr.hasNext(this);
	}
	
	public boolean hasSignificantContent() {
		return significantContent;
	}
	
	Optional<RowImpl> getNext(LineProperties lineProps) {
		return getNext(true, lineProps);
	}

	private Optional<RowImpl> getNext(boolean produceRow, LineProperties lineProps) {
		while (true) {
			if (cr!=null && cr.hasNext(this)) {
				try {
					Optional<RowImpl> ret = cr.process(this, lineProps);
					if (ret.isPresent()) {
						if (!produceRow) {
							// there is a test below that verifies that the current segment cannot produce a row result
							// and the segment was processed under this assumption. If a row has been produced anyway, that's an error
							// in the code.
							throw new RuntimeException("Error in code");
						}
						return ret;
					} // else try the next segment.
				} finally {
					if (!cr.hasNext(this)) {
						cr = null;
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

	private Optional<CurrentResult> loadNextSegment() {
		Segment s = segments.get(segmentIndex);
		segmentIndex++;
		switch (s.getSegmentType()) {
			case NewLine:
				//flush
				return Optional.of(new NewLineResult(spc, layoutLeader()));
			case Text:
				int len = 1;
				int fromIndex = segmentIndex-1;
				TextSegment ts = (TextSegment)s;
				while (canMergeWithSegmentAtIndex(ts, segmentIndex)) {
					len++;
					segmentIndex++;
				}
				return layoutTextSegment(ts, fromIndex, len);
			case Leader:
				return layoutLeaderSegment((LeaderSegment)s);
			case Reference:
				return layoutPageSegment((PageNumberReference)s);
			case Evaluate:
				return layoutEvaluate((Evaluate)s);
			case Marker:
				applyAfterLeader((MarkerSegment)s);
				return Optional.empty();
			case Anchor:
				applyAfterLeader((AnchorSegment)s);
				return Optional.empty();
			case Identifier:
				applyAfterLeader((IdentifierSegment)s);
				return Optional.empty();
			default:
				return Optional.empty();
		}
	}

	@Override
	public RowImpl flushCurrentRow() {
		if (empty) {
			// Clear group anchors and markers (since we have content, we don't need them)
			currentRow.addAnchors(0, groupAnchors);
			groupAnchors.clear();
			currentRow.addMarkers(0, groupMarkers);
			groupMarkers.clear();
			currentRow.addIdentifiers(0, groupIdentifiers);
			groupIdentifiers.clear();
		}
		RowImpl r = currentRow.build();
		empty = false;
		//Make calculations for underlining
		int width = r.getChars().length();
		int left = r.getLeftMargin().getContent().length();
		int right = r.getRightMargin().getContent().length();
		int space = spc.getFlowWidth() - width - left - right;
		left += r.getAlignment().getOffset(space);
		right = spc.getFlowWidth() - width - left;
		minLeft = Math.min(minLeft, left);
		minRight = Math.min(minRight, right);
		currentRow = null;
		return r;
	}

	private Optional<CurrentResult> layoutTextSegment(TextSegment ts, int fromIndex, int length) {
		String mode = ts.getTextProperties().getTranslationMode();
		BrailleTranslatorResult btr = null;
		if (!ts.canMakeResult()) {
			int toIndex = fromIndex+length;
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
			CurrentResult cr = new CurrentResultImpl(spc, btr, mode);
			return Optional.of(cr);
		}
		return Optional.empty();
	}
	
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
		if (refs!=null) {
			rs.setResolver(()->{
				Integer page = refs.getPageNumber(rs.getRefId());
				if (page==null) {
					return "??";
				} else {
					return "" + rs.getNumeralStyle().format(page);
				}
			});
		} else {
			rs.setResolver(()->"??");
		}
		//TODO: translate references using custom language?
		TranslatableWithContext spec;
		spec = TranslatableWithContext.from(segments, segmentIndex-1)
				.attributes(attr)
				.build();
		if (leaderManager.hasLeader()) {
			layoutAfterLeader(spec, null);
		} else {
			String mode = null;
			BrailleTranslatorResult btr = toResult(spec, null);
			CurrentResult cr = new CurrentResultImpl(spc, btr, mode);
			return Optional.of(cr);
		}
		return Optional.empty();
	}
	
	private Optional<CurrentResult> layoutEvaluate(Evaluate e) {
		e.setResolver(()->e.getExpression().render(getContext()));
		if (!e.peek().isEmpty()) { // Don't create a new row if the evaluated expression is empty
		                    // Note: this could be handled more generally (also for regular text) in layout().
			TranslatableWithContext spec = TranslatableWithContext.from(segments, segmentIndex-1)
					.attributes(attr)
					.build();
			if (leaderManager.hasLeader()) {
				layoutAfterLeader(spec, null);
			} else {
				String mode = null;
				BrailleTranslatorResult btr = toResult(spec, mode);
				CurrentResult cr = new CurrentResultImpl(spc, btr, mode);
				return Optional.of(cr);
			}
		}
		return Optional.empty(); 
	}
	
	private void layoutAfterLeader(TranslatableWithContext spec, String mode) {
		layoutAfterLeader(toResult(spec, mode), mode);
	}

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
	
	private void applyAfterLeader(MarkerSegment marker) {
		if (leaderManager.hasLeader()) {
			if (layoutOrApplyAfterLeader == null) {
				layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
			}
			layoutOrApplyAfterLeader.add(marker);
		} else {
			if (currentRow==null) {
				groupMarkers.add(marker);
			} else {
				currentRow.addMarker(marker);
			}
		}
	}
	
	private void applyAfterLeader(final AnchorSegment anchor) {
		if (leaderManager.hasLeader()) {
			if (layoutOrApplyAfterLeader == null) {
				layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
			}
			layoutOrApplyAfterLeader.add(anchor);
		} else {
			if (currentRow==null) {
				groupAnchors.add(anchor.getReferenceID());
			} else {
				currentRow.addAnchor(anchor.getReferenceID());
			}
		}
	}
	
	private void applyAfterLeader(final IdentifierSegment identifier) {
		if (leaderManager.hasLeader()) {
			if (layoutOrApplyAfterLeader == null) {
				layoutOrApplyAfterLeader = new AggregatedBrailleTranslatorResult.Builder();
			}
			layoutOrApplyAfterLeader.add(identifier);
		} else {
			if (currentRow==null) {
				groupIdentifiers.add(identifier.getName());
			} else {
				currentRow.addIdentifier(identifier.getName());
			}
		}
	}
	
	private Optional<CurrentResult> layoutLeader() {
		if (leaderManager.hasLeader()) {
			// layout() sets currentLeader to null
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
			CurrentResult cr = new CurrentResultImpl(spc, btr, mode);
			return Optional.of(cr);
		}
		return Optional.empty();
	}

	private BrailleTranslatorResult toResult(String c) {
		return toResult(Translatable.text(spc.getFormatterContext().getConfiguration().isMarkingCapitalLetters()?c:c.toLowerCase()).build(), null);
	}
	
	private BrailleTranslatorResult toResult(Translatable spec, String mode) {
		try {
			return spc.getFormatterContext().getTranslator(mode).translate(spec);
		} catch (TranslationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private BrailleTranslatorResult toResult(TranslatableWithContext spec, String mode) {
		try {
			return spc.getFormatterContext().getTranslator(mode).translate(spec);
		} catch (TranslationException e) {
			throw new RuntimeException(e);
		}		
	}

	void reset() {
		groupAnchors.clear();
		groupMarkers.clear();
		groupIdentifiers.clear();
		initFields();
	}
	
	List<Marker> getGroupMarkers() {
		return groupMarkers;
	}
	
	List<String> getGroupAnchors() {
		return groupAnchors;
	}
	
	List<String> getGroupIdentifiers() {
		return groupIdentifiers;
	}
	
	void setContext(DefaultContext context) {
		this.context = context;
	}
	
	private Context getContext() {
		return context;
	}
	
	int getForceCount() {
		return forceCount;
	}

	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public boolean hasCurrentRow() {
		return currentRow!=null;
	}

	@Override
	public int getUnusedLeft() {
		return minLeft;
	}

	@Override
	public int getUnusedRight() {
		return minRight;
	}

	@Override
	public void newCurrentRow(MarginProperties left, MarginProperties right) {
		currentRow = spc.getRdp().configureNewEmptyRowBuilder(left, right);
	}

	@Override
	public Builder getCurrentRow() {
		return currentRow;
	}

	@Override
	public void addToForceCount(double value) {
		forceCount += value;
	}

	@Override
	public LeaderManager getLeaderManager() {
		return leaderManager;
	}

	@Override
	public boolean hasListItem() {
		return item!=null;
	}

	@Override
	public void discardListItem() {
		item = null;
	}

	@Override
	public ListItem getListItem() {
		Objects.requireNonNull(item);
		return item;
	}
}
