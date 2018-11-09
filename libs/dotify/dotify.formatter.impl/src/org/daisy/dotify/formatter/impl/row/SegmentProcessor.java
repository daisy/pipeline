package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.Translatable;
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
import org.daisy.dotify.formatter.impl.segment.PageNumberReferenceSegment;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

class SegmentProcessor implements SegmentProcessing {
	private final List<Segment> segments;
	private final CrossReferenceHandler refs;
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
		this.segments = Collections.unmodifiableList(segments);
		this.refs = refs;
		this.context = context;
		this.groupMarkers = new ArrayList<>();
		this.groupAnchors = new ArrayList<>();
		this.groupIdentifiers = new ArrayList<>();
		this.leaderManager = new LeaderManager();
		this.significantContent = calculateSignificantContent(segments, context, rdp);
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
		this.segmentIndex = template.segmentIndex;
		this.cr = template.cr!=null?template.cr.copy():null;
		this.closed = template.closed;
		this.significantContent = template.significantContent;
		this.blockId = template.blockId;
	}
	
	private static boolean calculateSignificantContent(List<Segment> segments, Context context, RowDataProperties rdp) {
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
		getNext(false, false);
	}

	boolean couldTriggerNewRow() {
		if (!hasSegments()) {
			//There's a lot of conditions to keep track of here, but hopefully we can simplify later on
			return !closed && (currentRow!=null || !empty && spc.getRdp().getUnderlineStyle()!=null || leaderManager.hasLeader());
		}
		Segment s = segments.get(segmentIndex);
		switch (s.getSegmentType()) {
			case Marker:
			case Anchor:
			case Identifier:
				return false;
			case Evaluate:
				return !((Evaluate)s).getExpression().render(context).isEmpty();
			case Text:
				return !((TextSegment)s).getText().isEmpty();
			default:
				return true;
		}
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
	
	Optional<RowImpl> getNext(boolean wholeWordsOnly) {
		return getNext(true, wholeWordsOnly);
	}

	private Optional<RowImpl> getNext(boolean produceRow, boolean wholeWordsOnly) {
		while (true) {
			if (cr!=null && cr.hasNext(this)) {
				try {
					Optional<RowImpl> ret = cr.process(this, wholeWordsOnly);
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
				return layoutTextSegment((TextSegment)s);
			case Leader:
				return layoutLeaderSegment((LeaderSegment)s);
			case Reference:
				return layoutPageSegment((PageNumberReferenceSegment)s);
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

	private Optional<CurrentResult> layoutTextSegment(TextSegment ts) {
		String mode = ts.getTextProperties().getTranslationMode();
		BrailleTranslatorResult btr = null;
		if (!ts.canMakeResult()) {
			Translatable spec = Translatable.text(
					spc.getFormatterContext().getConfiguration().isMarkingCapitalLetters()?
					ts.getText():ts.getText().toLowerCase()
			)
			.locale(ts.getTextProperties().getLocale())
			.hyphenate(ts.getTextProperties().isHyphenating())
			.attributes(ts.getTextAttribute()).build();
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

	private Optional<CurrentResult> layoutPageSegment(PageNumberReferenceSegment rs) {
		Integer page = null;
		if (refs!=null) {
			page = refs.getPageNumber(rs.getRefId());
		}
		//TODO: translate references using custom language?
		Translatable spec;
		if (page==null) {
			spec = Translatable.text("??").locale(null).build();
		} else {
			String txt = "" + rs.getNumeralStyle().format(page);
			spec = Translatable.text(
					spc.getFormatterContext().getConfiguration().isMarkingCapitalLetters()?txt:txt.toLowerCase()
					).locale(null).attributes(rs.getTextAttribute(txt.length())).build();
		}
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
		String txt = e.getExpression().render(context);
		if (!txt.isEmpty()) { // Don't create a new row if the evaluated expression is empty
		                    // Note: this could be handled more generally (also for regular text) in layout().
			Translatable spec = Translatable.text(spc.getFormatterContext().getConfiguration().isMarkingCapitalLetters()?txt:txt.toLowerCase()).
					locale(e.getTextProperties().getLocale()).
					hyphenate(e.getTextProperties().isHyphenating()).
					attributes(e.getTextAttribute(txt.length())).
					build();
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
	
	private void layoutAfterLeader(Translatable spec, String mode) {
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
